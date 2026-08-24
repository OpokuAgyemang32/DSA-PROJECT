package gh.ug.dcit204.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:smart_service.db";

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
