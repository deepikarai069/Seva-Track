package com.sevatrack.dao;

import com.sevatrack.model.Category;
import com.sevatrack.model.Department;
import com.sevatrack.model.Ward;

import java.util.List;
import java.util.Optional;

public interface ReferenceDao {
    List<Department> departments();

    List<Category> categories();

    List<Ward> wards();

    Optional<Category> findCategory(int id);

    Optional<Ward> findWard(int id);
}
