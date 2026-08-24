-- =====================================================================
-- GH-SHOC: Ghana Smart Hospital Operations Optimizer
-- DCIT 204/308 Joint DSA Project — Database Schema
-- Context: Hospital / clinic operations (departments, patients, urgency,
--          pharmacy requests, dispatch routes)
-- Column names and ID formats match the official course CSV templates
-- (locations_template.csv, roads_template.csv, resources_template.csv,
-- service_requests_template.csv) — string IDs like 'L001', 'R001', 'Q001'.
-- Engine target: SQLite (portable, swap types trivially for MySQL/Postgres)
-- =====================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------
-- 1. locations  (nodes in the hospital's local service network)
-- ---------------------------------------------------------------------
CREATE TABLE locations (
    location_id     TEXT PRIMARY KEY,          -- e.g. 'L001'
    name            TEXT    NOT NULL,          -- e.g. 'Emergency Ward', 'Pharmacy Block A'
    area            TEXT    NOT NULL,          -- e.g. 'Korle-Bu Main Campus'
    location_type   TEXT    NOT NULL,          -- 'WARD','PHARMACY','LAB','ADMIN','STORE','GATE'
    x_coord         REAL,                      -- local coordinate (or synthetic campus grid)
    y_coord         REAL
);

-- ---------------------------------------------------------------------
-- 2. roads  (weighted edges between locations — hospital corridors/routes)
-- ---------------------------------------------------------------------
CREATE TABLE roads (
    road_id             TEXT PRIMARY KEY,      -- e.g. 'R001'
    from_location_id    TEXT NOT NULL REFERENCES locations(location_id),
    to_location_id      TEXT NOT NULL REFERENCES locations(location_id),
    distance_km         REAL NOT NULL,
    travel_time_min     REAL NOT NULL,
    condition_weight    REAL NOT NULL DEFAULT 1.0  -- congestion/obstruction multiplier
);

-- ---------------------------------------------------------------------
-- 3. service_requests  (jobs to be queued, prioritised, searched, sorted)
-- ---------------------------------------------------------------------
CREATE TABLE service_requests (
    request_id              TEXT PRIMARY KEY,     -- e.g. 'Q001'
    source_location_id      TEXT NOT NULL REFERENCES locations(location_id),
    destination_location_id TEXT NOT NULL REFERENCES locations(location_id),
    category                TEXT NOT NULL,        -- 'Medical','Document','Package','Equipment','Lab'
    urgency                 INTEGER NOT NULL CHECK (urgency BETWEEN 1 AND 5), -- 5 = most urgent
    time_submitted          TEXT NOT NULL,         -- ISO-8601
    deadline                TEXT,                  -- ISO-8601, nullable
    status                  TEXT NOT NULL DEFAULT 'NEW'  -- NEW/IN_PROGRESS/COMPLETED/CANCELLED
);

-- ---------------------------------------------------------------------
-- 4. resources  (porters, ambulances, nurses, trolleys — assignable assets)
-- ---------------------------------------------------------------------
CREATE TABLE resources (
    resource_id          TEXT PRIMARY KEY,     -- e.g. 'V001', 'R001' (prefix per type)
    resource_type        TEXT NOT NULL,        -- 'Porter','Ambulance','Nurse','Trolley'
    home_location_id     TEXT NOT NULL REFERENCES locations(location_id),
    capacity             INTEGER NOT NULL DEFAULT 1,
    availability_status  TEXT NOT NULL DEFAULT 'AVAILABLE' -- AVAILABLE/BUSY/OFFLINE
);

-- ---------------------------------------------------------------------
-- 5. algorithm_runs  (empirical performance measurements)
-- ---------------------------------------------------------------------
CREATE TABLE algorithm_runs (
    runId           INTEGER PRIMARY KEY AUTOINCREMENT,
    algorithmName   TEXT    NOT NULL,          -- e.g. 'MergeSort','Dijkstra','HashPut'
    inputSize       INTEGER NOT NULL,
    timeNs          INTEGER NOT NULL,
    memoryKb        INTEGER,
    dateRun         TEXT    NOT NULL           -- ISO-8601
);

-- ---------------------------------------------------------------------
-- 6. audit_events  (stack-based undo/audit trail of system events)
-- ---------------------------------------------------------------------
CREATE TABLE audit_events (
    eventId         INTEGER PRIMARY KEY AUTOINCREMENT,
    eventType       TEXT    NOT NULL,          -- 'ASSIGN','UNDO_ASSIGN','STATUS_CHANGE','DISPATCH'
    referenceTable  TEXT    NOT NULL,          -- which table the event relates to
    referenceId     TEXT    NOT NULL,          -- e.g. 'Q001' — now text since IDs are string-based
    detail          TEXT,
    eventTime       TEXT    NOT NULL           -- ISO-8601
);

-- Helpful indexes for the indexing engine (M6) to build on top of
CREATE INDEX idx_requests_urgency  ON service_requests(urgency);
CREATE INDEX idx_requests_status   ON service_requests(status);
CREATE INDEX idx_roads_from        ON roads(from_location_id);
CREATE INDEX idx_roads_to          ON roads(to_location_id);
