package pg.gipter.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

class PropertiesLoader {

    static final String APPLICATION_PROPERTIES = "application.properties";

    Optional<Properties> loadPropertiesFromFile() {
        Properties properties;
        try (InputStream is = new FileInputStream(APPLICATION_PROPERTIES)) {
            properties = new Properties();
            properties.load(is);
        } catch (IOException | NullPointerException e) {
            properties = null;
        }
        return Optional.ofNullable(properties);
    }
}
