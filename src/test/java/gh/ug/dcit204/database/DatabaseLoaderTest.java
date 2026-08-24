package gh.ug.dcit204.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseLoaderTest {
    @Test
    void loadsExpectedSeedCounts() throws Exception {
        Path temp = Files.createTempFile("smart-service-test-", ".db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + temp)) {
            DatabaseInitializer.initialize(connection);
            CsvDataLoader.LoadReport report = CsvDataLoader.loadAll(connection);
            assertEquals(50, report.locations());
            assertEquals(100, report.roads());
            assertEquals(300, report.serviceRequests());
            assertEquals(30, report.resources());
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
