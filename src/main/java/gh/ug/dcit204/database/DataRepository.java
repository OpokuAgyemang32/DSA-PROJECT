package gh.ug.dcit204.database;

import gh.ug.dcit204.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Read-only bridge from SQLite records to Java model objects used by the algorithm modules. */
public final class DataRepository {
    private final Connection connection;

    public DataRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Location> loadLocations() throws SQLException {
        List<Location> result = new ArrayList<>();
        String sql = "SELECT locationId,name,area,type,latitude,longitude FROM locations ORDER BY locationId";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Location(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getDouble(6)));
        }
        return result;
    }

    public List<Road> loadRoads() throws SQLException {
        List<Road> result = new ArrayList<>();
        String sql = "SELECT roadId,fromLocationId,toLocationId,distance,travelTime,roadConditionWeight FROM roads ORDER BY roadId";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Road(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6)));
        }
        return result;
    }

    public List<ServiceRequest> loadServiceRequests() throws SQLException {
        List<ServiceRequest> result = new ArrayList<>();
        String sql = "SELECT requestId,source,destination,category,urgency,timeSubmitted,deadline,status FROM service_requests ORDER BY requestId";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new ServiceRequest(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getString(6), rs.getString(7), rs.getString(8)));
        }
        return result;
    }

    public List<Resource> loadResources() throws SQLException {
        List<Resource> result = new ArrayList<>();
        String sql = "SELECT resourceId,type,homeLocation,capacity,availabilityStatus FROM resources ORDER BY resourceId";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Resource(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getString(5)));
        }
        return result;
    }

    public int count(String table) throws SQLException {
        if (!table.matches("[A-Za-z_]+")) throw new IllegalArgumentException("Invalid table name");
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + table); ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        }
    }

    public void saveAlgorithmRun(String algorithmName, int inputSize, long timeNs, double memoryKb, String dateRun) throws SQLException {
        String sql = "INSERT INTO algorithm_runs(algorithmName,inputSize,timeNs,memoryKb,dateRun) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, algorithmName); ps.setInt(2, inputSize); ps.setLong(3, timeNs); ps.setDouble(4, memoryKb); ps.setString(5, dateRun); ps.executeUpdate();
        }
    }
}
