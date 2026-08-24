# GH-SHOC — Data Dictionary

This documents every field in the dataset, matching the official course
templates (`locations_template.csv`, `roads_template.csv`,
`resources_template.csv`, `service_requests_template.csv`) exactly in
column name and ID format. Content is filled in for our chosen Ghana
context: hospital operations (Korle-Bu style department/corridor network).

## locations.csv → `locations` table
| Field | Type | Example | Description |
|---|---|---|---|
| `location_id` | TEXT (PK) | `L001` | Unique ID, prefix L + 3-digit number |
| `name` | TEXT | `Emergency Ward 6` | Human-readable department/block name |
| `area` | TEXT | `Korle-Bu Main Campus` | Campus/site the location sits within |
| `location_type` | TEXT | `WARD` | One of: WARD, PHARMACY, LAB, ADMIN, STORE, GATE |
| `x_coord` | REAL | `5.566984` | Synthetic campus-grid coordinate (analogous to latitude) |
| `y_coord` | REAL | `-0.184192` | Synthetic campus-grid coordinate (analogous to longitude) |

## roads.csv → `roads` table
| Field | Type | Example | Description |
|---|---|---|---|
| `road_id` | TEXT (PK) | `R001` | Unique ID, prefix R + 3-digit number |
| `from_location_id` | TEXT (FK → locations) | `L002` | Corridor start point |
| `to_location_id` | TEXT (FK → locations) | `L001` | Corridor end point |
| `distance_km` | REAL | `0.198` | Corridor length in kilometres (short — intra-campus) |
| `travel_time_min` | REAL | `2.07` | Estimated walking/trolley time in minutes |
| `condition_weight` | REAL | `1.30` | Congestion/obstruction multiplier, 1.0 (clear) – 1.5 (congested) |

Edge weight used by the routing algorithms = `travel_time_min × condition_weight`.

## service_requests.csv → `service_requests` table
| Field | Type | Example | Description |
|---|---|---|---|
| `request_id` | TEXT (PK) | `Q001` | Unique ID, prefix Q + 3-digit number |
| `source_location_id` | TEXT (FK → locations) | `L026` | Where the request originates |
| `destination_location_id` | TEXT (FK → locations) | `L040` | Where the request needs to go |
| `category` | TEXT | `Medical` | One of: Medical, Pharmacy, Lab, Equipment, Document |
| `urgency` | INTEGER (1–5) | `5` | 5 = most urgent (e.g. cardiac), 1 = least urgent |
| `time_submitted` | TEXT (ISO-8601) | `2026-06-17T05:56:00` | When the request was logged |
| `deadline` | TEXT (ISO-8601, nullable) | `2026-06-17T06:56:00` | Only set for urgency ≥ 4; empty otherwise |
| `status` | TEXT | `NEW` | One of: NEW, IN_PROGRESS, COMPLETED, CANCELLED |

## resources.csv → `resources` table
| Field | Type | Example | Description |
|---|---|---|---|
| `resource_id` | TEXT (PK) | `P001` | Prefix indicates type: P=Porter, A=Ambulance, N=Nurse, T=Trolley |
| `resource_type` | TEXT | `Porter` | One of: Porter, Ambulance, Nurse, Trolley |
| `home_location_id` | TEXT (FK → locations) | `L026` | Base location when idle |
| `capacity` | INTEGER | `1` | People/items it can carry (Ambulance = 2, others = 1) |
| `availability_status` | TEXT | `AVAILABLE` | One of: AVAILABLE, BUSY, OFFLINE |

## Two additional tables (not in the CSV templates, populated by the running program)

### algorithm_runs
Records timing measurements from `ghshoc.perf.PerformanceHarness` — see `data/performance/*.csv` for the raw data and `docs/performance_analysis.md` for the write-up. Fields: `runId`, `algorithmName`, `inputSize`, `timeNs`, `memoryKb`, `dateRun`.

### audit_events
Written to live by `Main.java` every time a request is dispatched or an undo happens — this is the proof that the program reads AND writes the database, not just seed data at startup. Fields: `eventId`, `eventType` (DISPATCH/UNDO_ASSIGN), `referenceTable`, `referenceId` (the affected request_id), `detail`, `eventTime`.

## Dataset size
| File | Rows |
|---|---|
| locations.csv | 50 |
| roads.csv | 100 |
| service_requests.csv | 300 |
| resources.csv | 30 |
| **Total** | **480** |

## Regenerating the dataset
```
javac -d out src/main/java/ghshoc/data/CsvSeedGenerator.java
java -cp out ghshoc.data.CsvSeedGenerator data
```
Uses a fixed random seed (2026), so output is identical every time you regenerate it — useful if you need to show the dataset is reproducible, not hand-edited.
