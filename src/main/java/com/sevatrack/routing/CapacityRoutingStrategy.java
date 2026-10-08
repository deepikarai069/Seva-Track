package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;

import java.util.List;

/**
 * Hard rule: officers who already hold max_load open complaints are skipped.
 * Runs before the ward preference so a full ward owner makes the complaint overflow to a colleague
 * instead of leaving it unassigned.
 */
public class CapacityRoutingStrategy implements RoutingStrategy {
    @Override public String name() { return "capacity"; }

    @Override public boolean mandatory() { return true; }

    @Override
    public List<Officer> apply(Complaint complaint, List<Officer> candidates) {
        return candidates.stream().filter(o -> o.getOpenLoad() < o.getMaxLoad()).toList();
    }
}
