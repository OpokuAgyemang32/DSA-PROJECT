package gh.ug.dcit204.model;

public record Road(int roadId, int fromLocationId, int toLocationId, double distance, double travelTime, double roadConditionWeight) {}
