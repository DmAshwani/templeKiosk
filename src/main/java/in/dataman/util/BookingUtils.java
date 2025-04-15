package in.dataman.util;

public class BookingUtils {
    public static Integer getInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue(); // handles Short, Integer, Long, etc.
        }
        return 0; // default to 0 if null or not a number
    }

    public static Long getLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0L;
    }

    public static String getString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}

