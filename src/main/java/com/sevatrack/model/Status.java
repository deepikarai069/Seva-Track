package com.sevatrack.model;

/** Complaint lifecycle. The same transition table is enforced in DB by trg_complaints_bu. */
public enum Status {
    NEW, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED;

    public boolean isOpen() {
        return this == NEW || this == ASSIGNED || this == IN_PROGRESS;
    }

    public boolean canTransitionTo(Status target) {
        if (this == target) return true;
        return switch (this) {
            case NEW -> target == ASSIGNED;
            case ASSIGNED -> target == IN_PROGRESS || target == RESOLVED;
            case IN_PROGRESS -> target == RESOLVED;
            case RESOLVED -> target == CLOSED || target == IN_PROGRESS; // IN_PROGRESS = reopen
            case CLOSED -> false;
        };
    }
}
