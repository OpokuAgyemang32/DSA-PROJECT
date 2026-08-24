package ghshoc.data;

import java.io.*;
import java.util.Random;

/**
 * Generates deterministic (fixed-seed) CSV seed data matching the official
 * course templates (locations_template.csv, roads_template.csv,
 * resources_template.csv, service_requests_template.csv) — same column
 * names and string-ID conventions ('L001', 'R001', 'Q001', etc.) — filled
 * with our chosen hospital operations context, sized per the brief:
 * 50 locations, 100 roads, 300 service requests, 30 resources.
 */
public class CsvSeedGenerator {

    private static final Random RNG = new Random(2026); // fixed seed -> reproducible dataset

    private static final String[] WARD_NAMES = {
        "Emergency Ward", "Surgical Ward", "Maternity Ward", "Pediatric Ward", "ICU",
        "General Ward A", "General Ward B", "Isolation Ward", "Cardiology Ward", "Orthopedic Ward"
    };
    // location_type values (kept close to the template's vocabulary: Library/Academic/Health, etc.,
    // specialised here to hospital departments)
    private static final String[] TYPES = {"WARD", "PHARMACY", "LAB", "ADMIN", "STORE", "GATE"};
    private static final String[] CATEGORIES = {"Medical", "Pharmacy", "Lab", "Equipment", "Document"};
    // resource_type -> ID prefix, matching the template convention (Van -> V001, Rider -> R001)
    private static final String[][] RESOURCE_TYPES = {
        {"Porter", "P"}, {"Ambulance", "A"}, {"Nurse", "N"}, {"Trolley", "T"}
    };
    private static final String[] STATUSES = {"NEW", "IN_PROGRESS", "COMPLETED"};

    public static void main(String[] args) throws IOException {
        String outDir = args.length > 0 ? args[0] : "data";
        new File(outDir).mkdirs();
        String[] locationIds = generateLocations(outDir + "/locations.csv", 50);
        generateRoads(outDir + "/roads.csv", locationIds, 100);
        generateServiceRequests(outDir + "/service_requests.csv", 300, locationIds);
        generateResources(outDir + "/resources.csv", 30, locationIds);
        System.out.println("Seed data written to " + outDir + "/ (template-format columns, string IDs)");
    }

    private static String id(String prefix, int n) {
        return String.format("%s%03d", prefix, n); // L001, R001, Q001, ...
    }

    private static String[] generateLocations(String path, int n) throws IOException {
        String[] ids = new String[n];
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("location_id,name,area,location_type,x_coord,y_coord");
            for (int i = 1; i <= n; i++) {
                String locId = id("L", i);
                ids[i - 1] = locId;
                String type = TYPES[RNG.nextInt(TYPES.length)];
                String name = type.equals("WARD")
                        ? WARD_NAMES[RNG.nextInt(WARD_NAMES.length)] + " " + i
                        : type + " Block " + (char) ('A' + RNG.nextInt(6));
                double x = 5.55 + RNG.nextDouble() * 0.02;  // synthetic campus-grid coords near Accra
                double y = -0.20 + RNG.nextDouble() * 0.02;
                w.printf("%s,%s,%s,%s,%.6f,%.6f%n", locId, csvSafe(name), "Korle-Bu Main Campus", type, x, y);
            }
        }
        return ids;
    }

    private static void generateRoads(String path, String[] locationIds, int targetRoads) throws IOException {
        int n = locationIds.length;
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("road_id,from_location_id,to_location_id,distance_km,travel_time_min,condition_weight");
            int roadNum = 1;
            // Step 1: guarantee connectivity — attach each location i (2..n) to a random earlier one
            for (int i = 1; i < n; i++) {
                int j = RNG.nextInt(i);
                roadNum = writeRoad(w, roadNum, locationIds[i], locationIds[j]);
            }
            // Step 2: add remaining random edges up to targetRoads total
            int remaining = targetRoads - (n - 1);
            for (int k = 0; k < remaining; k++) {
                String a = locationIds[RNG.nextInt(n)];
                String b = locationIds[RNG.nextInt(n)];
                if (a.equals(b)) { k--; continue; }
                roadNum = writeRoad(w, roadNum, a, b);
            }
        }
    }

    private static int writeRoad(PrintWriter w, int roadNum, String from, String to) {
        double distanceKm = (10 + RNG.nextDouble() * 190) / 1000.0;        // 10-200 metres, as km
        double travelTimeMin = (distanceKm * 1000) / (1.2 + RNG.nextDouble()) / 60.0; // walking speed, in minutes
        double condition = 1.0 + RNG.nextDouble() * 0.5;                   // 1.0-1.5 congestion multiplier
        w.printf("%s,%s,%s,%.3f,%.2f,%.2f%n", id("R", roadNum), from, to, distanceKm, travelTimeMin, condition);
        return roadNum + 1;
    }

    private static void generateServiceRequests(String path, int n, String[] locationIds) throws IOException {
        int numLocations = locationIds.length;
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("request_id,source_location_id,destination_location_id,category,urgency,time_submitted,deadline,status");
            for (int i = 1; i <= n; i++) {
                String source = locationIds[RNG.nextInt(numLocations)];
                String dest;
                do { dest = locationIds[RNG.nextInt(numLocations)]; } while (dest.equals(source));
                String category = CATEGORIES[RNG.nextInt(CATEGORIES.length)];
                int urgency = 1 + RNG.nextInt(5);
                int day = 1 + RNG.nextInt(28);
                int hour = RNG.nextInt(24), minute = RNG.nextInt(60);
                String timeSubmitted = String.format("2026-06-%02dT%02d:%02d:00", day, hour, minute);
                String deadline = urgency >= 4
                        ? String.format("2026-06-%02dT%02d:%02d:00", day, Math.min(hour + 1, 23), minute)
                        : "";
                String status = STATUSES[RNG.nextInt(STATUSES.length)];
                w.printf("%s,%s,%s,%s,%d,%s,%s,%s%n", id("Q", i), source, dest, category, urgency, timeSubmitted, deadline, status);
            }
        }
    }

    private static void generateResources(String path, int n, String[] locationIds) throws IOException {
        int numLocations = locationIds.length;
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("resource_id,resource_type,home_location_id,capacity,availability_status");
            String[] statuses = {"AVAILABLE", "BUSY", "OFFLINE"};
            // Track a running counter per resource-type prefix so IDs look like P001, A001, N001, T001, P002, ...
            java.util.Map<String, Integer> counters = new java.util.HashMap<>();
            for (int i = 1; i <= n; i++) {
                String[] typeAndPrefix = RESOURCE_TYPES[RNG.nextInt(RESOURCE_TYPES.length)];
                String type = typeAndPrefix[0], prefix = typeAndPrefix[1];
                int count = counters.merge(prefix, 1, Integer::sum);
                String home = locationIds[RNG.nextInt(numLocations)];
                int capacity = type.equals("Ambulance") ? 2 : 1;
                String status = statuses[RNG.nextInt(statuses.length)];
                w.printf("%s,%s,%s,%d,%s%n", id(prefix, count), type, home, capacity, status);
            }
        }
    }

    private static String csvSafe(String s) {
        return s.contains(",") ? "\"" + s + "\"" : s;
    }
}
