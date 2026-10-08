package com.sevatrack.dao;

import com.sevatrack.model.Category;
import com.sevatrack.model.Department;
import com.sevatrack.model.Priority;
import com.sevatrack.model.Ward;
import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.Jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcReferenceDao implements ReferenceDao {
    private final Jdbc jdbc;

    public JdbcReferenceDao(ConnectionProvider cp) {
        this.jdbc = new Jdbc(cp);
    }

    @Override
    public List<Department> departments() {
        return jdbc.list("SELECT id, name, code FROM departments ORDER BY id", Jdbc.NONE, rs -> {
            Department d = new Department();
            d.setId(rs.getInt("id"));
            d.setName(rs.getString("name"));
            d.setCode(rs.getString("code"));
            return d;
        });
    }

    @Override
    public List<Category> categories() {
        return jdbc.list("SELECT id, name, department_id, default_priority FROM categories ORDER BY department_id, name",
                Jdbc.NONE, JdbcReferenceDao::category);
    }

    @Override
    public List<Ward> wards() {
        return jdbc.list("SELECT id, name FROM wards ORDER BY name", Jdbc.NONE, JdbcReferenceDao::ward);
    }

    @Override
    public Optional<Category> findCategory(int id) {
        return jdbc.one("SELECT id, name, department_id, default_priority FROM categories WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcReferenceDao::category);
    }

    @Override
    public Optional<Ward> findWard(int id) {
        return jdbc.one("SELECT id, name FROM wards WHERE id = ?", ps -> ps.setInt(1, id), JdbcReferenceDao::ward);
    }

    private static Category category(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDepartmentId(rs.getInt("department_id"));
        c.setDefaultPriority(Priority.valueOf(rs.getString("default_priority")));
        return c;
    }

    private static Ward ward(ResultSet rs) throws SQLException {
        Ward w = new Ward();
        w.setId(rs.getInt("id"));
        w.setName(rs.getString("name"));
        return w;
    }
}
