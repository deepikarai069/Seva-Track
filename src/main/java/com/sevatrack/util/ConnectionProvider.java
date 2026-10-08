package com.sevatrack.util;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {
    Connection get() throws SQLException;

    /** Reads DB_URL / DB_USER / DB_PASSWORD from the environment (or system properties). */
    static ConnectionProvider fromEnv() {
        String url = env("DB_URL", "jdbc:mariadb://localhost:3306/sevatrack");
        String user = env("DB_USER", "sevatrack");
        String pass = env("DB_PASSWORD", "sevatrack");
        try {
            // Needed inside Tomcat: DriverManager's ServiceLoader does not see webapp classloader drivers.
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MariaDB JDBC driver not on classpath", e);
        }
        return () -> java.sql.DriverManager.getConnection(url, user, pass);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) v = System.getProperty(key);
        return v == null || v.isBlank() ? def : v;
    }
}
