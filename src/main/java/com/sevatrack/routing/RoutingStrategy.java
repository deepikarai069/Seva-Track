package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;

import java.util.List;

/**
 * One rule in the routing pipeline. A strategy narrows and/or re-orders the candidate officers.
 * Mandatory strategies are hard constraints (an empty result means "nobody can take this");
 * non-mandatory ones are preferences (an empty result means "rule not applicable, keep the previous list").
 */
public interface RoutingStrategy {
    String name();

    boolean mandatory();

    List<Officer> apply(Complaint complaint, List<Officer> candidates);
}
