package pg.gipter.configuration;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoConstants;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.dto.NameSetting;
import pg.gipter.utils.PasswordUtils;
import pg.gipter.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ApplicationConfigurationDao implements ConfigurationDao {

    private final Logger logger = LoggerFactory.getLogger(ApplicationConfigurationDao.class);

    private final ConfigHelper configHelper;
    private JsonObject appSettings;

    ApplicationConfigurationDao() {
        this.configHelper = new ConfigHelper();
    }

    @Override
    public Optional<Properties> loadConfiguration(String configurationName) {
        Map<String, Properties> propertiesMap = loadAllConfigs();
        if (StringUtils.nullOrEmpty(configurationName)) {
            configurationName = ApplicationProperties.APPLICATION_PROPERTIES;
        }
        return Optional.ofNullable(propertiesMap.get(configurationName));
    }

    @Override
    public String[] loadArgumentArray(String configurationName) {
        Optional<Properties> properties = loadConfiguration(configurationName);
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
    public Properties createConfig(String[] args) {
        Properties properties = new Properties();
        for (String arg : args) {
            String key = arg.substring(0, arg.indexOf("="));
            String value = arg.substring(arg.indexOf("=") + 1);
            properties.setProperty(key, value);
        }
        return properties;
    }

    @Override
    public Map<String, Properties> loadAllConfigs() {
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
                if (toolkitConfig != null && toolkitConfig.has(ArgName.toolkitPassword.name())) {
                    setProperties(toolkitConfig, properties, ConfigHelper.TOOLKIT_CONFIG_PROPERTIES);
                    PasswordUtils.decryptPassword(properties, ArgName.toolkitPassword.name());
                }
                JsonObject runConfig = runConfigs.get(i).getAsJsonObject();
                setProperties(runConfig, properties, ConfigHelper.RUN_CONFIG_PROPERTIES);
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

    @Override
    public void saveRunConfig(Properties properties) {
        if (StringUtils.nullOrEmpty(properties.getProperty(ArgName.configurationName.name()))) {
            logger.warn("empty configurationName. Can not save run config without configurationName.");
            return;
        }
        PasswordUtils.encryptPassword(properties, ArgName.toolkitPassword.name());
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        }
        configHelper.addOrReplaceRunConfig(properties, jsonObject);
        logger.info("Saving run config [{}].", properties.getProperty(ArgName.configurationName.name()));
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
            appSettings = null;
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }

    @Override
    public JsonObject readJsonConfig() {
        if (appSettings == null) {
            try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
                 InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)
            ) {
                appSettings = new Gson().fromJson(reader, JsonObject.class);
            } catch (IOException | NullPointerException e) {
                logger.warn("Warning when loading {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            }
        }
        return appSettings;
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
                logger.info("Removing run config [{}].", configurationName);
                writeJsonConfig(jsonObject);
            } else {
                logger.error(errMsg);
                throw new IllegalStateException(errMsg);
            }
        }
    }

    @Override
    public void saveAppSettings(Properties properties) {
        PasswordUtils.encryptPassword(properties, ArgName.toolkitPassword.name());
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        }
        jsonObject.add(ConfigHelper.APP_CONFIG, configHelper.buildAppConfig(properties));
        logger.info("Saving application settings.");
        writeJsonConfig(jsonObject);
    }

    @Override
    public void saveToolkitSettings(Properties properties) {
        PasswordUtils.encryptPassword(properties, ArgName.toolkitPassword.name());
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = configHelper.buildFullJson(properties);
        }
        jsonObject.add(ConfigHelper.TOOLKIT_CONFIG, configHelper.buildToolkitConfig(properties));
        logger.info("Saving toolkit settings.");
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
                PasswordUtils.decryptPassword(result, ArgName.toolkitPassword.name());
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

    @Override
    public void removeFileNameSetting() {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject != null) {
            jsonObject.remove(ConfigHelper.FILE_NAME_SETTING);
            logger.info("File name settings removed.");
            writeJsonConfig(jsonObject);
        }
    }

    @Override
    public Optional<Properties> loadAppSettings() {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            return Optional.empty();
        }
        Properties appConfigProperties = new Properties();
        JsonObject appConfig = jsonObject.getAsJsonObject(ConfigHelper.APP_CONFIG);
        for (String key : ConfigHelper.APP_CONFIG_PROPERTIES) {
            appConfigProperties.put(key, appConfig.get(key).getAsString());
        }
        return Optional.of(appConfigProperties);
    }
}
