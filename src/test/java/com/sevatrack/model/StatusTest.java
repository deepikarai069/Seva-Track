package com.sevatrack.model;

import org.junit.jupiter.api.Test;

import static com.sevatrack.model.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class StatusTest {
    @Test
    void happyPathIsAllowed() {
        assertTrue(NEW.canTransitionTo(ASSIGNED));
        assertTrue(ASSIGNED.canTransitionTo(IN_PROGRESS));
        assertTrue(IN_PROGRESS.canTransitionTo(RESOLVED));
        assertTrue(RESOLVED.canTransitionTo(CLOSED));
    }

    @Test
    void resolvedComplaintCanBeReopened() {
        assertTrue(RESOLVED.canTransitionTo(IN_PROGRESS));
    }

    @Test
    void skippingStepsAndLeavingClosedIsRejected() {
        assertFalse(NEW.canTransitionTo(IN_PROGRESS));
        assertFalse(NEW.canTransitionTo(RESOLVED));
        assertFalse(IN_PROGRESS.canTransitionTo(CLOSED));
        for (Status s : values()) {
            if (s != CLOSED) assertFalse(CLOSED.canTransitionTo(s), "CLOSED -> " + s);
        }
    }

    @Test
    void onlyThreeStatusesAreOpen() {
        assertTrue(NEW.isOpen() && ASSIGNED.isOpen() && IN_PROGRESS.isOpen());
        assertFalse(RESOLVED.isOpen() || CLOSED.isOpen());
    }
}
