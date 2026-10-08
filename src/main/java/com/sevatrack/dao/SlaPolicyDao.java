package com.sevatrack.dao;

import java.util.Map;

public interface SlaPolicyDao {
    /** Keys look like "HIGH:2" (priority:level), values are allowed hours. */
    Map<String, Integer> loadAll();
}
