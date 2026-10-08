package com.sevatrack.service;

import com.sevatrack.dao.ComplaintDao;
import com.sevatrack.dao.OfficerDao;
import com.sevatrack.dao.SlaPolicyDao;
import com.sevatrack.model.Complaint;
import com.sevatrack.model.Priority;
import com.sevatrack.model.Status;
import com.sevatrack.routing.RoutingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.sevatrack.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EscalationServiceTest {
    private ComplaintDao complaints;
    private OfficerDao officers;
    private EscalationService service;

    @BeforeEach
    void setUp() {
        complaints = mock(ComplaintDao.class);
        officers = mock(OfficerDao.class);
        SlaPolicyDao slaDao = mock(SlaPolicyDao.class);
        when(slaDao.loadAll()).thenReturn(Map.of("HIGH:2", 12, "HIGH:3", 6));
        service = new EscalationService(complaints, officers, new SlaPolicy(slaDao), RoutingEngine.forEscalation(), fixedClock());
    }

    private Complaint overdue(long id, int level) {
        return complaint(id, 1, 5, Priority.HIGH, Status.ASSIGNED, level, NOW.minusHours(1));
    }

    @Test
    void nothingOverdueMeansNothingHappens() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of());
        EscalationService.CycleResult r = service.runCycle();
        assertEquals(0, r.checked());
        verify(complaints, never()).escalate(anyLong(), anyInt(), anyInt(), any(), any());
    }

    @Test
    void levelOneMovesToSupervisorWithTheLevelTwoSlaWindow() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of(overdue(10, 1)));
        when(officers.findActive(1, 2)).thenReturn(List.of(officer(104, 1, null, 2, 3, 15, true)));

        EscalationService.CycleResult r = service.runCycle();

        assertEquals(1, r.escalated());
        verify(complaints).escalate(eq(10L), eq(104), eq(2), eq(NOW.plusHours(12)), contains("escalated to Officer 104"));
    }

    @Test
    void levelTwoMovesToDepartmentHead() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of(overdue(11, 2)));
        when(officers.findActive(1, 3)).thenReturn(List.of(officer(105, 1, null, 3, 0, 30, true)));

        service.runCycle();

        verify(complaints).escalate(eq(11L), eq(105), eq(3), eq(NOW.plusHours(6)), anyString());
    }

    @Test
    void missingTheSlaAtLevelThreeIsFlaggedAsBreachNotEscalatedFurther() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of(overdue(12, 3)));

        EscalationService.CycleResult r = service.runCycle();

        assertEquals(1, r.breached());
        verify(complaints).markBreached(eq(12L), contains("final level"));
        verify(complaints, never()).escalate(anyLong(), anyInt(), anyInt(), any(), any());
        verify(officers, never()).findActive(anyInt(), anyInt());
    }

    @Test
    void noOfficerAtNextLevelLeavesComplaintUntouchedForTheNextCycle() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of(overdue(13, 1)));
        when(officers.findActive(1, 2)).thenReturn(List.of());

        EscalationService.CycleResult r = service.runCycle();

        assertEquals(0, r.escalated());
        assertEquals(0, r.failed());
        verify(complaints, never()).escalate(anyLong(), anyInt(), anyInt(), any(), any());
    }

    @Test
    void oneFailingComplaintDoesNotStopTheRest() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of(overdue(20, 1), overdue(21, 1)));
        when(officers.findActive(1, 2)).thenReturn(List.of(officer(104, 1, null, 2, 0, 15, true)));
        doThrow(new RuntimeException("db down")).when(complaints).escalate(eq(20L), anyInt(), anyInt(), any(), any());

        EscalationService.CycleResult r = service.runCycle();

        assertEquals(1, r.failed());
        assertEquals(1, r.escalated());
        verify(complaints).escalate(eq(21L), eq(104), eq(2), any(), any());
    }

    @Test
    void escalationPrefersSupervisorOfTheComplaintWardThenLeastLoaded() {
        when(complaints.findOverdue(NOW)).thenReturn(List.of(overdue(30, 1)));
        when(officers.findActive(1, 2)).thenReturn(List.of(
                officer(1, 1, 4, 2, 0, 15, true),
                officer(2, 1, 5, 2, 9, 15, true),
                officer(3, 1, 5, 2, 2, 15, true)));

        service.runCycle();

        verify(complaints).escalate(eq(30L), eq(3), eq(2), any(), any());
    }
}
