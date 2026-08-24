package gh.ug.dcit204.database;

import gh.ug.dcit204.model.Location;
import gh.ug.dcit204.model.ServiceRequest;

import java.sql.Connection;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Ghana Smart Service Operations Optimizer ===");
        System.out.println("=== JDBC & SQLite Database Data Loader ===\n");

        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("[1/4] JDBC connection: SUCCESS");

            DatabaseInitializer.initialize(connection);
            System.out.println("[2/4] Database schema: READY");

            CsvDataLoader.LoadReport report = CsvDataLoader.loadAll(connection);
            System.out.println("[3/4] CSV import: SUCCESS");
            System.out.println("      Locations:        " + report.locations());
            System.out.println("      Roads:            " + report.roads());
            System.out.println("      Service requests: " + report.serviceRequests());
            System.out.println("      Resources:        " + report.resources());
            System.out.println("      Total records:    " + report.total());

            DataRepository repository = new DataRepository(connection);
            List<Location> locations = repository.loadLocations();
            List<ServiceRequest> requests = repository.loadServiceRequests();

            System.out.println("\n[4/4] Database reload: SUCCESS");
            System.out.println("      Locations reloaded into Java objects: " + locations.size());
            System.out.println("      Requests reloaded into Java objects:  " + requests.size());
            System.out.println("\nSample location: " + locations.get(0));
            System.out.println("Sample request:  " + requests.get(0));

            System.out.println("\nDatabase counts:");
            System.out.println("  locations        = " + repository.count("locations"));
            System.out.println("  roads            = " + repository.count("roads"));
            System.out.println("  service_requests = " + repository.count("service_requests"));
            System.out.println("  resources        = " + repository.count("resources"));
            System.out.println("\nALL DATABASE LOADER STEPS COMPLETED.");
        } catch (Exception e) {
            System.err.println("\nDATABASE LOADER FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
