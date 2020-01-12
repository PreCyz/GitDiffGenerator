package pg.gipter.utils;

import java.util.Properties;

public final class PropertiesUtils {
    private PropertiesUtils() {}

    public static String[] propertiesToArray(Properties properties) {
        if (properties == null) {
            return new String[]{};
        }
        return properties.entrySet()
                .stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .toArray(String[]::new);
    }
}
