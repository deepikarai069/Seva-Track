package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;
import com.sevatrack.model.Priority;
import com.sevatrack.model.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.sevatrack.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

class RoutingEngineTest {
    private final RoutingEngine assign = RoutingEngine.forAssignment();
    private final RoutingEngine escalate = RoutingEngine.forEscalation();

    private Complaint c(int dept, int ward, Priority p) {
        return complaint(1, dept, ward, p, Status.NEW, 1, NOW);
    }

    @Test
    void categoryIsAHardRule_officersOfOtherDepartmentsAreNeverPicked() {
        List<Officer> others = List.of(officer(1, 9, 3, 1, 0, 5, false), officer(2, 8, 3, 1, 0, 5, false));
        assertTrue(assign.pick(c(2, 3, Priority.MEDIUM), others).isEmpty());
    }

    @Test
    void wardOwnerBeatsLessLoadedOfficerFromAnotherWard() {
        Officer local = officer(1, 2, 3, 1, 4, 8, false);
        Officer other = officer(2, 2, 4, 1, 0, 8, false);
        assertEquals(1, assign.pick(c(2, 3, Priority.MEDIUM), List.of(other, local)).orElseThrow().getId());
    }

    @Test
    void floatingOfficerIsUsedWhenNobodyOwnsTheWard() {
        Officer elsewhere = officer(1, 2, 4, 1, 0, 8, false);
        Officer floating = officer(2, 2, null, 1, 5, 8, false);
        assertEquals(2, assign.pick(c(2, 3, Priority.LOW), List.of(elsewhere, floating)).orElseThrow().getId());
    }

    @Test
    void criticalComplaintGoesToEmergencyOfficerEvenIfBusier() {
        Officer routine = officer(1, 2, 3, 1, 0, 8, false);
        Officer emergency = officer(2, 2, 3, 1, 5, 8, true);
        assertEquals(2, assign.pick(c(2, 3, Priority.CRITICAL), List.of(routine, emergency)).orElseThrow().getId());
    }

    @Test
    void routineComplaintKeepsEmergencyOfficersFree() {
        Officer routine = officer(1, 2, 3, 1, 6, 8, false);
        Officer emergency = officer(2, 2, 3, 1, 0, 8, true);
        assertEquals(1, assign.pick(c(2, 3, Priority.MEDIUM), List.of(routine, emergency)).orElseThrow().getId());
    }

    @Test
    void priorityRuleIsOnlyAPreference_fallsBackWhenNoEmergencyOfficerExists() {
        Officer routine = officer(1, 2, 3, 1, 0, 8, false);
        assertEquals(1, assign.pick(c(2, 3, Priority.CRITICAL), List.of(routine)).orElseThrow().getId());
    }

    @Test
    void leastLoadedByRatioWinsAmongEquals() {
        Officer a = officer(1, 2, 3, 1, 3, 6, false);   // 50%
        Officer b = officer(2, 2, 3, 1, 4, 10, false);  // 40%
        Officer d = officer(3, 2, 3, 1, 5, 6, false);   // 83%
        assertEquals(2, assign.pick(c(2, 3, Priority.LOW), List.of(a, b, d)).orElseThrow().getId());
    }

    @Test
    void assignmentSkipsOfficersAtCapacity_escalationDoesNot() {
        Officer full = officer(1, 2, 3, 1, 8, 8, false);
        assertTrue(assign.pick(c(2, 3, Priority.LOW), List.of(full)).isEmpty());
        Optional<Officer> forEscalation = escalate.pick(c(2, 3, Priority.LOW), List.of(full));
        assertEquals(1, forEscalation.orElseThrow().getId());
    }

    @Test
    void fullWardOwnerOverflowsToAnotherWardInsteadOfLeavingComplaintUnassigned() {
        Officer fullOwner = officer(1, 2, 3, 1, 8, 8, false);
        Officer otherWard = officer(2, 2, 4, 1, 1, 8, false);
        assertEquals(2, assign.pick(c(2, 3, Priority.LOW), List.of(fullOwner, otherWard)).orElseThrow().getId());
    }

    @Test
    void emptyCandidateListGivesEmptyResult() {
        assertTrue(assign.pick(c(2, 3, Priority.LOW), List.of()).isEmpty());
    }
}
