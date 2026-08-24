# Ghana Smart Service Operations Optimizer — JDBC & Database Data Loader

## Purpose
This module implements M2 (Database and Data Loader) for the DCIT 204/308 Joint DSA Semester Project.

It provides:
- SQLite schema creation through JDBC.
- CSV import for locations, roads, service requests and resources.
- Input validation before database insertion.
- Transaction + rollback protection during seed loading.
- Foreign-key validation between roads/requests/resources and locations.
- Java model objects reloaded from SQLite for the team's custom data structures and algorithms.
- `algorithm_runs` and `audit_events` tables for later project modules.

## Dataset supplied by the project
The included seed files contain:
- 50 locations
- 100 roads
- 300 service requests
- 30 resources

These match the minimum dataset requirements in the project brief.

## Requirements
- JDK 17 or later
- Maven 3.9+ recommended
- VS Code with Extension Pack for Java recommended

## Open in VS Code
1. Extract this folder.
2. Open VS Code.
3. Select **File → Open Folder** and choose `JDBC_Database_Data_Loader`.
4. Wait for Maven/Java extensions to finish loading.
5. Open `src/main/java/gh/ug/dcit204/database/Main.java`.

## Run
From the VS Code terminal:

```bash
mvn clean test
mvn exec:java
```

The first command compiles the project and runs the database loader test.
The second command creates `smart_service.db` in the project root, creates all tables, loads the CSV files, validates the foreign keys and prints database counts.

Expected counts:

```text
locations        = 50
roads            = 100
service_requests = 300
resources        = 30
```

## Main classes
- `DatabaseConnection.java` — JDBC connection to SQLite.
- `DatabaseInitializer.java` — creates the schema.
- `CsvParser.java` — parses CSV lines including quoted fields.
- `DataValidator.java` — validates IDs, numbers, urgency and required fields.
- `CsvDataLoader.java` — imports all four CSV datasets inside one transaction.
- `DataRepository.java` — reads database rows back into Java model objects.
- `Main.java` — demonstration entry point.

## Database tables
The schema creates:
- `locations`
- `roads`
- `service_requests`
- `resources`
- `algorithm_runs`
- `audit_events`

## How this connects to the rest of the team
Other modules should not read CSV files directly. They should use `DataRepository` to obtain records from SQLite, then copy those records into the team's assessed custom structures (graph, queue, priority queue/heap, BST, hash table, etc.).

Example:

```java
DataRepository repository = new DataRepository(connection);
var locations = repository.loadLocations();
var roads = repository.loadRoads();
var requests = repository.loadServiceRequests();
var resources = repository.loadResources();
```

The `java.util.ArrayList` instances in the database layer are only transport collections. They are not intended to replace the custom assessed data structures required by the project.

## Validation rules implemented
- Correct CSV headers.
- Required text fields cannot be empty.
- IDs must be positive and unique within each CSV.
- Road distance/travel time cannot be negative.
- Road condition weight must be greater than zero.
- Request urgency must be 1–5.
- Request source/destination must exist in `locations`.
- Road endpoints must exist in `locations`.
- Resource home locations must exist in `locations`.
- Resource capacity must be positive.

## Important
Do not commit the generated `smart_service.db` if the team wants the database generated from `schema.sql` + CSV seed data. The `.gitignore` already excludes SQLite runtime files.
