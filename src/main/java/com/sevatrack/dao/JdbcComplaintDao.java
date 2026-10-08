package com.sevatrack.dao;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Priority;
import com.sevatrack.model.Status;
import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.Jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class JdbcComplaintDao implements ComplaintDao {
    private static final String SELECT =
            "SELECT c.*, cat.name AS category_name, w.name AS ward_name, d.name AS department_name, "
          + "o.name AS officer_name FROM complaints c "
          + "JOIN categories cat ON cat.id = c.category_id JOIN wards w ON w.id = c.ward_id "
          + "JOIN departments d ON d.id = c.department_id LEFT JOIN officers o ON o.id = c.assigned_officer_id ";

    private final Jdbc jdbc;

    public JdbcComplaintDao(ConnectionProvider cp) {
        this.jdbc = new Jdbc(cp);
    }

    @Override
    public long insert(Complaint c) {
        return jdbc.insert(
                "INSERT INTO complaints(ticket_no, citizen_name, citizen_phone, citizen_email, title, description, "
              + "category_id, ward_id, department_id, priority, status, escalation_level, sla_due_at) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                ps -> {
                    ps.setString(1, c.getTicketNo());
                    ps.setString(2, c.getCitizenName());
                    ps.setString(3, c.getCitizenPhone());
                    ps.setString(4, c.getCitizenEmail());
                    ps.setString(5, c.getTitle());
                    ps.setString(6, c.getDescription());
                    ps.setInt(7, c.getCategoryId());
                    ps.setInt(8, c.getWardId());
                    ps.setInt(9, c.getDepartmentId());
                    ps.setString(10, c.getPriority().name());
                    ps.setString(11, c.getStatus().name());
                    ps.setInt(12, c.getEscalationLevel());
                    ps.setTimestamp(13, Jdbc.ts(c.getSlaDueAt()));
                });
    }

    @Override
    public Optional<Complaint> findById(long id) {
        return jdbc.one(SELECT + "WHERE c.id = ?", ps -> ps.setLong(1, id), JdbcComplaintDao::map);
    }

    @Override
    public Optional<Complaint> findByTicket(String ticketNo) {
        return jdbc.one(SELECT + "WHERE c.ticket_no = ?", ps -> ps.setString(1, ticketNo), JdbcComplaintDao::map);
    }

    @Override
    public List<Complaint> findByOfficer(int officerId) {
        return jdbc.list(SELECT + "WHERE c.assigned_officer_id = ? AND c.status <> 'CLOSED' "
                       + "ORDER BY FIELD(c.status,'NEW','ASSIGNED','IN_PROGRESS','RESOLVED'), c.sla_due_at",
                ps -> ps.setInt(1, officerId), JdbcComplaintDao::map);
    }

    @Override
    public List<Complaint> findOverdue(LocalDateTime now) {
        return jdbc.list(SELECT + "WHERE c.status IN ('NEW','ASSIGNED','IN_PROGRESS') AND c.sla_due_at < ? "
                       + "AND NOT (c.escalation_level = 3 AND c.sla_breached = 1) ORDER BY c.sla_due_at",
                ps -> ps.setTimestamp(1, Jdbc.ts(now)), JdbcComplaintDao::map);
    }

    @Override
    public List<Complaint> findRecent(int limit) {
        return jdbc.list(SELECT + "ORDER BY c.created_at DESC, c.id DESC LIMIT ?",
                ps -> ps.setInt(1, limit), JdbcComplaintDao::map);
    }

    @Override
    public void assign(long id, int officerId, LocalDateTime newDue, String actor, String note) {
        jdbc.call("{CALL sp_assign_complaint(?,?,?,?,?)}", ps -> {
            ps.setLong(1, id);
            ps.setInt(2, officerId);
            ps.setTimestamp(3, Jdbc.ts(newDue));
            ps.setString(4, actor);
            ps.setString(5, note);
        });
    }

    @Override
    public void updateStatus(long id, Status status, String actor, String note) {
        jdbc.call("{CALL sp_update_status(?,?,?,?)}", ps -> {
            ps.setLong(1, id);
            ps.setString(2, status.name());
            ps.setString(3, actor);
            ps.setString(4, note);
        });
    }

    @Override
    public void escalate(long id, int toOfficerId, int toLevel, LocalDateTime newDue, String reason) {
        jdbc.call("{CALL sp_escalate_complaint(?,?,?,?,?)}", ps -> {
            ps.setLong(1, id);
            ps.setInt(2, toOfficerId);
            ps.setInt(3, toLevel);
            ps.setTimestamp(4, Jdbc.ts(newDue));
            ps.setString(5, reason);
        });
    }

    @Override
    public void markBreached(long id, String note) {
        jdbc.call("{CALL sp_mark_breached(?,?)}", ps -> {
            ps.setLong(1, id);
            ps.setString(2, note);
        });
    }

    static Complaint map(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getLong("id"));
        c.setTicketNo(rs.getString("ticket_no"));
        c.setCitizenName(rs.getString("citizen_name"));
        c.setCitizenPhone(rs.getString("citizen_phone"));
        c.setCitizenEmail(rs.getString("citizen_email"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        c.setWardId(rs.getInt("ward_id"));
        c.setWardName(rs.getString("ward_name"));
        c.setDepartmentId(rs.getInt("department_id"));
        c.setDepartmentName(rs.getString("department_name"));
        c.setPriority(Priority.valueOf(rs.getString("priority")));
        c.setStatus(Status.valueOf(rs.getString("status")));
        c.setAssignedOfficerId(Jdbc.intOrNull(rs, "assigned_officer_id"));
        c.setAssignedOfficerName(rs.getString("officer_name"));
        c.setEscalationLevel(rs.getInt("escalation_level"));
        c.setSlaDueAt(Jdbc.ldt(rs, "sla_due_at"));
        c.setCreatedAt(Jdbc.ldt(rs, "created_at"));
        c.setUpdatedAt(Jdbc.ldt(rs, "updated_at"));
        c.setResolvedAt(Jdbc.ldt(rs, "resolved_at"));
        c.setSlaBreached(rs.getBoolean("sla_breached"));
        return c;
    }
}
