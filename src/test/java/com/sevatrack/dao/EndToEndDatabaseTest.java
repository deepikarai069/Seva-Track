package com.sevatrack.dao;

import com.sevatrack.TestData.MutableClock;
import com.sevatrack.model.*;
import com.sevatrack.service.*;
import com.sevatrack.service.ReportService.Report;
import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.ReportFormatter;
import com.sevatrack.web.AppContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs against a real MariaDB that has db/*.sql loaded. Skipped unless DB_URL is set, e.g.
 * DB_URL=jdbc:mariadb://localhost:3306/sevatrack DB_USER=sevatrack DB_PASSWORD=sevatrack mvn test
 * WARNING: wipes complaints, escalations and status_history of that database.
 */
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class EndToEndDatabaseTest {
    private ConnectionProvider cp;
    private MutableClock clock;
    private AppContext app;

    @BeforeEach
    void setUp() throws Exception {
        cp = ConnectionProvider.fromEnv();
        try (Connection c = cp.get(); Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM escalations");
            s.executeUpdate("DELETE FROM complaints");   // status_history cascades
        }
        clock = new MutableClock(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        app = AppContext.create(cp, clock);
    }

    private ComplaintForm form(int category, int ward, String priority) {
        ComplaintForm f = new ComplaintForm();
        f.setCitizenName("Asha Singh");
        f.setCitizenPhone("9876543210");
        f.setTitle("Test complaint " + category);
        f.setDescription("Created by the end to end test, category " + category);
        f.setCategoryId(category);
        f.setWardId(ward);
        f.setPriority(priority);
        return f;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    @Test
    void registerRoutesToTheRightOfficerAndWritesAuditTrailViaTriggers() {
        // Water supply (dept 1), HIGH by default, ward 5 (Gomti Nagar) -> Imran Qureshi (ward 5, emergency)
        Complaint c = app.complaints().register(form(1, 5, null));
        assertEquals(Status.ASSIGNED, c.getStatus());
        assertEquals(103, c.getAssignedOfficerId());
        assertEquals(1, c.getDepartmentId());
        assertEquals(Priority.HIGH, c.getPriority());
        assertEquals(now().plusHours(24), c.getSlaDueAt());

        List<StatusHistory> h = app.complaints().history(c.getId());
        assertEquals(2, h.size());
        assertEquals("CITIZEN", h.get(0).getActor());
        assertEquals(Status.NEW, h.get(0).getNewStatus());
        assertEquals("SYSTEM", h.get(1).getActor());
        assertEquals(Status.ASSIGNED, h.get(1).getNewStatus());
        assertEquals("Imran Qureshi", h.get(1).getNewOfficerName());

        assertEquals(1, app.officers().findById(103).orElseThrow().getOpenLoad());
    }

    @Test
    void routineComplaintGoesToWardOfficerOfTheRightDepartment() {
        Complaint pothole = app.complaints().register(form(4, 3, null));   // Roads, Chowk
        assertEquals(201, pothole.getAssignedOfficerId());                  // Pankaj Tiwari
        Complaint light = app.complaints().register(form(10, 2, null));     // Street light, Aminabad
        assertEquals(403, light.getAssignedOfficerId());                    // only Aminabad owner of ELC
    }

    @Test
    void officerLifecycleIsEnforcedByServiceAndByDatabaseTrigger() {
        Complaint c = app.complaints().register(form(1, 5, null));
        long id = c.getId();

        assertThrows(BusinessException.class, () -> app.complaints().updateStatus(id, 101, Status.IN_PROGRESS, null)); // not owner
        app.complaints().updateStatus(id, 103, Status.IN_PROGRESS, "Crew dispatched");

        // bypass the service: the trigger must still stop an illegal jump
        BusinessException e = assertThrows(BusinessException.class,
                () -> new JdbcComplaintDao(cp).updateStatus(id, Status.CLOSED, "test", null));
        assertTrue(e.getMessage().contains("Illegal"), e.getMessage());

        assertThrows(BusinessException.class, () -> app.complaints().updateStatus(id, 103, Status.RESOLVED, " "));
        app.complaints().updateStatus(id, 103, Status.RESOLVED, "Valve replaced");

        Complaint after = app.complaints().get(id);
        assertEquals(Status.RESOLVED, after.getStatus());
        assertNotNull(after.getResolvedAt(), "trigger should stamp resolved_at");

        List<StatusHistory> h = app.complaints().history(id);
        StatusHistory last = h.get(h.size() - 1);
        assertEquals("OFFICER:Imran Qureshi", last.getActor());
        assertEquals("Valve replaced", last.getNote());

        // resolved complaints can no longer be reassigned (stored procedure rule)
        assertThrows(BusinessException.class, () -> new JdbcComplaintDao(cp).assign(id, 101, null, "test", null));
    }

    @Test
    void unresolvedComplaintClimbsL1L2L3ThenIsFlaggedAsBreached() {
        Complaint c = app.complaints().register(form(1, 5, null));
        long id = c.getId();

        assertEquals(0, app.escalation().runCycle().checked());           // not due yet

        clock.advanceHours(25);                                             // L1 window (HIGH) = 24h
        EscalationService.CycleResult r1 = app.escalation().runCycle();
        assertEquals(1, r1.escalated());
        Complaint l2 = app.complaints().get(id);
        assertEquals(2, l2.getEscalationLevel());
        assertEquals(104, l2.getAssignedOfficerId());                       // Alok Mishra, Water supervisor
        assertEquals(now().plusHours(12), l2.getSlaDueAt());                // L2 window (HIGH) = 12h
        assertEquals(Status.ASSIGNED, l2.getStatus());

        assertEquals(0, app.escalation().runCycle().checked());             // idempotent within the window

        clock.advanceHours(13);
        assertEquals(1, app.escalation().runCycle().escalated());
        Complaint l3 = app.complaints().get(id);
        assertEquals(3, l3.getEscalationLevel());
        assertEquals(105, l3.getAssignedOfficerId());
        assertFalse(l3.isSlaBreached());

        clock.advanceHours(7);                                              // L3 window (HIGH) = 6h
        EscalationService.CycleResult r3 = app.escalation().runCycle();
        assertEquals(1, r3.breached());
        assertTrue(app.complaints().get(id).isSlaBreached());
        assertEquals(0, app.escalation().runCycle().checked(), "breach is reported once");

        List<EscalationRecord> esc = app.complaints().escalations(id);
        assertEquals(2, esc.size());
        assertEquals("Imran Qureshi", esc.get(0).getFromOfficerName());
        assertEquals("Alok Mishra", esc.get(0).getToOfficerName());
        assertEquals(2, esc.get(0).getToLevel());

        List<StatusHistory> h = app.complaints().history(id);
        assertEquals(5, h.size());                                          // registered, assigned, L2, L3, breached
        assertEquals("SLA-MONITOR", h.get(4).getActor());
        assertTrue(h.get(4).getNote().contains("final level"));

        // report: department 1 shows the escalated + breached complaint
        LocalDate today = LocalDate.now();
        Report report = app.reports().build(today.minusDays(1), today.plusDays(3));
        DepartmentStat water = report.departments().stream().filter(d -> d.getDepartmentId() == 1).findFirst().orElseThrow();
        assertEquals(1, water.getTotal());
        assertEquals(1, water.getEscalated());
        assertEquals(1, water.getBreached());
        String json = ReportFormatter.toJson(report);
        assertTrue(json.contains("\"slaBreached\": 1"), json);
        assertTrue(ReportFormatter.toXml(report).contains("slaBreached=\"1\""));
    }

    @Test
    void resolvedComplaintsAreNeverEscalated() {
        Complaint c = app.complaints().register(form(4, 3, null));          // Roads, MEDIUM, 72h
        app.complaints().updateStatus(c.getId(), 201, Status.IN_PROGRESS, null);
        app.complaints().updateStatus(c.getId(), 201, Status.RESOLVED, "Patched");
        clock.advanceHours(200);
        assertEquals(0, app.escalation().runCycle().checked());
        assertEquals(1, app.complaints().get(c.getId()).getEscalationLevel());
    }

    @Test
    void workloadLimitSendsOverflowToNextOfficerOrLeavesUnassigned() {
        // Street lighting ward 6 (Indira Nagar): Shweta (402, max 8). Fill her up with routine complaints.
        for (int i = 0; i < 8; i++) app.complaints().register(form(10, 6, "LOW"));
        assertEquals(8, app.officers().findById(402).orElseThrow().getOpenLoad());
        Complaint overflow = app.complaints().register(form(10, 6, "LOW"));
        // ward owner is full, so a different ELC L1 officer must take it (never above capacity)
        assertNotEquals(402, overflow.getAssignedOfficerId());
        assertNotNull(overflow.getAssignedOfficerId());
    }
}
