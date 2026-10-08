package com.sevatrack.dao;

import com.sevatrack.model.CategoryStat;
import com.sevatrack.model.DepartmentStat;
import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.Jdbc;

import java.time.LocalDateTime;
import java.util.List;

public class JdbcReportDao implements ReportDao {
    private final Jdbc jdbc;

    public JdbcReportDao(ConnectionProvider cp) {
        this.jdbc = new Jdbc(cp);
    }

    @Override
    public List<DepartmentStat> departmentStats(LocalDateTime from, LocalDateTime to) {
        return jdbc.callQuery("{CALL sp_department_report(?,?)}", ps -> {
            ps.setTimestamp(1, Jdbc.ts(from));
            ps.setTimestamp(2, Jdbc.ts(to));
        }, rs -> {
            DepartmentStat s = new DepartmentStat();
            s.setDepartmentId(rs.getInt("department_id"));
            s.setDepartmentName(rs.getString("department_name"));
            s.setTotal(rs.getInt("total"));
            s.setOpen(rs.getInt("open_count"));
            s.setResolved(rs.getInt("resolved_count"));
            s.setEscalated(rs.getInt("escalated_count"));
            s.setBreached(rs.getInt("breached_count"));
            s.setAvgResolutionHours(rs.getDouble("avg_hours"));
            return s;
        });
    }

    @Override
    public List<CategoryStat> categoryStats(LocalDateTime from, LocalDateTime to) {
        return jdbc.list(
                "SELECT c.department_id, cat.name AS category_name, COUNT(*) AS total, "
              + "SUM(c.status IN ('NEW','ASSIGNED','IN_PROGRESS')) AS open_count "
              + "FROM complaints c JOIN categories cat ON cat.id = c.category_id "
              + "WHERE c.created_at >= ? AND c.created_at < ? "
              + "GROUP BY c.department_id, cat.id, cat.name ORDER BY c.department_id, total DESC, cat.name",
                ps -> { ps.setTimestamp(1, Jdbc.ts(from)); ps.setTimestamp(2, Jdbc.ts(to)); }, rs -> {
                    CategoryStat s = new CategoryStat();
                    s.setDepartmentId(rs.getInt("department_id"));
                    s.setCategoryName(rs.getString("category_name"));
                    s.setTotal(rs.getInt("total"));
                    s.setOpen(rs.getInt("open_count"));
                    return s;
                });
    }
}
