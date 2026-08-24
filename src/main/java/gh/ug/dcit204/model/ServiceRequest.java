package gh.ug.dcit204.model;

public record ServiceRequest(int requestId, int source, int destination, String category, int urgency,
                             String timeSubmitted, String deadline, String status) {}
