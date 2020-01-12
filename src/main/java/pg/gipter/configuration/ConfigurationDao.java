package pg.gipter.configuration;

import com.google.gson.JsonObject;
import pg.gipter.settings.dto.NameSetting;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public interface ConfigurationDao {

    Map<String, Properties> loadAllConfigs();
    Optional<Properties> loadConfiguration(String configurationName);
    Properties createConfig(String[] args);
    void saveRunConfig(Properties properties);
    void removeConfig(String configurationName);
    String[] loadArgumentArray(String configurationName);
    JsonObject readJsonConfig();
    void saveAppSettings(Properties properties);
    Optional<Properties> loadAppSettings();
    void saveToolkitSettings(Properties properties);
    Properties loadToolkitCredentials();
    void saveFileNameSetting(NameSetting fileNameSetting);
    Optional<NameSetting> loadFileNameSetting();
    void removeFileNameSetting();
}
