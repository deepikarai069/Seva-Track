package com.sevatrack.service;

import com.sevatrack.dao.SlaPolicyDao;
import com.sevatrack.model.Priority;

import java.time.Duration;
import java.util.Map;

/** SLA window for a (priority, escalation level) pair. Loaded lazily from sla_policy and cached. */
public class SlaPolicy {
    private final SlaPolicyDao dao;
    private volatile Map<String, Integer> table;

    public SlaPolicy(SlaPolicyDao dao) {
        this.dao = dao;
    }

    public Duration windowFor(Priority priority, int level) {
        Map<String, Integer> t = table;
        if (t == null) {
            t = dao.loadAll();
            table = t;
        }
        Integer hours = t.get(priority.name() + ":" + level);
        return Duration.ofHours(hours != null ? hours : fallbackHours(priority));
    }

    public void reload() {
        table = null;
    }

    private static int fallbackHours(Priority p) {
        return switch (p) {
            case CRITICAL -> 4;
            case HIGH -> 24;
            case MEDIUM -> 72;
            case LOW -> 120;
        };
    }
}
