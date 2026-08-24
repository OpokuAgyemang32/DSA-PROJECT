 GH-SHOC — Ghana Smart Hospital Operations Optimizer
DCIT 204/308 Joint DSA Project — Hospital/clinic operations context



 schema, seed data, database loader, and console app were revised
to exactly match the official course-issued templates (`locations_template.csv`,
`roads_template.csv`, `resources_template.csv`, `service_requests_template.csv`)
— string IDs (`L001`, `R001`, `Q001`, `P001`/`A001`/`N001`/`T001`) and the
template's exact column names, while keeping the GH-SHOC hospital operations
context and story. See `docs/data_dictionary.md` for the full field-by-field
mapping. All 123 unit tests + the full console app were re-verified working
against the new schema.


- `sql/schema.sql` — full database schema per Section 4.
- `src/main/java/ghshoc/ds/` — 11 custom data structures, no built-in Java
  collections (Section 8.i): DynamicArray, DoublyLinkedList, ArrayStack,
  CircularQueue, Deque, MinHeap, BST, RedBlackTree, HashTable, DisjointSet, Graph.
- `src/main/java/ghshoc/algo/` — search & sort algorithms (Section 7):
  - `SearchAlgorithms.java` — linear search O(n), binary search O(log n),
    both instrumented with a comparison counter for the Phase 6 empirical harness
  - `SortAlgorithms.java` — bubble, insertion, selection, merge, quick sort,
    each instrumented with comparisons + swaps/writes. Quick sort uses a
    fixed-seed random pivot so results are reproducible but not vulnerable
    to the classic sorted-input worst case.
- `src/main/java/ghshoc/trace/TraceGenerator.java` — produces verified
  step-by-step output (not hand-worked) for the report's trace tables.
- `docs/trace_tables.md` — ready-to-paste trace tables for binary search,
  insertion sort, and merge sort, generated from the actual code.
- `src/main/java/ghshoc/algo/GraphAlgorithms.java` — route engine (Section 7 / M7):
  - `bfs` — unweighted shortest path (hop count), O(V+E), built on `CircularQueue`
  - `dfs` — iterative reachability/connectivity check, O(V+E), built on `ArrayStack` (no recursion — safe on large graphs)
  - `dijkstra` — weighted shortest path, O((V+E) log V), via a lazy-deletion
    binary heap keyed by double weight (separate from the int-keyed `MinHeap`
    in Phase 2, since edge weights are continuous)
  - `primMST` / `kruskalMST` — minimum spanning tree, two independent
    implementations that **cross-validate each other** (both must produce
    the same total weight on the same graph — useful correctness evidence
    for your report). Kruskal uses `DisjointSet` for cycle detection and a
    from-scratch merge sort (no `java.util.Arrays.sort` anywhere in the codebase).
- `src/main/java/ghshoc/algo/GreedyAlgorithms.java` + `DPAlgorithms.java` —
  optimisation engine (Section 7 / M8):
  - `activitySelection` — provably optimal greedy (exchange-argument proof
    in the javadoc), max non-overlapping requests one resource can serve
  - `fractionalKnapsack` — provably optimal greedy for divisible resources
  - **`zeroOneKnapsackGreedy` vs `zeroOneKnapsackDP` — the required
    counterexample.** Same instance, indivisible items: greedy (by
    value/weight ratio) gets 160, DP gets the true optimum 220, and an
    exhaustive brute-force check over all 8 subsets confirms 220 is
    correct independent of the DP code. This is the concrete proof that
    greedy is not always optimal and DP is genuinely necessary — see
    `Phase5OptimisationTest.testGreedyVsDpCounterexample` for the full
    worked instance and reasoning, ready to drop into your report.
  - `longestCommonSubsequence` — bonus DP example (fuzzy drug-name matching)
- `src/test/java/ghshoc/` — Phase 1-5 tests, **123 tests total, all passing**.


```
javac -d out $(find src/main src/test -name "*.java")
java -ea -cp out ghshoc.Phase1StructureTest
java -ea -cp out ghshoc.Phase2StructureTest
java -ea -cp out ghshoc.Phase3AlgorithmTest
java -ea -cp out ghshoc.Phase4GraphAlgorithmTest
java -ea -cp out ghshoc.Phase5OptimisationTest
java -cp out ghshoc.trace.TraceGenerator
```

 Phase 6: Database, integration, and empirical performance (final)
- `lib/sqlite-jdbc-3.53.2.1.jar` — the real JDBC driver (downloaded from
  the official GitHub release, not fabricated) — this is genuine JDBC
  against a real SQLite file, the same API you'd use for MySQL/Postgres.
- `src/main/java/ghshoc/data/CsvSeedGenerator.java` — deterministic
  (fixed-seed) CSV generator producing exactly 50 locations, 100 roads,
  300 service requests, 30 resources, matching schema.sql. Re-run to
  regenerate `data/*.csv`.
- `src/main/java/ghshoc/db/DatabaseLoader.java` — executes schema.sql
  against SQLite, bulk-loads all 4 CSVs via `PreparedStatement` batches,
  runs a sanity `GROUP BY` query. Verified working end-to-end (480 rows loaded).
- `src/main/java/ghshoc/Main.java` — the console menu, tying every
  structure/algorithm from Phases 1-5 to the live database. **Reads AND
  writes**: dispatching a request runs a real `UPDATE service_requests`
  and `INSERT INTO audit_events`; undo reverses both. Option 8 queries
  `audit_events` straight from SQLite so you can see the writes actually
  landed, not just trust the console output. This satisfies the brief's
  explicit requirement: *"the final program must read from and write to
  the database... it must be part of the running system."*
- `src/main/java/ghshoc/perf/PerformanceHarness.java` — real timed
  benchmarks (not estimates) across increasing input sizes for every
  sort/search/tree/hash/graph algorithm, exported to `data/performance/*.csv`.
- `scripts/plot_performance.py` — turns those CSVs into 7 PNG charts in
  `docs/charts/`. Standout result: BST height hits 4999 at n=5000 under
  adversarial sorted-order insertion, while Red-Black Tree stays at 12.
- `docs/performance_analysis.md` — Section 9 write-up interpreting every
  chart, ready to paste into the report.

 How to run everything
```
# Compile (needs the sqlite-jdbc jar on the classpath for db/menu classes)
javac -cp lib/sqlite-jdbc-3.53.2.1.jar -d out $(find src/main src/test -name "*.java")

# Unit tests (123 tests, no DB needed)
java -ea -cp out ghshoc.Phase1StructureTest
java -ea -cp out ghshoc.Phase2StructureTest
java -ea -cp out ghshoc.Phase3AlgorithmTest
java -ea -cp out ghshoc.Phase4GraphAlgorithmTest
java -ea -cp out ghshoc.Phase5OptimisationTest

# Regenerate seed data (optional — data/*.csv already included)
java -cp out ghshoc.data.CsvSeedGenerator data

# Run the console app (creates+seeds ghshoc.db fresh each run)
java -cp "out:lib/sqlite-jdbc-3.53.2.1.jar" ghshoc.Main

# Regenerate performance data + charts (optional — already included)
java -cp out ghshoc.perf.PerformanceHarness data/performance
python3 scripts/plot_performance.py data/performance docs/charts
```

 Full project structure
```
ghshoc/
  sql/schema.sql                     — 6-table SQLite schema
  data/*.csv                         — seed data (50/100/300/30 rows)
  data/performance/*.csv             — raw benchmark measurements
  lib/sqlite-jdbc-3.53.2.1.jar       — real JDBC driver
  src/main/java/ghshoc/
    ds/        — 11 custom data structures
    algo/      — search, sort, graph, greedy, DP algorithms
    db/        — JDBC schema + CSV loader
    data/      — CSV seed generator
    perf/      — performance benchmark harness
    trace/     — verified trace-table generator
    Main.java  — console application
  src/test/java/ghshoc/              — 123 tests across 5 phases
  scripts/plot_performance.py        — CSV → PNG chart generator
  docs/
    trace_tables.md                  — Section 7 trace tables
    performance_analysis.md          — Section 9 write-up
    charts/*.png                     — 7 performance charts
  README.md                          — this file
```

 What's still on you
This gets you a fully working, tested system — but the brief (Section 15)
requires you to be able to explain and modify every part of it at the oral
defense, and requires disclosure of AI assistance. Before submission:
1. Read through the code** — start with `Main.java` (it ties everything
   together) then trace into whichever structures/algorithms you're least
   sure of. Ask me to walk through any specific file in depth.
2. Write the remaining report prose** — problem statement, individual
   contribution breakdown per team member, architecture diagram, and the
   edge-case discussion (this repo gives you the technical content and
   evidence; the narrative connecting it to your specific team's process
   still needs to be written by your group).
3. Disclose AI assistance** per Section 15, and be ready to explain any
   line of this code without me in the room.
- Phase 6: JDBC database loader, CSV seed data (50 locations/100 roads/300
  requests/30 resources), console menu, empirical performance harness + CSVs
  + graphs, and report sections (trace tables, proof sketches, edge cases)
