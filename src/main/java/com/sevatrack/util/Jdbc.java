package com.sevatrack.util;

import com.sevatrack.service.BusinessException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Tiny JDBC helper: every call opens/closes its own connection, so session variables never leak. */
public final class Jdbc {
    @FunctionalInterface public interface Binder { void bind(PreparedStatement ps) throws SQLException; }
    @FunctionalInterface public interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }

    public static final Binder NONE = ps -> { };

    private final ConnectionProvider cp;

    public Jdbc(ConnectionProvider cp) {
        this.cp = cp;
    }

    public <T> List<T> list(String sql, Binder b, RowMapper<T> m) {
        try (Connection c = cp.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            b.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(m.map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    public <T> Optional<T> one(String sql, Binder b, RowMapper<T> m) {
        List<T> rows = list(sql, b, m);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public long insert(String sql, Binder b) {
        try (Connection c = cp.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            b.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key returned");
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    /** CALL a stored procedure that returns no result set. */
    public void call(String sql, Binder b) {
        try (Connection c = cp.get(); CallableStatement cs = c.prepareCall(sql)) {
            b.bind(cs);
            cs.execute();
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    /** CALL a stored procedure that returns a result set. */
    public <T> List<T> callQuery(String sql, Binder b, RowMapper<T> m) {
        try (Connection c = cp.get(); CallableStatement cs = c.prepareCall(sql)) {
            b.bind(cs);
            List<T> out = new ArrayList<>();
            if (cs.execute()) {
                try (ResultSet rs = cs.getResultSet()) {
                    while (rs.next()) out.add(m.map(rs));
                }
            }
            return out;
        } catch (SQLException e) {
            throw translate(sql, e);
        }
    }

    private static RuntimeException translate(String sql, SQLException e) {
        // SQLSTATE 45000 = our SIGNALs from triggers / procedures (business rules).
        if ("45000".equals(e.getSQLState())) return new BusinessException(e.getMessage());
        return new DataAccessException("SQL failed: " + sql, e);
    }

    public static Timestamp ts(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    public static LocalDateTime ldt(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    public static Integer intOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
