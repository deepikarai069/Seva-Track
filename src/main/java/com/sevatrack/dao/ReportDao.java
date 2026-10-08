package com.sevatrack.dao;

import com.sevatrack.model.CategoryStat;
import com.sevatrack.model.DepartmentStat;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportDao {
    /** Backed by stored procedure sp_department_report. [from, to) on created_at. */
    List<DepartmentStat> departmentStats(LocalDateTime from, LocalDateTime to);

    List<CategoryStat> categoryStats(LocalDateTime from, LocalDateTime to);
}
