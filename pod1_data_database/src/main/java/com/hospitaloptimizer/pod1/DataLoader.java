package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.shared.db.Database;
import com.hospitaloptimizer.shared.model.*;
import com.hospitaloptimizer.shared.util.CsvUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pod 1 -- "Data, Database & Delivery".
 * Loads the seed CSV files into the SQLite database (idempotent -- safe to
 * run every startup), and reloads data FROM the database back into plain
 * Java objects that the other pods' custom data structures then get built
 * from. This is the one place that talks JDBC; everyone else works with
 * List<Location>, List<Road>, etc.
 */
public class DataLoader {

    private final Database db;

    public DataLoader(Database db) {
        this.db = db;
    }

    /** Loads all CSV seed files into the database if the tables are currently empty. */
    public void seedDatabaseIfEmpty() {
        if (db.count("locations") == 0) {
            loadCsvIntoTable("data/locations.csv",
                    "INSERT INTO locations (locationId, name, area, type, latitude, longitude) VALUES (?,?,?,?,?,?)",
                    (ps, row) -> {
                        ps.setString(1, row[0]);
                        ps.setString(2, row[1]);
                        ps.setString(3, row[2]);
                        ps.setString(4, row[3]);
                        ps.setDouble(5, Double.parseDouble(row[4]));
                        ps.setDouble(6, Double.parseDouble(row[5]));
                    });
        }
        if (db.count("roads") == 0) {
            loadCsvIntoTable("data/roads.csv",
                    "INSERT INTO roads (roadId, fromLocationId, toLocationId, distance, travelTime, roadConditionWeight) VALUES (?,?,?,?,?,?)",
                    (ps, row) -> {
                        ps.setString(1, row[0]);
                        ps.setString(2, row[1]);
                        ps.setString(3, row[2]);
                        ps.setDouble(4, Double.parseDouble(row[3]));
                        ps.setDouble(5, Double.parseDouble(row[4]));
                        ps.setDouble(6, Double.parseDouble(row[5]));
                    });
        }
        if (db.count("resources") == 0) {
            loadCsvIntoTable("data/resources.csv",
                    "INSERT INTO resources (resourceId, type, homeLocation, capacity, availabilityStatus) VALUES (?,?,?,?,?)",
                    (ps, row) -> {
                        ps.setString(1, row[0]);
                        ps.setString(2, row[1]);
                        ps.setString(3, row[2]);
                        ps.setInt(4, Integer.parseInt(row[3]));
                        ps.setString(5, row[4]);
                    });
        }
        if (db.count("service_requests") == 0) {
            loadCsvIntoTable("data/service_requests.csv",
                    "INSERT INTO service_requests (requestId, source, destination, category, urgency, timeSubmitted, deadline, status) VALUES (?,?,?,?,?,?,?,?)",
                    (ps, row) -> {
                        ps.setString(1, row[0]);
                        ps.setString(2, row[1]);
                        ps.setString(3, row[2]);
                        ps.setString(4, row[3]);
                        ps.setInt(5, Integer.parseInt(row[4]));
                        ps.setString(6, row[5]);
                        ps.setString(7, row[6]);
                        ps.setString(8, row[7]);
                    });
        }
    }

    @FunctionalInterface
    private interface RowBinder {
        void bind(PreparedStatement ps, String[] row) throws SQLException;
    }

    private void loadCsvIntoTable(String csvResource, String insertSql, RowBinder binder) {
        List<String[]> rows = CsvUtil.read(csvResource);
        try (PreparedStatement ps = db.getConnection().prepareStatement(insertSql)) {
            for (int i = 1; i < rows.size(); i++) { // skip header row
                binder.bind(ps, rows.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed loading " + csvResource + " into DB", e);
        }
    }

    // ---- read back from DB into plain model lists ----

    public List<Location> loadLocations() {
        List<Location> out = new ArrayList<>();
        try (Statement s = db.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM locations")) {
            while (rs.next()) {
                out.add(new Location(rs.getString("locationId"), rs.getString("name"), rs.getString("area"),
                        rs.getString("type"), rs.getDouble("latitude"), rs.getDouble("longitude")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public List<Road> loadRoads() {
        List<Road> out = new ArrayList<>();
        try (Statement s = db.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM roads")) {
            while (rs.next()) {
                out.add(new Road(rs.getString("roadId"), rs.getString("fromLocationId"), rs.getString("toLocationId"),
                        rs.getDouble("distance"), rs.getDouble("travelTime"), rs.getDouble("roadConditionWeight")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public List<Resource> loadResources() {
        List<Resource> out = new ArrayList<>();
        try (Statement s = db.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM resources")) {
            while (rs.next()) {
                out.add(new Resource(rs.getString("resourceId"), rs.getString("type"), rs.getString("homeLocation"),
                        rs.getInt("capacity"), rs.getString("availabilityStatus")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public List<ServiceRequest> loadServiceRequests() {
        List<ServiceRequest> out = new ArrayList<>();
        try (Statement s = db.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM service_requests")) {
            while (rs.next()) {
                out.add(new ServiceRequest(rs.getString("requestId"), rs.getString("source"), rs.getString("destination"),
                        rs.getString("category"), rs.getInt("urgency"), rs.getString("timeSubmitted"),
                        rs.getString("deadline"), rs.getString("status")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    /** Persists an algorithm timing measurement (used by the efficiency lab). */
    public void logAlgorithmRun(String algorithmName, int inputSize, long timeNs, long memoryKb, String dateRun) {
        String sql = "INSERT INTO algorithm_runs (algorithmName, inputSize, timeNs, memoryKb, dateRun) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, algorithmName);
            ps.setInt(2, inputSize);
            ps.setLong(3, timeNs);
            ps.setLong(4, memoryKb);
            ps.setString(5, dateRun);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void logAuditEvent(String eventType, String description, String eventTime) {
        String sql = "INSERT INTO audit_events (eventType, description, eventTime) VALUES (?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, description);
            ps.setString(3, eventTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
