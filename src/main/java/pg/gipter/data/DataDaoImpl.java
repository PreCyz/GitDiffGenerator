package pg.gipter.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoConstants;
import pg.gipter.job.upload.JobProperty;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;

class DataDaoImpl implements DataDao {

    private final Logger logger = LoggerFactory.getLogger(DataDaoImpl.class);

    private static final String DATA_PROPERTIES = "data.properties";

    @Override
    public Optional<Properties> loadDataProperties() {
        Properties properties;

        try (InputStream fis = new FileInputStream(DATA_PROPERTIES);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException | NullPointerException e) {
            logger.warn("Error when loading {}. Exception message is: {}", DATA_PROPERTIES, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    void saveProperties(Properties properties) {
        try (OutputStream os = new FileOutputStream(DATA_PROPERTIES);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            properties.store(writer, null);
            logger.info("File {} saved.", DATA_PROPERTIES);
        } catch (IOException | NullPointerException e) {
            logger.error("Error when saving {}.", DATA_PROPERTIES, e);
        }
    }

    @Override
    public void saveUploadStatus(String status) {
        Properties data = loadDataProperties().orElseGet(Properties::new);
        data.put(DaoConstants.UPLOAD_DATE_TIME_KEY, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        data.put(DaoConstants.UPLOAD_STATUS_KEY, status);
        saveProperties(data);
    }

    @Override
    public void saveNextUpload(String nextUploadDateTime) {
        Properties data = loadDataProperties().orElseGet(Properties::new);
        data.put(JobProperty.NEXT_FIRE_DATE.key(), nextUploadDateTime);
        saveProperties(data);
    }

    @Override
    public void saveDataProperties(Properties properties) {
        try (OutputStream os = new FileOutputStream(DATA_PROPERTIES);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            properties.store(writer, null);
            logger.info("File {} saved.", DATA_PROPERTIES);
        } catch (IOException | NullPointerException e) {
            logger.error("Error when saving {}.", DATA_PROPERTIES, e);
        }
    }

}
