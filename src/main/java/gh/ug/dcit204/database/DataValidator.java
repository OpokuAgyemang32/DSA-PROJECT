package gh.ug.dcit204.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DataValidator {
    private DataValidator() {}

    public static void validateHeader(List<String> actual, String... expected) throws DataValidationException {
        if (actual.size() != expected.length) {
            throw new DataValidationException("Expected " + expected.length + " columns but found " + actual.size());
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual.get(i))) {
                throw new DataValidationException("Column " + (i + 1) + " expected '" + expected[i] + "' but found '" + actual.get(i) + "'");
            }
        }
    }

    public static int integer(String value, String field, int row) throws DataValidationException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new DataValidationException("Row " + row + ": invalid integer in " + field + ": " + value);
        }
    }

    public static long longValue(String value, String field, int row) throws DataValidationException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new DataValidationException("Row " + row + ": invalid long in " + field + ": " + value);
        }
    }

    public static double decimal(String value, String field, int row) throws DataValidationException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new DataValidationException("Row " + row + ": invalid number in " + field + ": " + value);
        }
    }

    public static String required(String value, String field, int row) throws DataValidationException {
        if (value == null || value.isBlank()) {
            throw new DataValidationException("Row " + row + ": " + field + " cannot be empty");
        }
        return value;
    }

    public static void positive(int value, String field, int row) throws DataValidationException {
        if (value <= 0) throw new DataValidationException("Row " + row + ": " + field + " must be positive");
    }

    public static void nonNegative(double value, String field, int row) throws DataValidationException {
        if (value < 0) throw new DataValidationException("Row " + row + ": " + field + " cannot be negative");
    }

    public static void urgency(int value, int row) throws DataValidationException {
        if (value < 1 || value > 5) throw new DataValidationException("Row " + row + ": urgency must be between 1 and 5");
    }

    public static void uniqueId(int id, Set<Integer> ids, String field, int row) throws DataValidationException {
        if (!ids.add(id)) throw new DataValidationException("Row " + row + ": duplicate " + field + " " + id);
    }

    public static Set<Integer> locationIds(java.sql.Connection connection) throws java.sql.SQLException {
        Set<Integer> ids = new HashSet<>();
        try (var ps = connection.prepareStatement("SELECT locationId FROM locations"); var rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt(1));
        }
        return ids;
    }
}
