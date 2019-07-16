package pg.gipter.utils;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.job.JobProperty;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertiesHelper {

    private final Logger logger = LoggerFactory.getLogger(PropertiesHelper.class);

    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String UI_APPLICATION_PROPERTIES = "ui-application.properties";
    private static final String DATA_PROPERTIES = "data.properties";
    static final String APPLICATION_PROPERTIES_JSON = "applicationProperties.json";

    public static final String UPLOAD_STATUS_KEY = "lastUploadStatus";
    public static final String UPLOAD_DATE_TIME_KEY = "lastUploadDateTime";

    public Optional<Properties> loadApplicationProperties() {
        Optional<Properties> properties = loadProperties(APPLICATION_PROPERTIES);
        properties.ifPresent(this::decryptPassword);
        return properties;
    }

    private void decryptPassword(Properties properties) {
        if (properties.containsKey(ArgName.toolkitPassword.name())) {
            try {
                properties.replace(
                        ArgName.toolkitPassword.name(),
                        CryptoUtils.decrypt(properties.getProperty(ArgName.toolkitPassword.name()))
                );
            } catch (GeneralSecurityException e) {
                logger.warn("Can not decode property. {}", e.getMessage(), e);
            }
        }
    }

    public Optional<Properties> loadUIApplicationProperties() {
        Optional<Properties> properties = loadProperties(UI_APPLICATION_PROPERTIES);
        properties.ifPresent(this::decryptPassword);
        return properties;
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
        encryptPassword(properties);
        saveProperties(properties, APPLICATION_PROPERTIES);
    }

    private void encryptPassword(Properties properties) {
        if (properties.containsKey(ArgName.toolkitPassword.name())) {
            try {
                properties.replace(
                        ArgName.toolkitPassword.name(),
                        CryptoUtils.encrypt(properties.getProperty(ArgName.toolkitPassword.name()))
                );
            } catch (GeneralSecurityException e) {
                logger.warn("Can not decode property. {}", e.getMessage());
            }
        }
    }

    public void saveUploadStatus(String status) {
        Properties data = loadDataProperties().orElseGet(Properties::new);
        data.put(PropertiesHelper.UPLOAD_DATE_TIME_KEY, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        data.put(PropertiesHelper.UPLOAD_STATUS_KEY, status);
        saveProperties(data, DATA_PROPERTIES);
    }

    public void saveNextUpload(String nextUploadDateTime) {
        Properties data = loadDataProperties().orElseGet(Properties::new);
        data.put(JobProperty.NEXT_FIRE_DATE.value(), nextUploadDateTime);
        saveProperties(data, DATA_PROPERTIES);
    }

    public void saveToUIApplicationProperties(Properties properties) {
        encryptPassword(properties);
        saveProperties(properties, UI_APPLICATION_PROPERTIES);
    }

    public void saveDataProperties(Properties properties) {
        saveProperties(properties, DATA_PROPERTIES);
    }

    public Map<String, Properties> loadAllApplicationProperties() {
        Map<String, Properties> result = new HashMap<>();
        JsonArray jsonArray = readJsonConfig();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); ++i) {
                Properties properties = new Properties();
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                for (ArgName argName : ArgName.values()) {
                    String value = jsonObject.get(argName.name()).getAsString();
                    if (!StringUtils.nullOrEmpty(value)) {
                        properties.put(argName.name(), value);
                    }
                }
                result.put(properties.getProperty(ArgName.configurationName.name()), properties);
            }
        }
        return result;
    }

    public void addAndSaveApplicationProperties(Properties properties) {
        if (properties == null || properties.keySet().isEmpty()) {
            logger.error("Properties does not contain any values.");
            throw new IllegalArgumentException("Properties does not contain any values.");
        }

        JsonObject jsonObject = new JsonObject();
        for (Object key : properties.keySet()) {
            String keyStr = String.valueOf(key);
            jsonObject.add(keyStr, new JsonPrimitive(properties.getProperty(keyStr)));
        }
        JsonArray jsonArray = readJsonConfig();
        if (jsonArray == null) {
            jsonArray = new JsonArray();
        }
        jsonArray.add(jsonObject);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonArray);
        try (FileWriter writer = new FileWriter(APPLICATION_PROPERTIES_JSON)){
            writer.write(json);
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}", APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }

    }

    JsonArray readJsonConfig() {
        try (FileReader fr = new FileReader(APPLICATION_PROPERTIES_JSON);
             BufferedReader reader = new BufferedReader(fr)) {
            return new Gson().fromJson(reader, JsonArray.class);
        } catch (IOException | NullPointerException e) {
            logger.warn("Warning when loading {}. Exception message is: {}", APPLICATION_PROPERTIES_JSON, e.getMessage());
        }
        return null;
    }
}
