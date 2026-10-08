package com.sevatrack.dao;

import com.sevatrack.model.Officer;

import java.util.List;
import java.util.Optional;

public interface OfficerDao {
    /** Active officers of a department at a level, each with their current open workload. */
    List<Officer> findActive(int departmentId, int level);

    List<Officer> findAllActive();

    Optional<Officer> findById(int id);
}
