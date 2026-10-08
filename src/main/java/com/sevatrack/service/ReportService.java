package com.sevatrack.service;

import com.sevatrack.dao.ReportDao;
import com.sevatrack.model.CategoryStat;
import com.sevatrack.model.DepartmentStat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReportService {
    public record Report(LocalDate from, LocalDate to, LocalDateTime generatedAt,
                         List<DepartmentStat> departments, List<CategoryStat> categories) {}

    private final ReportDao dao;
    private final Clock clock;

    public ReportService(ReportDao dao, Clock clock) {
        this.dao = dao;
        this.clock = clock;
    }

    /** Inclusive date range [from, to]. */
    public Report build(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new ValidationException(List.of("Report range is invalid: 'to' must not be before 'from'"));
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return new Report(from, to, LocalDateTime.now(clock), dao.departmentStats(start, end), dao.categoryStats(start, end));
    }

    public Report lastDays(int days) {
        LocalDate today = LocalDate.now(clock);
        return build(today.minusDays(days - 1L), today);
    }
}
