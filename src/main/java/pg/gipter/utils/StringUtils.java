package pg.gipter.utils;

import java.nio.charset.StandardCharsets;

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

    public static String utf8(String value) {
        try {
            return new String(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return value;
        }
    }
}
