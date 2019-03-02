package pg.gipter.util;

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
        boolean result = Boolean.valueOf(trimmedValue);
        result |= "t".equalsIgnoreCase(trimmedValue);
        result |= "yes".equalsIgnoreCase(trimmedValue);
        result |= "y".equalsIgnoreCase(trimmedValue);
        return result;
    }
}
