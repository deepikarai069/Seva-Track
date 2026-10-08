package com.sevatrack.model;

public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL;

    public boolean atLeast(Priority other) {
        return compareTo(other) >= 0;
    }

    public static Priority parse(String value, Priority fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
