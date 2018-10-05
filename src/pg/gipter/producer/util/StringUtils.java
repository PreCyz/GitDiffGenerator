package pg.gipter.producer.util;

public final class StringUtils {

    private StringUtils() { }

    public static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean getBoolean(String value) {
        boolean result = Boolean.valueOf(value);
        result |= "t".equalsIgnoreCase(value);
        result |= "yes".equalsIgnoreCase(value);
        result |= "y".equalsIgnoreCase(value);
        return result;
    }
}
