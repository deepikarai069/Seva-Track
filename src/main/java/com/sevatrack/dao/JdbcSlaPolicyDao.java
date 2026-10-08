package com.sevatrack.dao;

import com.sevatrack.util.ConnectionProvider;
import com.sevatrack.util.Jdbc;

import java.util.HashMap;
import java.util.Map;

public class JdbcSlaPolicyDao implements SlaPolicyDao {
    private final Jdbc jdbc;

    public JdbcSlaPolicyDao(ConnectionProvider cp) {
        this.jdbc = new Jdbc(cp);
    }

    @Override
    public Map<String, Integer> loadAll() {
        Map<String, Integer> m = new HashMap<>();
        jdbc.list("SELECT priority, level, hours FROM sla_policy", Jdbc.NONE, rs -> {
            m.put(rs.getString("priority") + ":" + rs.getInt("level"), rs.getInt("hours"));
            return null;
        });
        return m;
    }
}
