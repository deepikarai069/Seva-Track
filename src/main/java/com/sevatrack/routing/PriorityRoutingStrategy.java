package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;
import com.sevatrack.model.Priority;

import java.util.List;

/**
 * HIGH / CRITICAL complaints go to emergency-certified officers. Routine complaints prefer
 * non-emergency officers so that emergency capacity is kept free.
 */
public class PriorityRoutingStrategy implements RoutingStrategy {
    @Override public String name() { return "priority"; }

    @Override public boolean mandatory() { return false; }

    @Override
    public List<Officer> apply(Complaint complaint, List<Officer> candidates) {
        boolean urgent = complaint.getPriority() != null && complaint.getPriority().atLeast(Priority.HIGH);
        return candidates.stream().filter(o -> o.isEmergency() == urgent).toList();
    }
}
