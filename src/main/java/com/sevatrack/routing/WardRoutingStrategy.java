package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;

import java.util.List;

/** Prefer the officer who owns the ward; otherwise floating (department-wide) officers; otherwise anyone. */
public class WardRoutingStrategy implements RoutingStrategy {
    @Override public String name() { return "ward"; }

    @Override public boolean mandatory() { return false; }

    @Override
    public List<Officer> apply(Complaint complaint, List<Officer> candidates) {
        List<Officer> local = candidates.stream()
                .filter(o -> o.getWardId() != null && o.getWardId() == complaint.getWardId()).toList();
        if (!local.isEmpty()) return local;
        return candidates.stream().filter(o -> o.getWardId() == null).toList();
    }
}
