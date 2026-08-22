package hospital.util;

import hospital.model.ServiceRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads data/service_requests.csv into ServiceRequest objects.
 *
 * This is intentionally a minimal hand-rolled CSV reader (split on comma) because
 * the dataset has no quoted fields or embedded commas. If Role 3/4 (database loader)
 * later needs quoted-field support, swap this for a proper CSV library without
 * changing the ServiceRequest model.
 */
public final class CsvLoader {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private CsvLoader() {
        // utility class
    }

    public static List<ServiceRequest> loadServiceRequests(String csvPath) throws IOException {
        List<ServiceRequest> requests = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(csvPath))) {
            String header = reader.readLine(); // discard header row
            if (header == null) {
                return requests;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    requests.add(parseLine(line));
                } catch (Exception e) {
                    // Don't let one malformed row kill the whole load; report and skip.
                    System.err.println("Skipping malformed row " + lineNumber + ": " + line
                            + " (" + e.getMessage() + ")");
                }
            }
        }
        return requests;
    }

    private static ServiceRequest parseLine(String line) {
        // requestId,source,destination,category,urgency,timeSubmitted,deadline,status
        String[] fields = line.split(",", -1); // -1 keeps trailing empty fields (e.g. blank deadline)
        if (fields.length != 8) {
            throw new IllegalArgumentException("expected 8 fields, found " + fields.length);
        }

        int requestId = Integer.parseInt(fields[0].trim());
        int source = Integer.parseInt(fields[1].trim());
        int destination = Integer.parseInt(fields[2].trim());
        String category = fields[3].trim();
        int urgency = Integer.parseInt(fields[4].trim());
        LocalDateTime timeSubmitted = parseTimestamp(fields[5].trim());
        LocalDateTime deadline = parseTimestamp(fields[6].trim()); // may be null
        String status = fields[7].trim();

        return new ServiceRequest(requestId, source, destination, category,
                urgency, timeSubmitted, deadline, status);
    }

    private static LocalDateTime parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(raw, TIMESTAMP_FORMAT);
    }
}
