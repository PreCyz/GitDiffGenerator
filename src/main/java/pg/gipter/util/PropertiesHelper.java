package pg.gipter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

public class PropertiesHelper {

    private final Logger logger = LoggerFactory.getLogger(PropertiesHelper.class);

    public static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String UI_APPLICATION_PROPERTIES = "ui-application.properties";

    public Optional<Properties> loadPropertiesFromFile() {
        Properties properties;
        try (InputStream is = new FileInputStream(APPLICATION_PROPERTIES)) {
            properties = new Properties();
            properties.load(is);
        } catch (IOException | NullPointerException e) {
            logger.warn("Error when loading {}. Exception message is {}", APPLICATION_PROPERTIES, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    private void saveProperties(Properties properties, String file) {
        try (OutputStream os = new FileOutputStream(file)) {
            properties.store(os, null);
        } catch (IOException | NullPointerException e) {
            logger.error("Error when saving {}.", file, e);
        }
    }

    public void saveToApplicationProperties(Properties properties) {
        saveProperties(properties, APPLICATION_PROPERTIES);
    }

    public void saveToUIApplicationProperties(Properties properties) {
        saveProperties(properties, UI_APPLICATION_PROPERTIES);
    }

}
