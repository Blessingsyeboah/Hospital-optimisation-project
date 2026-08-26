-- Hospital Smart Service Operations Optimizer
-- SQLite schema. Run automatically by Database.initSchema() on startup,
-- and also kept here as a standalone script per submission requirements.

CREATE TABLE IF NOT EXISTS locations (
    locationId TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    area TEXT NOT NULL,
    type TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS roads (
    roadId TEXT PRIMARY KEY,
    fromLocationId TEXT NOT NULL,
    toLocationId TEXT NOT NULL,
    distance REAL NOT NULL,
    travelTime REAL NOT NULL,
    roadConditionWeight REAL NOT NULL,
    FOREIGN KEY (fromLocationId) REFERENCES locations(locationId),
    FOREIGN KEY (toLocationId) REFERENCES locations(locationId)
);

CREATE TABLE IF NOT EXISTS service_requests (
    requestId TEXT PRIMARY KEY,
    source TEXT NOT NULL,
    destination TEXT NOT NULL,
    category TEXT NOT NULL,
    urgency INTEGER NOT NULL,
    timeSubmitted TEXT NOT NULL,
    deadline TEXT NOT NULL,
    status TEXT NOT NULL,
    FOREIGN KEY (source) REFERENCES locations(locationId),
    FOREIGN KEY (destination) REFERENCES locations(locationId)
);

CREATE TABLE IF NOT EXISTS resources (
    resourceId TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    homeLocation TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    availabilityStatus TEXT NOT NULL,
    FOREIGN KEY (homeLocation) REFERENCES locations(locationId)
);

CREATE TABLE IF NOT EXISTS algorithm_runs (
    runId INTEGER PRIMARY KEY AUTOINCREMENT,
    algorithmName TEXT NOT NULL,
    inputSize INTEGER NOT NULL,
    timeNs INTEGER NOT NULL,
    memoryKb INTEGER NOT NULL,
    dateRun TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_events (
    eventId INTEGER PRIMARY KEY AUTOINCREMENT,
    eventType TEXT NOT NULL,
    description TEXT NOT NULL,
    eventTime TEXT NOT NULL
);
