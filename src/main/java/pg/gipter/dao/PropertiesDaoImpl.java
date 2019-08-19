package pg.gipter.dao;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.dto.NameSetting;
import pg.gipter.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

class PropertiesDaoImpl implements PropertiesDao {

    private final Logger logger = LoggerFactory.getLogger(PropertiesDaoImpl.class);

    static final String APPLICATION_PROPERTIES = "application.properties";
    static final String UI_APPLICATION_PROPERTIES = "ui-application.properties";

    private final ConfigHelper configHelper;

    PropertiesDaoImpl() {
        this.configHelper = new ConfigHelper();
    }

    Optional<Properties> loadApplicationProperties() {
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

    Optional<Properties> loadUIApplicationProperties() {
        Optional<Properties> properties = loadProperties(UI_APPLICATION_PROPERTIES);
        properties.ifPresent(this::decryptPassword);
        return properties;
    }

    private Optional<Properties> loadProperties(String fileName) {
        Properties properties;

        try (InputStream fis = new FileInputStream(fileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException | NullPointerException e) {
            logger.warn("Error when loading {}. Exception message is: {}", fileName, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    void saveProperties(Properties properties, String file) {
        try (OutputStream os = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            properties.store(writer, null);
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

    @Override
    public Optional<Properties> loadApplicationProperties(String configurationName) {
        Map<String, Properties> propertiesMap = loadAllApplicationProperties();
        if (StringUtils.nullOrEmpty(configurationName)) {
            configurationName = PropertiesDaoImpl.APPLICATION_PROPERTIES;
        }
        return Optional.ofNullable(propertiesMap.get(configurationName));
    }

    @Override
    public String[] loadArgumentArray(String configurationName) {
        Optional<Properties> properties = loadApplicationProperties(configurationName);
        String[] result = new String[0];
        if (properties.isPresent()) {
            result = new String[properties.get().size()];
            int idx = 0;
            for (Object keyObj : properties.get().keySet()) {
                String key = String.valueOf(keyObj);
                result[idx++] = String.format("%s=%s", key, properties.get().getProperty(key));
            }
        }
        return result;
    }

    @Override
    public Properties createProperties(String[] args) {
        Properties properties = new Properties();
        for (String arg : args) {
            String key = arg.substring(0, arg.indexOf("="));
            String value = arg.substring(arg.indexOf("=") + 1);
            properties.setProperty(key, value);
        }
        return properties;
    }

    @Override
    public Map<String, Properties> loadAllApplicationProperties() {
        Map<String, Properties> result = new LinkedHashMap<>();
        JsonObject config = readJsonConfig();
        if (config != null) {
            JsonObject appConfig = config.getAsJsonObject(ConfigHelper.APP_CONFIG);
            JsonObject toolkitConfig = config.getAsJsonObject(ConfigHelper.TOOLKIT_CONFIG);
            JsonArray runConfigs = config.getAsJsonArray(ConfigHelper.RUN_CONFIGS);
            for (int i = 0; runConfigs != null && i < runConfigs.size(); ++i) {
                Properties properties = new Properties();
                if (appConfig != null) {
                    setProperties(appConfig, properties, ConfigHelper.APP_CONFIG_PROPERTIES);
                }
                if (toolkitConfig != null) {
                    setProperties(toolkitConfig, properties, ConfigHelper.TOOLKIT_CONFIG_PROPERTIES);
                }
                JsonObject runConfig = runConfigs.get(i).getAsJsonObject();
                setProperties(runConfig, properties, ConfigHelper.RUN_CONFIG_PROPERTIES);
                decryptPassword(properties);
                result.put(properties.getProperty(ArgName.configurationName.name()), properties);
            }
        }
        return result;
    }

    private void setProperties(JsonObject jsonObject, Properties properties, Set<String> propertiesNameSet) {
        for (String propertyName : propertiesNameSet) {
            JsonElement jsonElement = jsonObject.get(propertyName);
            if (jsonElement != null) {
                properties.put(propertyName, jsonElement.getAsString());
            }
        }
    }

    private JsonObject buildJsonConfig(Properties properties) {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        } else {
            jsonObject.add(ConfigHelper.APP_CONFIG, configHelper.buildAppConfig(properties));
            jsonObject.add(ConfigHelper.TOOLKIT_CONFIG, configHelper.buildToolkitConfig(properties));
            configHelper.addOrReplaceRunConfig(properties, jsonObject);
        }
        return jsonObject;
    }

    @Override
    public void saveRunConfig(Properties properties) {
        if (StringUtils.nullOrEmpty(properties.getProperty(ArgName.configurationName.name()))) {
            logger.warn("empty configurationName. Can not save run config without configurationName.");
            return;
        }
        encryptPassword(properties);
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        }
        configHelper.addOrReplaceRunConfig(properties, jsonObject);
        writeJsonConfig(jsonObject);
    }

    void writeJsonConfig(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonObject);
        try (OutputStream os = new FileOutputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
            logger.info("File {} saved.", DaoConstants.APPLICATION_PROPERTIES_JSON);
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }

    JsonObject readJsonConfig() {
        try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException | NullPointerException e) {
            logger.warn("Warning when loading {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
        }
        return null;
    }

    @Override
    public void removeConfig(String configurationName) {
        final String errMsg = "Can not remove the configuration, because it does not exists!";
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        } else {
            JsonElement jsonElement = jsonObject.get(ConfigHelper.RUN_CONFIGS);

            int existingConfIdx = configHelper.getIndexOfExistingConfig(jsonElement, configurationName);
            if (existingConfIdx > ConfigHelper.NO_CONFIGURATION_FOUND) {
                JsonArray runConfigs = jsonElement.getAsJsonArray();
                runConfigs.remove(existingConfIdx);
                jsonObject.add(ConfigHelper.RUN_CONFIGS, runConfigs);
                writeJsonConfig(jsonObject);
            } else {
                logger.error(errMsg);
                throw new IllegalStateException(errMsg);
            }
        }
    }

    void buildAndSaveJsonConfig(Properties properties, String applicationProperties) {
        properties.put(ArgName.configurationName.name(), applicationProperties);
        encryptPassword(properties);
        JsonObject jsonObject = buildJsonConfig(properties);
        writeJsonConfig(jsonObject);
        logger.info("{} converted to JSON format.", applicationProperties);
    }

    @Override
    public void saveAppSettings(Properties properties) {
        encryptPassword(properties);
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        }
        jsonObject.add(ConfigHelper.APP_CONFIG, configHelper.buildAppConfig(properties));
        writeJsonConfig(jsonObject);
    }

    @Override
    public void saveToolkitSettings(Properties properties) {
        encryptPassword(properties);
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        }
        jsonObject.add(ConfigHelper.TOOLKIT_CONFIG, configHelper.buildToolkitConfig(properties));
        writeJsonConfig(jsonObject);
    }

    @Override
    public Properties loadToolkitCredentials() {
        JsonObject jsonObject = readJsonConfig();
        Properties result = new Properties();
        if (jsonObject == null) {
            result.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
            result.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
        } else {
            JsonObject toolkitConfig = jsonObject.getAsJsonObject(ConfigHelper.TOOLKIT_CONFIG);
            if (toolkitConfig == null) {
                result.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
                result.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
            } else {
                result.setProperty(ArgName.toolkitUsername.name(), toolkitConfig.get(ArgName.toolkitUsername.name()).getAsString());
                result.setProperty(ArgName.toolkitPassword.name(), toolkitConfig.get(ArgName.toolkitPassword.name()).getAsString());
                decryptPassword(result);
            }
        }
        return result;
    }

    @Override
    public void saveFileNameSetting(NameSetting fileNameSetting) {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }
        jsonObject.add(ConfigHelper.FILE_NAME_SETTING, new Gson().toJsonTree(fileNameSetting));
        logger.info("Saving file name settings {}", fileNameSetting.getNameSettings());
        writeJsonConfig(jsonObject);
    }

    @Override
    public Optional<NameSetting> loadFileNameSetting() {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            return Optional.empty();
        }
        JsonElement jsonElement = jsonObject.get(ConfigHelper.FILE_NAME_SETTING);
        return Optional.ofNullable(new Gson().fromJson(jsonElement, NameSetting.class));
    }
}
