package pg.gipter.utils;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.job.JobProperty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertiesHelper {

    private final Logger logger = LoggerFactory.getLogger(PropertiesHelper.class);

    static final String APPLICATION_PROPERTIES = "application.properties";
    static final String UI_APPLICATION_PROPERTIES = "ui-application.properties";
    private static final String DATA_PROPERTIES = "data.properties";
    static final String APPLICATION_PROPERTIES_JSON = "applicationProperties.json";

    public static final String UPLOAD_STATUS_KEY = "lastUploadStatus";
    public static final String UPLOAD_DATE_TIME_KEY = "lastUploadDateTime";

    private Optional<Properties> loadApplicationProperties() {
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

    private Optional<Properties> loadUIApplicationProperties() {
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

    void saveProperties(Properties properties, String file) {
        try (OutputStream os = new FileOutputStream(file)) {
            properties.store(os, null);
            logger.info("File {} saved.", file);
        } catch (IOException | NullPointerException e) {
            logger.error("Error when saving {}.", file, e);
        }
    }

    void encryptPassword(Properties properties) {
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

    public void saveDataProperties(Properties properties) {
        saveProperties(properties, DATA_PROPERTIES);
    }

    public Optional<Properties> loadApplicationProperties(String configurationName) {
        Map<String, Properties> propertiesMap = loadAllApplicationProperties();
        if (StringUtils.nullOrEmpty(configurationName)) {
            configurationName = PropertiesHelper.APPLICATION_PROPERTIES;
        }
        return Optional.ofNullable(propertiesMap.get(configurationName));
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
                decryptPassword(properties);
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

        encryptPassword(properties);
        JsonObject jsonObject = new JsonObject();
        for (Object key : properties.keySet()) {
            String keyStr = String.valueOf(key);
            jsonObject.add(keyStr, new JsonPrimitive(properties.getProperty(keyStr)));
        }
        JsonArray jsonArray = readJsonConfig();
        int existingConfIdx = -1;
        if (jsonArray == null) {
            jsonArray = new JsonArray();
        } else {

            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonObject jObj = jsonArray.get(i).getAsJsonObject();
                String existingConfName = jObj.get(ArgName.configurationName.name()).getAsString();
                String newConfName = properties.getProperty(ArgName.configurationName.name());
                if (!StringUtils.nullOrEmpty(existingConfName) && existingConfName.equals(newConfName)) {
                    existingConfIdx = i;
                    break;
                }
            }
        }
        if (existingConfIdx > -1) {
            jsonArray.remove(existingConfIdx);
        }
        jsonArray.add(jsonObject);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonArray);
        try (FileWriter writer = new FileWriter(APPLICATION_PROPERTIES_JSON)) {
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

    public void convertPropertiesToNewFormat() {
        convertPropertiesToJson();
        deleteProperties(APPLICATION_PROPERTIES);
        deleteProperties(UI_APPLICATION_PROPERTIES);
    }

    private void convertPropertiesToJson() {
        boolean nothingToConvert = true;
        Optional<Properties> properties = loadApplicationProperties();
        if (properties.isPresent()) {
            Properties oldProperties = properties.get();
            oldProperties.put(ArgName.configurationName.name(), APPLICATION_PROPERTIES);
            addAndSaveApplicationProperties(oldProperties);
            nothingToConvert = false;
            logger.info("{} converted to JSON format.", APPLICATION_PROPERTIES);
        }
        properties = loadUIApplicationProperties();
        if (properties.isPresent()) {
            Properties oldProperties = properties.get();
            oldProperties.put(ArgName.configurationName.name(), UI_APPLICATION_PROPERTIES);
            addAndSaveApplicationProperties(oldProperties);
            nothingToConvert = false;
            logger.info("{} converted to JSON format.", UI_APPLICATION_PROPERTIES);
        }
        if (nothingToConvert) {
            logger.info("There is no old properties to convert to JSON format.");
        }
    }

    private void deleteProperties(String propertyFile) {
        try {
            Files.deleteIfExists(Paths.get(propertyFile));
        } catch (IOException e) {
            logger.warn("Can not delete {} file. {}", propertyFile, e.getMessage());
        }
    }
}
