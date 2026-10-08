package com.sevatrack.service;

import com.sevatrack.dao.*;
import com.sevatrack.model.*;
import com.sevatrack.routing.RoutingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sevatrack.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ComplaintServiceTest {
    private ComplaintDao complaints;
    private ReferenceDao reference;
    private OfficerDao officers;
    private HistoryDao history;
    private SlaPolicyDao slaDao;
    private ComplaintService service;

    @BeforeEach
    void setUp() {
        complaints = mock(ComplaintDao.class);
        reference = mock(ReferenceDao.class);
        officers = mock(OfficerDao.class);
        history = mock(HistoryDao.class);
        slaDao = mock(SlaPolicyDao.class);
        when(slaDao.loadAll()).thenReturn(Map.of("MEDIUM:1", 72, "CRITICAL:1", 4));
        service = new ComplaintService(complaints, reference, officers, history, new SlaPolicy(slaDao),
                RoutingEngine.forAssignment(), fixedClock());

        Category pothole = new Category();
        pothole.setId(4); pothole.setName("Pothole"); pothole.setDepartmentId(2); pothole.setDefaultPriority(Priority.MEDIUM);
        when(reference.findCategory(4)).thenReturn(Optional.of(pothole));
        when(reference.findCategory(99)).thenReturn(Optional.empty());
        when(reference.findWard(3)).thenReturn(Optional.of(new Ward()));
        when(complaints.findByTicket(anyString())).thenReturn(Optional.empty());
        when(complaints.insert(any())).thenReturn(42L);
    }

    private ComplaintForm validForm() {
        ComplaintForm f = new ComplaintForm();
        f.setCitizenName("Asha Singh");
        f.setCitizenPhone("+91 98765 43210");
        f.setTitle("Deep pothole near Chowk crossing");
        f.setDescription("The pothole has been there for two weeks and is growing.");
        f.setCategoryId(4);
        f.setWardId(3);
        return f;
    }

    @Test
    void registerRoutesToWardOfficerAndSetsSlaFromPriorityDefault() {
        Officer ward3 = officer(201, 2, 3, 1, 2, 8, false);
        Officer ward4 = officer(202, 2, 4, 1, 0, 8, false);
        when(officers.findActive(2, 1)).thenReturn(List.of(ward4, ward3));
        when(complaints.findById(42L)).thenReturn(Optional.of(new Complaint()));

        service.register(validForm());

        ArgumentCaptor<Complaint> saved = ArgumentCaptor.forClass(Complaint.class);
        verify(complaints).insert(saved.capture());
        Complaint c = saved.getValue();
        assertEquals(Priority.MEDIUM, c.getPriority());          // category default
        assertEquals(2, c.getDepartmentId());                     // department comes from category
        assertEquals(Status.NEW, c.getStatus());
        assertEquals(NOW.plus(Duration.ofHours(72)), c.getSlaDueAt());
        assertTrue(c.getTicketNo().matches("ST-261007-[A-Z0-9]{5}"), c.getTicketNo());
        verify(complaints).assign(eq(42L), eq(201), eq(NOW.plusHours(72)), eq("SYSTEM"), anyString());
    }

    @Test
    void explicitPriorityOverridesCategoryDefaultAndShortensSla() {
        when(officers.findActive(2, 1)).thenReturn(List.of());
        when(complaints.findById(42L)).thenReturn(Optional.of(new Complaint()));
        ComplaintForm f = validForm();
        f.setPriority("critical");

        service.register(f);

        ArgumentCaptor<Complaint> saved = ArgumentCaptor.forClass(Complaint.class);
        verify(complaints).insert(saved.capture());
        assertEquals(Priority.CRITICAL, saved.getValue().getPriority());
        assertEquals(NOW.plusHours(4), saved.getValue().getSlaDueAt());
    }

    @Test
    void complaintStaysUnassignedWhenNobodyCanTakeIt() {
        when(officers.findActive(2, 1)).thenReturn(List.of(officer(201, 2, 3, 1, 8, 8, false))); // at capacity
        when(complaints.findById(42L)).thenReturn(Optional.of(new Complaint()));

        service.register(validForm());

        verify(complaints, never()).assign(anyLong(), anyInt(), any(), any(), any());
    }

    @Test
    void invalidInputIsRejectedWithAllErrorsAndNothingIsStored() {
        ComplaintForm f = new ComplaintForm();
        f.setCitizenPhone("abc");
        f.setCategoryId(99);
        ValidationException e = assertThrows(ValidationException.class, () -> service.register(f));
        assertTrue(e.getErrors().size() >= 5, e.getErrors().toString());
        verify(complaints, never()).insert(any());
    }

    @Test
    void trackIsCaseInsensitiveAndReportsMissingTickets() {
        Complaint c = new Complaint();
        when(complaints.findByTicket("ST-1")).thenReturn(Optional.of(c));
        assertSame(c, service.track("  st-1 "));
        assertThrows(NotFoundException.class, () -> service.track("ST-NOPE"));
        assertThrows(ValidationException.class, () -> service.track(" "));
    }

    // ---------------------------------------------------------------- officer updates

    private Complaint assigned(Status s, int officerId) {
        Complaint c = complaint(7, 2, 3, Priority.MEDIUM, s, 1, NOW.plusHours(5));
        c.setAssignedOfficerId(officerId);
        return c;
    }

    private void officerExists(int id) {
        Officer o = officer(id, 2, 3, 1, 0, 5, false);
        when(officers.findById(id)).thenReturn(Optional.of(o));
    }

    @Test
    void officerCanMoveOwnComplaintForward() {
        when(complaints.findById(7L)).thenReturn(Optional.of(assigned(Status.ASSIGNED, 201)));
        officerExists(201);
        service.updateStatus(7, 201, Status.IN_PROGRESS, "  ");
        verify(complaints).updateStatus(7L, Status.IN_PROGRESS, "OFFICER:Officer 201", null);
    }

    @Test
    void officerCannotTouchSomeoneElsesComplaint() {
        when(complaints.findById(7L)).thenReturn(Optional.of(assigned(Status.ASSIGNED, 201)));
        officerExists(202);
        assertThrows(BusinessException.class, () -> service.updateStatus(7, 202, Status.IN_PROGRESS, null));
        verify(complaints, never()).updateStatus(anyLong(), any(), any(), any());
    }

    @Test
    void illegalTransitionIsRejectedBeforeReachingTheDatabase() {
        when(complaints.findById(7L)).thenReturn(Optional.of(assigned(Status.NEW, 201)));
        officerExists(201);
        assertThrows(BusinessException.class, () -> service.updateStatus(7, 201, Status.RESOLVED, "done"));
        verify(complaints, never()).updateStatus(anyLong(), any(), any(), any());
    }

    @Test
    void resolvingRequiresANote() {
        when(complaints.findById(7L)).thenReturn(Optional.of(assigned(Status.IN_PROGRESS, 201)));
        officerExists(201);
        assertThrows(BusinessException.class, () -> service.updateStatus(7, 201, Status.RESOLVED, ""));
        service.updateStatus(7, 201, Status.RESOLVED, "Filled and compacted");
        verify(complaints).updateStatus(7L, Status.RESOLVED, "OFFICER:Officer 201", "Filled and compacted");
    }

    @Test
    void unknownComplaintIsReported() {
        when(complaints.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.updateStatus(1, 201, Status.IN_PROGRESS, null));
    }
}
