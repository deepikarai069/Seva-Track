package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;

import java.util.Comparator;
import java.util.List;

/** Orders candidates by load ratio (open / max), then open count, then id. The first one wins. */
public class WorkloadRoutingStrategy implements RoutingStrategy {
    @Override public String name() { return "workload"; }

    @Override public boolean mandatory() { return false; }

    @Override
    public List<Officer> apply(Complaint complaint, List<Officer> candidates) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(Officer::getLoadRatio)
                        .thenComparingInt(Officer::getOpenLoad)
                        .thenComparingInt(Officer::getId))
                .toList();
    }
}
