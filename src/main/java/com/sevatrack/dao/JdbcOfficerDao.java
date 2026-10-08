package com.sevatrack.dao;

import com.sevatrack.model.Officer;
import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.Jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcOfficerDao implements OfficerDao {
    private static final String SELECT =
            "SELECT o.*, d.name AS department_name, w.name AS ward_name, fn_open_load(o.id) AS open_load "
          + "FROM officers o JOIN departments d ON d.id = o.department_id LEFT JOIN wards w ON w.id = o.ward_id ";

    private final Jdbc jdbc;

    public JdbcOfficerDao(ConnectionProvider cp) {
        this.jdbc = new Jdbc(cp);
    }

    @Override
    public List<Officer> findActive(int departmentId, int level) {
        return jdbc.list(SELECT + "WHERE o.active = 1 AND o.department_id = ? AND o.level = ? ORDER BY o.id",
                ps -> { ps.setInt(1, departmentId); ps.setInt(2, level); }, JdbcOfficerDao::map);
    }

    @Override
    public List<Officer> findAllActive() {
        return jdbc.list(SELECT + "WHERE o.active = 1 ORDER BY o.department_id, o.level, o.id", Jdbc.NONE, JdbcOfficerDao::map);
    }

    @Override
    public Optional<Officer> findById(int id) {
        return jdbc.one(SELECT + "WHERE o.id = ?", ps -> ps.setInt(1, id), JdbcOfficerDao::map);
    }

    static Officer map(ResultSet rs) throws SQLException {
        Officer o = new Officer();
        o.setId(rs.getInt("id"));
        o.setName(rs.getString("name"));
        o.setEmail(rs.getString("email"));
        o.setDepartmentId(rs.getInt("department_id"));
        o.setDepartmentName(rs.getString("department_name"));
        o.setWardId(Jdbc.intOrNull(rs, "ward_id"));
        o.setWardName(rs.getString("ward_name"));
        o.setLevel(rs.getInt("level"));
        o.setMaxLoad(rs.getInt("max_load"));
        o.setOpenLoad(rs.getInt("open_load"));
        o.setEmergency(rs.getBoolean("emergency"));
        return o;
    }
}
