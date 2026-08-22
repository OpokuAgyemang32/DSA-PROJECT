package hospital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain object for one row of data/service_requests.csv.
 *
 * CSV header: requestId,source,destination,category,urgency,timeSubmitted,deadline,status
 *
 * Notes on the real dataset (as opposed to the earlier template):
 *  - requestId is a plain integer, not a "Q001" style code.
 *  - source / destination are integer locationIds (foreign keys into locations.csv).
 *  - deadline can be an empty string in the CSV -> represented here as null.
 */
public class ServiceRequest {

    private final int requestId;
    private final int sourceLocationId;
    private final int destinationLocationId;
    private final String category;
    private final int urgency;              // 1 (low) .. 5 (high)
    private final LocalDateTime timeSubmitted;
    private final LocalDateTime deadline;    // nullable
    private final String status;

    public ServiceRequest(int requestId,
                           int sourceLocationId,
                           int destinationLocationId,
                           String category,
                           int urgency,
                           LocalDateTime timeSubmitted,
                           LocalDateTime deadline,
                           String status) {
        this.requestId = requestId;
        this.sourceLocationId = sourceLocationId;
        this.destinationLocationId = destinationLocationId;
        this.category = category;
        this.urgency = urgency;
        this.timeSubmitted = timeSubmitted;
        this.deadline = deadline;
        this.status = status;
    }

    public int getRequestId() { return requestId; }
    public int getSourceLocationId() { return sourceLocationId; }
    public int getDestinationLocationId() { return destinationLocationId; }
    public String getCategory() { return category; }
    public int getUrgency() { return urgency; }
    public LocalDateTime getTimeSubmitted() { return timeSubmitted; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getStatus() { return status; }

    public boolean hasDeadline() {
        return deadline != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceRequest)) return false;
        ServiceRequest that = (ServiceRequest) o;
        return requestId == that.requestId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "id=" + requestId +
                ", category='" + category + '\'' +
                ", urgency=" + urgency +
                ", submitted=" + timeSubmitted +
                ", deadline=" + deadline +
                ", status='" + status + '\'' +
                '}';
    }
}
