package ghshoc;

import ghshoc.db.DatabaseLoader;
import ghshoc.ds.*;
import ghshoc.algo.*;

import java.sql.*;
import java.util.Scanner;

/**
 * GH-SHOC console application — Section 5/M3 (integration layer).
 * Loads the SQLite-backed dataset (schema/CSVs use the official course
 * template columns and string IDs like 'L001','Q007'), builds the
 * in-memory graph and priority queue from it, and exposes every data
 * structure/algorithm built in Phases 1-5 through a single menu.
 *
 * Internally, Graph still works with int vertex indices (0..n-1) for
 * performance — locationIndex/locationIdByIndex translate between the
 * external string IDs (what the user types, what the DB stores) and
 * those internal indices.
 */
public class Main {

    static Connection conn; // kept open for the whole session so dispatch/undo can write back
    static Graph corridorGraph;
    static int numLocations;
    static String[] locationIdByIndex;                 // index -> 'L001'
    static HashTable<String, Integer> locationIndex;    // 'L001' -> index
    static HashTable<String, String> locationNames;     // 'L001' -> 'Emergency Ward 3'
    static HashTable<String, String> resourceStatus = new HashTable<>();
    static ArrayStack<DispatchRequest> auditLog = new ArrayStack<>();
    static MinHeap<DispatchRequest> dispatchQueue = new MinHeap<>(); // priority = 6-urgency (lower=served first)

    /** A pending/dispatched service request, keyed by its string ID (e.g. 'Q014'). */
    static class DispatchRequest {
        String requestId, sourceId, destId;
        int urgency;
        DispatchRequest(String requestId, String sourceId, String destId, int urgency) {
            this.requestId = requestId; this.sourceId = sourceId; this.destId = destId; this.urgency = urgency;
        }
    }

    public static void main(String[] args) throws Exception {
        String dbFile = "ghshoc.db";
        new java.io.File(dbFile).delete();
        conn = DatabaseLoader.connect(dbFile); // stays open for the whole run — this is what makes dispatch/undo real writes
        DatabaseLoader.initSchema(conn, "sql/schema.sql");
        DatabaseLoader.loadAll(conn, "data");
        loadIntoMemory(conn);

        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choice: ");
            String choice = sc.hasNextLine() ? sc.nextLine().trim() : "0";
            switch (choice) {
                case "1" -> listLocations();
                case "2" -> shortestRoute(sc);
                case "3" -> minimumSpanningNetwork();
                case "4" -> dispatchNext();
                case "5" -> undoLastDispatch();
                case "6" -> searchSortDemo();
                case "7" -> knapsackCounterexampleDemo();
                case "8" -> showAuditLogFromDatabase();
                case "0" -> running = false;
                default -> System.out.println("Unrecognised choice.");
            }
        }
        conn.close();
        System.out.println("Goodbye.");
    }

    static void printMenu() {
        System.out.println("\n=== GH-SHOC: Ghana Smart Hospital Operations Optimizer ===");
        System.out.println("1) List all locations");
        System.out.println("2) Shortest route between two locations (Dijkstra)");
        System.out.println("3) Minimum-cost corridor network (Prim + Kruskal cross-check)");
        System.out.println("4) Dispatch next request by urgency (priority queue)");
        System.out.println("5) Undo last dispatch (audit stack)");
        System.out.println("6) Search & sort demo (linear/binary search, 4 sort algorithms)");
        System.out.println("7) Greedy-vs-DP knapsack counterexample");
        System.out.println("8) Show audit_events log (read directly from the database)");
        System.out.println("0) Exit");
    }

    static void loadIntoMemory(Connection conn) throws SQLException {
        // First pass: assign each location_id a stable internal integer index (0..n-1)
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM locations")) {
            rs.next();
            numLocations = rs.getInt(1);
        }
        locationIdByIndex = new String[numLocations];
        locationIndex = new HashTable<>();
        locationNames = new HashTable<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT location_id, name FROM locations ORDER BY location_id")) {
            int idx = 0;
            while (rs.next()) {
                String locId = rs.getString("location_id");
                locationIdByIndex[idx] = locId;
                locationIndex.put(locId, idx);
                locationNames.put(locId, rs.getString("name"));
                idx++;
            }
        }

        corridorGraph = new Graph(numLocations, false);
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT from_location_id, to_location_id, travel_time_min, condition_weight FROM roads")) {
            while (rs.next()) {
                int from = locationIndex.get(rs.getString("from_location_id"));
                int to = locationIndex.get(rs.getString("to_location_id"));
                double weight = rs.getDouble("travel_time_min") * rs.getDouble("condition_weight");
                corridorGraph.addEdge(from, to, weight);
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT request_id, source_location_id, destination_location_id, urgency FROM service_requests WHERE status='NEW'")) {
            while (rs.next()) {
                int priority = 6 - rs.getInt("urgency"); // urgency 5 (most urgent) -> priority key 1 (served first)
                dispatchQueue.insert(priority, new DispatchRequest(
                        rs.getString("request_id"), rs.getString("source_location_id"),
                        rs.getString("destination_location_id"), rs.getInt("urgency")));
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT resource_id, availability_status FROM resources")) {
            while (rs.next()) resourceStatus.put(rs.getString("resource_id"), rs.getString("availability_status"));
        }

        System.out.printf("Loaded into memory: %d locations, %d corridor edges, %d pending requests, %d resources%n",
                numLocations, corridorGraph.numEdges(), dispatchQueue.size(), resourceStatus.size());
    }

    static void listLocations() {
        int shown = Math.min(numLocations, 15);
        for (int i = 0; i < shown; i++) {
            String id = locationIdByIndex[i];
            System.out.printf("  [%s] %s%n", id, locationNames.get(id));
        }
        if (numLocations > shown) System.out.println("  ... and " + (numLocations - shown) + " more.");
    }

    static void shortestRoute(Scanner sc) {
        String fromId = readLocationId(sc, "From location ID (e.g. L001): ");
        if (fromId == null) return; // stop immediately — don't consume another input line for 'to'
        String toId = readLocationId(sc, "To location ID (e.g. L025): ");
        if (toId == null) return;

        int fromIdx = locationIndex.get(fromId), toIdx = locationIndex.get(toId);
        GraphAlgorithms.DijkstraResult r = GraphAlgorithms.dijkstra(corridorGraph, fromIdx);
        if (r.dist[toIdx] == Double.POSITIVE_INFINITY) {
            System.out.println("No route found (locations are in disconnected components).");
            return;
        }
        var path = r.pathTo(toIdx);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            String id = locationIdByIndex[path.get(i)];
            sb.append(locationNames.get(id)).append(" (").append(id).append(")");
            if (i < path.size() - 1) sb.append(" -> ");
        }
        System.out.printf("Shortest route (weighted cost %.2f): %s%n", r.dist[toIdx], sb);
    }

    /** Reads a location ID string from the user and validates it exists. Returns null (with a message) if invalid. */
    static String readLocationId(Scanner sc, String prompt) {
        System.out.print(prompt);
        String id = sc.nextLine().trim().toUpperCase();
        if (!locationIndex.containsKey(id)) {
            System.out.println("Unknown location ID '" + id + "'. Try option 1 to list valid IDs.");
            return null;
        }
        return id;
    }

    static void minimumSpanningNetwork() {
        GraphAlgorithms.MstResult prim = GraphAlgorithms.primMST(corridorGraph, 0);
        GraphAlgorithms.MstResult kruskal = GraphAlgorithms.kruskalMST(corridorGraph);
        System.out.printf("Prim MST:    %d edges, total weight %.2f%n", prim.edges.size(), prim.totalWeight);
        System.out.printf("Kruskal MST: %d edges, total weight %.2f%n", kruskal.edges.size(), kruskal.totalWeight);
        System.out.println(Math.abs(prim.totalWeight - kruskal.totalWeight) < 1e-6
                ? "Cross-check PASSED: both algorithms agree on the minimum cost."
                : "Cross-check MISMATCH — investigate (graph may be disconnected).");
    }

    static void dispatchNext() {
        if (dispatchQueue.isEmpty()) { System.out.println("No pending requests."); return; }
        DispatchRequest req = dispatchQueue.extractMin();
        String detail = String.format("request#%s (urgency %d): %s -> %s",
                req.requestId, req.urgency, locationNames.get(req.sourceId), locationNames.get(req.destId));

        try {
            // WRITE #1: persist the status change to service_requests
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE service_requests SET status='IN_PROGRESS' WHERE request_id=?")) {
                ps.setString(1, req.requestId);
                ps.executeUpdate();
            }
            // WRITE #2: log the event to audit_events — the audit trail lives in the DB, not just in-memory
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO audit_events(eventType,referenceTable,referenceId,detail,eventTime) VALUES(?,?,?,?,?)")) {
                ps.setString(1, "DISPATCH");
                ps.setString(2, "service_requests");
                ps.setString(3, req.requestId);
                ps.setString(4, detail);
                ps.setString(5, java.time.LocalDateTime.now().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("DB write failed: " + e.getMessage());
            return;
        }

        auditLog.push(req);
        System.out.println("DISPATCH " + detail + "  [written to service_requests + audit_events]");
    }

    static void undoLastDispatch() {
        if (auditLog.isEmpty()) { System.out.println("Nothing to undo."); return; }
        DispatchRequest req = auditLog.pop();
        String detail = String.format("request#%s (urgency %d): %s -> %s",
                req.requestId, req.urgency, locationNames.get(req.sourceId), locationNames.get(req.destId));

        try {
            // WRITE #1: revert the status change
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE service_requests SET status='NEW' WHERE request_id=?")) {
                ps.setString(1, req.requestId);
                ps.executeUpdate();
            }
            // WRITE #2: log the undo event too
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO audit_events(eventType,referenceTable,referenceId,detail,eventTime) VALUES(?,?,?,?,?)")) {
                ps.setString(1, "UNDO_ASSIGN");
                ps.setString(2, "service_requests");
                ps.setString(3, req.requestId);
                ps.setString(4, detail);
                ps.setString(5, java.time.LocalDateTime.now().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("DB write failed: " + e.getMessage());
            return;
        }

        dispatchQueue.insert(6 - req.urgency, req); // put it back in the priority queue too
        System.out.println("UNDO " + detail + "  [written to service_requests + audit_events]");
    }

    static void searchSortDemo() {
        int[] data = {42, 17, 93, 8, 55, 31, 76, 24, 61, 5};
        System.out.println("Original: " + java.util.Arrays.toString(data));

        int[] sortedCopy = data.clone();
        SortAlgorithms.mergeSort(sortedCopy);
        System.out.println("Sorted (merge sort): " + java.util.Arrays.toString(sortedCopy));

        var lin = SearchAlgorithms.linearSearch(data, 55);
        var bin = SearchAlgorithms.binarySearch(sortedCopy, 55);
        System.out.printf("Linear search for 55 in unsorted array: %s%n", lin);
        System.out.printf("Binary search for 55 in sorted array:  %s%n", bin);

        int[] a1 = data.clone(); SortAlgorithms.bubbleSort(a1);
        int[] a2 = data.clone(); SortAlgorithms.insertionSort(a2);
        int[] a3 = data.clone(); SortAlgorithms.selectionSort(a3);
        int[] a4 = data.clone(); var mStats = SortAlgorithms.mergeSort(a4);
        int[] a5 = data.clone(); var qStats = SortAlgorithms.quickSort(a5);
        System.out.println("All 5 sorts agree: " +
                (java.util.Arrays.equals(a1, a2) && java.util.Arrays.equals(a2, a3)
                 && java.util.Arrays.equals(a3, a4) && java.util.Arrays.equals(a4, a5)));
        System.out.println("Merge sort stats: " + mStats + "   Quick sort stats: " + qStats);
    }

    static void knapsackCounterexampleDemo() {
        GreedyAlgorithms.KnapsackItem[] items = {
            new GreedyAlgorithms.KnapsackItem(1, 10, 60),
            new GreedyAlgorithms.KnapsackItem(2, 20, 100),
            new GreedyAlgorithms.KnapsackItem(3, 30, 120)
        };
        double greedyValue = GreedyAlgorithms.zeroOneKnapsackGreedy(items, 50);
        var dp = DPAlgorithms.zeroOneKnapsackDP(new int[]{10, 20, 30}, new int[]{60, 100, 120}, 50);
        System.out.printf("Greedy (0/1, ratio heuristic): value = %.0f%n", greedyValue);
        System.out.printf("DP (optimal):                  value = %d%n", dp.maxValue);
        System.out.println(dp.maxValue > greedyValue
                ? "Confirms: greedy is suboptimal here; DP is required for the true optimum."
                : "Unexpected — greedy matched DP on this instance.");
    }

    /** Proves the writes from dispatchNext/undoLastDispatch actually landed — this queries SQLite directly, not memory. */
    static void showAuditLogFromDatabase() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT eventId, eventType, referenceId, detail, eventTime FROM audit_events ORDER BY eventId DESC LIMIT 10")) {
            System.out.println("Last 10 audit_events rows (read live from the database):");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("  [%d] %s ref#%s @ %s - %s%n",
                        rs.getInt("eventId"), rs.getString("eventType"), rs.getString("referenceId"),
                        rs.getString("eventTime"), rs.getString("detail"));
            }
            if (!any) System.out.println("  (empty — try dispatching a request first, option 4)");
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }
}
