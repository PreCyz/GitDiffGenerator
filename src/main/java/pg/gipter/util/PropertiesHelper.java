package pg.gipter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;

public class PropertiesHelper {

    private final Logger logger = LoggerFactory.getLogger(PropertiesHelper.class);

    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String UI_APPLICATION_PROPERTIES = "ui-application.properties";
    private static final String DATA_PROPERTIES = "data.properties";

    public static final String UPLOAD_STATUS_KEY = "lastUploadStatus";
    public static final String UPLOAD_DATE_TIME_KEY = "lastUploadDateTime";

    public Optional<Properties> loadApplicationProperties() {
        return loadProperties(APPLICATION_PROPERTIES);
    }

    public Optional<Properties> loadUIApplicationProperties() {
        return loadProperties(UI_APPLICATION_PROPERTIES);
    }

    private Optional<Properties> loadProperties(String fileName) {
        Properties properties;
        try (InputStream is = new FileInputStream(fileName)) {
            properties = new Properties();
            properties.load(is);
        } catch (IOException | NullPointerException e) {
            logger.warn("Error when loading {}. Exception message is: {}", fileName, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    public Optional<Properties> loadDataProperties() {
        return loadProperties(DATA_PROPERTIES);
    }

    private void saveProperties(Properties properties, String file) {
        try (OutputStream os = new FileOutputStream(file)) {
            properties.store(os, null);
            logger.info("File {} saved.", file);
        } catch (IOException | NullPointerException e) {
            logger.error("Error when saving {}.", file, e);
        }
    }

    public void saveToApplicationProperties(Properties properties) {
        saveProperties(properties, APPLICATION_PROPERTIES);
    }

    public void saveUploadInfo(String status) {
        Properties data = loadDataProperties().orElseGet(Properties::new);
        data.put(PropertiesHelper.UPLOAD_DATE_TIME_KEY, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        data.put(PropertiesHelper.UPLOAD_STATUS_KEY, status);
        saveProperties(data, DATA_PROPERTIES);
    }

    public void saveToUIApplicationProperties(Properties properties) {
        saveProperties(properties, UI_APPLICATION_PROPERTIES);
    }

    public void saveDataProperties(Properties properties) {
        saveProperties(properties, DATA_PROPERTIES);
    }

}
