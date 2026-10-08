package com.sevatrack.dao;

import com.sevatrack.model.EscalationRecord;
import com.sevatrack.model.Status;
import com.sevatrack.model.StatusHistory;
import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.Jdbc;

import java.util.List;

public class JdbcHistoryDao implements HistoryDao {
    private final Jdbc jdbc;

    public JdbcHistoryDao(ConnectionProvider cp) {
        this.jdbc = new Jdbc(cp);
    }

    @Override
    public List<StatusHistory> history(long complaintId) {
        return jdbc.list(
                "SELECT h.*, olo.name AS old_officer, nwo.name AS new_officer FROM status_history h "
              + "LEFT JOIN officers olo ON olo.id = h.old_officer_id LEFT JOIN officers nwo ON nwo.id = h.new_officer_id "
              + "WHERE h.complaint_id = ? ORDER BY h.changed_at, h.id",
                ps -> ps.setLong(1, complaintId), rs -> {
                    StatusHistory h = new StatusHistory();
                    h.setId(rs.getLong("id"));
                    h.setComplaintId(rs.getLong("complaint_id"));
                    String old = rs.getString("old_status");
                    h.setOldStatus(old == null ? null : Status.valueOf(old));
                    h.setNewStatus(Status.valueOf(rs.getString("new_status")));
                    h.setOldOfficerName(rs.getString("old_officer"));
                    h.setNewOfficerName(rs.getString("new_officer"));
                    h.setOldLevel(Jdbc.intOrNull(rs, "old_level"));
                    h.setNewLevel(rs.getInt("new_level"));
                    h.setActor(rs.getString("actor"));
                    h.setNote(rs.getString("note"));
                    h.setChangedAt(Jdbc.ldt(rs, "changed_at"));
                    return h;
                });
    }

    @Override
    public List<EscalationRecord> escalations(long complaintId) {
        return jdbc.list(
                "SELECT e.*, fo.name AS from_officer, t.name AS to_officer FROM escalations e "
              + "LEFT JOIN officers fo ON fo.id = e.from_officer_id JOIN officers t ON t.id = e.to_officer_id "
              + "WHERE e.complaint_id = ? ORDER BY e.escalated_at, e.id",
                ps -> ps.setLong(1, complaintId), rs -> {
                    EscalationRecord e = new EscalationRecord();
                    e.setId(rs.getLong("id"));
                    e.setComplaintId(rs.getLong("complaint_id"));
                    e.setFromLevel(rs.getInt("from_level"));
                    e.setToLevel(rs.getInt("to_level"));
                    e.setFromOfficerName(rs.getString("from_officer"));
                    e.setToOfficerName(rs.getString("to_officer"));
                    e.setReason(rs.getString("reason"));
                    e.setEscalatedAt(Jdbc.ldt(rs, "escalated_at"));
                    return e;
                });
    }
}
