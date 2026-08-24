package gh.ug.dcit204.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Imports the supplied CSV seed files into SQLite using JDBC prepared statements and transactions. */
public final class CsvDataLoader {
    private CsvDataLoader() {}

    public static LoadReport loadAll(Connection connection) throws Exception {
        connection.setAutoCommit(false);
        try {
            clearSeedTables(connection);
            int locations = loadLocations(connection, "/data/locations.csv");
            Set<Integer> locationIds = DataValidator.locationIds(connection);
            int roads = loadRoads(connection, "/data/roads.csv", locationIds);
            int requests = loadServiceRequests(connection, "/data/service_requests.csv", locationIds);
            int resources = loadResources(connection, "/data/resources.csv", locationIds);
            logAudit(connection, "DATA_LOAD", "Loaded CSV seed data: " + locations + " locations, " + roads + " roads, " + requests + " service requests, " + resources + " resources.");
            connection.commit();
            return new LoadReport(locations, roads, requests, resources);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void clearSeedTables(Connection connection) throws SQLException {
        // Delete dependent tables first so the seed loader can be run repeatedly.
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM audit_events");
            statement.executeUpdate("DELETE FROM algorithm_runs");
            statement.executeUpdate("DELETE FROM service_requests");
            statement.executeUpdate("DELETE FROM roads");
            statement.executeUpdate("DELETE FROM resources");
            statement.executeUpdate("DELETE FROM locations");
        }
    }

    private static InputStream open(String resource) throws IOException {
        InputStream in = CsvDataLoader.class.getResourceAsStream(resource);
        if (in == null) throw new IOException("CSV resource not found: " + resource);
        return in;
    }

    private static int loadLocations(Connection connection, String resource) throws Exception {
        List<List<String>> rows;
        try (InputStream in = open(resource)) {
            rows = CsvParser.readAll(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        if (rows.isEmpty()) throw new DataValidationException("locations.csv is empty");
        DataValidator.validateHeader(rows.get(0), "locationId", "name", "area", "type", "latitude", "longitude");
        String sql = "INSERT INTO locations(locationId,name,area,type,latitude,longitude) VALUES(?,?,?,?,?,?)";
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i); int row = i + 1;
                if (r.size() != 6) throw new DataValidationException("locations.csv row " + row + " has " + r.size() + " columns");
                int id = DataValidator.integer(r.get(0), "locationId", row); DataValidator.positive(id, "locationId", row); DataValidator.uniqueId(id, ids, "locationId", row);
                String name = DataValidator.required(r.get(1), "name", row);
                String area = DataValidator.required(r.get(2), "area", row);
                String type = DataValidator.required(r.get(3), "type", row);
                double lat = DataValidator.decimal(r.get(4), "latitude", row);
                double lon = DataValidator.decimal(r.get(5), "longitude", row);
                ps.setInt(1,id); ps.setString(2,name); ps.setString(3,area); ps.setString(4,type); ps.setDouble(5,lat); ps.setDouble(6,lon); ps.addBatch();
            }
            ps.executeBatch();
        }
        return rows.size() - 1;
    }

    private static int loadRoads(Connection connection, String resource, Set<Integer> locationIds) throws Exception {
        List<List<String>> rows;
        try (InputStream in = open(resource)) { rows = CsvParser.readAll(new InputStreamReader(in, StandardCharsets.UTF_8)); }
        if (rows.isEmpty()) throw new DataValidationException("roads.csv is empty");
        DataValidator.validateHeader(rows.get(0), "roadId", "fromLocationId", "toLocationId", "distance", "travelTime", "roadConditionWeight");
        String sql = "INSERT INTO roads(roadId,fromLocationId,toLocationId,distance,travelTime,roadConditionWeight) VALUES(?,?,?,?,?,?)";
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i=1;i<rows.size();i++) {
                List<String> r=rows.get(i); int row=i+1;
                if(r.size()!=6) throw new DataValidationException("roads.csv row "+row+" has "+r.size()+" columns");
                int id=DataValidator.integer(r.get(0),"roadId",row); DataValidator.positive(id,"roadId",row); DataValidator.uniqueId(id,ids,"roadId",row);
                int from=DataValidator.integer(r.get(1),"fromLocationId",row); int to=DataValidator.integer(r.get(2),"toLocationId",row);
                if(!locationIds.contains(from)||!locationIds.contains(to)) throw new DataValidationException("Row "+row+": road references a location that does not exist");
                double distance=DataValidator.decimal(r.get(3),"distance",row); double time=DataValidator.decimal(r.get(4),"travelTime",row); double weight=DataValidator.decimal(r.get(5),"roadConditionWeight",row);
                DataValidator.nonNegative(distance,"distance",row); DataValidator.nonNegative(time,"travelTime",row); if(weight<=0) throw new DataValidationException("Row "+row+": roadConditionWeight must be > 0");
                ps.setInt(1,id);ps.setInt(2,from);ps.setInt(3,to);ps.setDouble(4,distance);ps.setDouble(5,time);ps.setDouble(6,weight);ps.addBatch();
            }
            ps.executeBatch();
        }
        return rows.size()-1;
    }

    private static int loadServiceRequests(Connection connection, String resource, Set<Integer> locationIds) throws Exception {
        List<List<String>> rows;
        try(InputStream in=open(resource)){rows=CsvParser.readAll(new InputStreamReader(in,StandardCharsets.UTF_8));}
        if(rows.isEmpty()) throw new DataValidationException("service_requests.csv is empty");
        DataValidator.validateHeader(rows.get(0),"requestId","source","destination","category","urgency","timeSubmitted","deadline","status");
        String sql="INSERT INTO service_requests(requestId,source,destination,category,urgency,timeSubmitted,deadline,status) VALUES(?,?,?,?,?,?,?,?)";
        Set<Integer> ids=new HashSet<>();
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            for(int i=1;i<rows.size();i++){
                List<String> r=rows.get(i);int row=i+1;if(r.size()!=8) throw new DataValidationException("service_requests.csv row "+row+" has "+r.size()+" columns");
                int id=DataValidator.integer(r.get(0),"requestId",row);DataValidator.positive(id,"requestId",row);DataValidator.uniqueId(id,ids,"requestId",row);
                int source=DataValidator.integer(r.get(1),"source",row), destination=DataValidator.integer(r.get(2),"destination",row);
                if(!locationIds.contains(source)||!locationIds.contains(destination)) throw new DataValidationException("Row "+row+": service request references a location that does not exist");
                String category=DataValidator.required(r.get(3),"category",row);int urgency=DataValidator.integer(r.get(4),"urgency",row);DataValidator.urgency(urgency,row);
                String submitted=DataValidator.required(r.get(5),"timeSubmitted",row);String deadline=r.get(6).isBlank()?null:r.get(6);String status=DataValidator.required(r.get(7),"status",row);
                ps.setInt(1,id);ps.setInt(2,source);ps.setInt(3,destination);ps.setString(4,category);ps.setInt(5,urgency);ps.setString(6,submitted);ps.setString(7,deadline);ps.setString(8,status);ps.addBatch();
            } ps.executeBatch();
        } return rows.size()-1;
    }

    private static int loadResources(Connection connection,String resource,Set<Integer> locationIds)throws Exception{
        List<List<String>> rows;try(InputStream in=open(resource)){rows=CsvParser.readAll(new InputStreamReader(in,StandardCharsets.UTF_8));}
        if(rows.isEmpty())throw new DataValidationException("resources.csv is empty");
        DataValidator.validateHeader(rows.get(0),"resourceId","type","homeLocation","capacity","availabilityStatus");
        String sql="INSERT INTO resources(resourceId,type,homeLocation,capacity,availabilityStatus) VALUES(?,?,?,?,?)";Set<Integer> ids=new HashSet<>();
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            for(int i=1;i<rows.size();i++){List<String> r=rows.get(i);int row=i+1;if(r.size()!=5)throw new DataValidationException("resources.csv row "+row+" has "+r.size()+" columns");
                int id=DataValidator.integer(r.get(0),"resourceId",row);DataValidator.positive(id,"resourceId",row);DataValidator.uniqueId(id,ids,"resourceId",row);String type=DataValidator.required(r.get(1),"type",row);int home=DataValidator.integer(r.get(2),"homeLocation",row);if(!locationIds.contains(home))throw new DataValidationException("Row "+row+": resource homeLocation does not exist");int cap=DataValidator.integer(r.get(3),"capacity",row);DataValidator.positive(cap,"capacity",row);String status=DataValidator.required(r.get(4),"availabilityStatus",row);
                ps.setInt(1,id);ps.setString(2,type);ps.setInt(3,home);ps.setInt(4,cap);ps.setString(5,status);ps.addBatch();
            }ps.executeBatch();
        }return rows.size()-1;
    }

    private static void logAudit(Connection connection,String type,String description)throws SQLException{
        try(PreparedStatement ps=connection.prepareStatement("INSERT INTO audit_events(eventType,description,eventTime) VALUES(?,?,datetime('now'))")){ps.setString(1,type);ps.setString(2,description);ps.executeUpdate();}
    }

    public record LoadReport(int locations,int roads,int serviceRequests,int resources){
        public int total(){return locations+roads+serviceRequests+resources;}
    }
}
