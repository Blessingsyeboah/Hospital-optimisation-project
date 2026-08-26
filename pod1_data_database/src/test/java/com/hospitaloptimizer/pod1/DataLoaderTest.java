package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.shared.db.Database;
import com.hospitaloptimizer.shared.model.Location;
import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.io.File;
import java.util.List;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

/**
 * Uses a throwaway SQLite file per test run so tests never collide with the
 * real hospital_optimizer.db the console app uses.
 */
public class DataLoaderTest {

    private Database freshDb() {
        String path = "test_" + System.nanoTime() + ".db";
        Database db = new Database(path);
        db.initSchema("schema.sql");
        new File(path).deleteOnExit();
        return db;
    }

    public void testSeedDatabaseIfEmpty_normalCase_loadsAllTables() {
        Database db = freshDb();
        DataLoader loader = new DataLoader(db);
        loader.seedDatabaseIfEmpty();
        assertEquals(50, db.count("locations"));
        assertEquals(100, db.count("roads"));
        assertEquals(30, db.count("resources"));
        assertEquals(300, db.count("service_requests"));
        db.close();
    }

    public void testSeedDatabaseIfEmpty_isIdempotent() {
        Database db = freshDb();
        DataLoader loader = new DataLoader(db);
        loader.seedDatabaseIfEmpty();
        loader.seedDatabaseIfEmpty(); // second call should NOT double-insert
        assertEquals(50, db.count("locations"));
        db.close();
    }

    public void testLoadLocations_roundTrip() {
        Database db = freshDb();
        DataLoader loader = new DataLoader(db);
        loader.seedDatabaseIfEmpty();
        List<Location> locations = loader.loadLocations();
        assertEquals(50, locations.size());
        assertNotNull(locations.get(0).getName());
        db.close();
    }

    public void testLoadServiceRequests_fieldsPopulated() {
        Database db = freshDb();
        DataLoader loader = new DataLoader(db);
        loader.seedDatabaseIfEmpty();
        List<ServiceRequest> requests = loader.loadServiceRequests();
        assertEquals(300, requests.size());
        ServiceRequest first = requests.get(0);
        assertTrue(first.getUrgency() >= 1 && first.getUrgency() <= 5, "urgency must be in [1,5]");
        db.close();
    }

    public void testLogAlgorithmRun_boundaryCase_persistsRow() {
        Database db = freshDb();
        DataLoader loader = new DataLoader(db);
        int before = db.count("algorithm_runs");
        loader.logAlgorithmRun("TestAlgo", 100, 12345L, 500L, "2026-01-01 00:00:00");
        assertEquals(before + 1, db.count("algorithm_runs"));
        db.close();
    }

    public static void main(String[] args) {
        run(DataLoaderTest.class);
    }
}
