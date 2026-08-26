package com.hospitaloptimizer.shared.db;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

/**
 * Thin JDBC wrapper around a SQLite database file. This is the ONLY class
 * that should hold a live java.sql.Connection; every other module works
 * through the custom data structures (see pod2-pod5) once data is loaded.
 *
 * Uses the vendored sqlite-jdbc driver in /lib (no Maven/network dependency
 * required to build or run this project).
 */
public class Database {

    private final Connection connection;

    public Database(String dbFilePath) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not open database at " + dbFilePath, e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /** Executes schema.sql (idempotent CREATE TABLE IF NOT EXISTS statements). */
    public void initSchema(String schemaResourcePath) {
        try {
            String sql = readResource(schemaResourcePath);
            try (Statement stmt = connection.createStatement()) {
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise schema", e);
        }
    }

    private String readResource(String path) throws IOException {
        // Try classpath resource first, then filesystem fallback (useful when
        // running straight from source without a packaged jar).
        InputStream in = Database.class.getClassLoader().getResourceAsStream(path);
        if (in != null) {
            return new String(in.readAllBytes());
        }
        File f = new File(path);
        if (f.exists()) {
            return Files.readString(f.toPath());
        }
        throw new FileNotFoundException("schema resource not found: " + path);
    }

    public int count(String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {
        }
    }
}
