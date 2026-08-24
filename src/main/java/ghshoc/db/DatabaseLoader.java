package ghshoc.db;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;

/**
 * Database integration layer — GH-SHOC Section 4/M2. Uses real JDBC
 * (org.sqlite.JDBC, from the sqlite-jdbc driver in lib/) against a SQLite
 * file, not an in-memory mock: this is the same API surface you'd use for
 * MySQL/Postgres in production, swapping only the connection URL/driver.
 */
public class DatabaseLoader {

    public static Connection connect(String dbFilePath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
    }

    /** Executes the full schema.sql (multiple statements, split on ';') against a fresh connection. */
    public static void initSchema(Connection conn, String schemaSqlPath) throws SQLException, IOException {
        String raw = Files.readString(new File(schemaSqlPath).toPath());
        // Strip full-line comments first — some contain ';' inside prose (e.g. "portable; swap types..."),
        // which would otherwise be mistaken for a statement terminator by the naive split below.
        StringBuilder sql = new StringBuilder();
        for (String line : raw.split("\n")) {
            if (line.strip().startsWith("--")) continue;
            sql.append(line).append('\n');
        }
        try (Statement stmt = conn.createStatement()) {
            for (String rawStatement : sql.toString().split(";")) {
                String s = rawStatement.strip();
                if (s.isEmpty()) continue;
                stmt.execute(s);
            }
        }
    }

    /** Generic CSV bulk loader: reads a CSV with a header row and inserts every row via a parameterised statement. */
    public static int loadCsv(Connection conn, String csvPath, String insertSql, int columnCount) throws SQLException, IOException {
        int rowsInserted = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            String header = br.readLine(); // skip header
            String line;
            conn.setAutoCommit(false);
            while ((line = br.readLine()) != null) {
                String[] fields = splitCsvLine(line, columnCount);
                for (int i = 0; i < columnCount; i++) {
                    String v = fields[i];
                    if (v == null || v.isEmpty()) ps.setNull(i + 1, Types.NULL);
                    else ps.setString(i + 1, v);
                }
                ps.addBatch();
                rowsInserted++;
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        }
        return rowsInserted;
    }

    /** Minimal CSV split that respects quoted fields (handles the one quoted-name edge case in locations.csv). */
    private static String[] splitCsvLine(String line, int expectedFields) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) { fields.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        fields.add(cur.toString());
        while (fields.size() < expectedFields) fields.add(""); // pad short rows (e.g. empty trailing deadline)
        return fields.toArray(new String[0]);
    }

    /** Loads all four CSVs for the GH-SHOC dataset into an already-schema'd connection. */
    public static void loadAll(Connection conn, String dataDir) throws SQLException, IOException {
        int locs = loadCsv(conn, dataDir + "/locations.csv",
                "INSERT INTO locations(location_id,name,area,location_type,x_coord,y_coord) VALUES(?,?,?,?,?,?)", 6);
        int roads = loadCsv(conn, dataDir + "/roads.csv",
                "INSERT INTO roads(road_id,from_location_id,to_location_id,distance_km,travel_time_min,condition_weight) VALUES(?,?,?,?,?,?)", 6);
        int requests = loadCsv(conn, dataDir + "/service_requests.csv",
                "INSERT INTO service_requests(request_id,source_location_id,destination_location_id,category,urgency,time_submitted,deadline,status) VALUES(?,?,?,?,?,?,?,?)", 8);
        int resources = loadCsv(conn, dataDir + "/resources.csv",
                "INSERT INTO resources(resource_id,resource_type,home_location_id,capacity,availability_status) VALUES(?,?,?,?,?)", 5);
        System.out.printf("Loaded: %d locations, %d roads, %d requests, %d resources%n", locs, roads, requests, resources);
    }

    public static void main(String[] args) throws Exception {
        String dbFile = args.length > 0 ? args[0] : "ghshoc.db";
        String schemaPath = args.length > 1 ? args[1] : "sql/schema.sql";
        String dataDir = args.length > 2 ? args[2] : "data";

        new File(dbFile).delete(); // fresh DB each run for reproducibility
        try (Connection conn = connect(dbFile)) {
            initSchema(conn, schemaPath);
            loadAll(conn, dataDir);

            // Sanity query proving the JDBC round-trip actually works
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT urgency, COUNT(*) as cnt FROM service_requests GROUP BY urgency ORDER BY urgency DESC")) {
                System.out.println("Request counts by urgency:");
                while (rs.next()) {
                    System.out.printf("  urgency %d: %d requests%n", rs.getInt("urgency"), rs.getInt("cnt"));
                }
            }
        }
        System.out.println("Database ready at " + dbFile);
    }
}
