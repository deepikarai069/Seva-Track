package com.sevatrack.routing;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Officer;

import java.util.List;

/** Hard rule: the officer must belong to the department that owns the complaint category. */
public class CategoryRoutingStrategy implements RoutingStrategy {
    @Override public String name() { return "category"; }

    @Override public boolean mandatory() { return true; }

    @Override
    public List<Officer> apply(Complaint complaint, List<Officer> candidates) {
        return candidates.stream().filter(o -> o.getDepartmentId() == complaint.getDepartmentId()).toList();
    }
}
