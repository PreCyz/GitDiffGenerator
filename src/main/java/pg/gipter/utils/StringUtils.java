package pg.gipter.utils;

public final class StringUtils {

    private StringUtils() { }

    public static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean nullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean getBoolean(String value) {
        String trimmedValue = value.trim();
        boolean result = Boolean.parseBoolean(trimmedValue);
        result |= "t".equalsIgnoreCase(trimmedValue);
        result |= "yes".equalsIgnoreCase(trimmedValue);
        result |= "y".equalsIgnoreCase(trimmedValue);
        return result;
    }

    public static String trimTo50(String value) {
        int maxPathLength = 50;
        if (value.length() > 50) {
            value = value.substring(0, maxPathLength) + "...";
        }
        return value;
    }
}
