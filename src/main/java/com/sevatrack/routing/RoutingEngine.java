package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;

import java.util.List;
import java.util.Optional;

/** Runs the strategies in order and returns the best remaining officer. */
public class RoutingEngine {
    private final List<RoutingStrategy> strategies;

    public RoutingEngine(List<RoutingStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    /** category (hard) -> capacity (hard) -> ward -> priority -> workload ordering. */
    public static RoutingEngine forAssignment() {
        return new RoutingEngine(List.of(new CategoryRoutingStrategy(), new CapacityRoutingStrategy(),
                new WardRoutingStrategy(), new PriorityRoutingStrategy(), new WorkloadRoutingStrategy()));
    }

    /** Supervisors / heads take escalations regardless of capacity: a stuck complaint must move. */
    public static RoutingEngine forEscalation() {
        return new RoutingEngine(List.of(new CategoryRoutingStrategy(), new WardRoutingStrategy(),
                new WorkloadRoutingStrategy()));
    }

    public Optional<Officer> pick(Complaint complaint, List<Officer> candidates) {
        List<Officer> current = candidates;
        for (RoutingStrategy s : strategies) {
            List<Officer> next = s.apply(complaint, current);
            if (next.isEmpty()) {
                if (s.mandatory()) return Optional.empty();
                continue; // preference not applicable, keep previous candidates
            }
            current = next;
        }
        return current.stream().findFirst();
    }
}
