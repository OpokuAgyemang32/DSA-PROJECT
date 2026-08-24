package gh.ug.dcit204.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    public static void initialize(Connection connection) throws SQLException, IOException {
        try (InputStream input = DatabaseInitializer.class.getResourceAsStream("/schema.sql")) {
            if (input == null) {
                // When running from the project root, fall back to the file in sql/.
                initializeFromExternalFile(connection);
                return;
            }
            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append('\n');
                }
            }
            executeStatements(connection, sql.toString());
        }
    }

    private static void initializeFromExternalFile(Connection connection) throws SQLException, IOException {
        java.nio.file.Path path = java.nio.file.Path.of("sql", "schema.sql");
        String sql = java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
        executeStatements(connection, sql);
    }

    private static void executeStatements(Connection connection, String sql) throws SQLException {
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\\R")) {
            if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
            current.append(line).append('\n');
            if (line.trim().endsWith(";")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(current.toString());
                }
                current.setLength(0);
            }
        }
    }
}
