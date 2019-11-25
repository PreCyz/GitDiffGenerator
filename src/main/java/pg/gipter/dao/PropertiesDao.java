package pg.gipter.dao;

import com.google.gson.JsonObject;
import pg.gipter.settings.dto.NameSetting;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public interface PropertiesDao {

    Optional<Properties> loadApplicationProperties();
    Optional<Properties> loadUIApplicationProperties();

    void saveProperties(Properties properties, String file);

    Optional<Properties> loadApplicationProperties(String configurationName);
    String[] loadArgumentArray(String configurationName);
    Properties createProperties(String[] args);
    Map<String, Properties> loadAllApplicationProperties();
    void saveRunConfig(Properties properties);
    JsonObject readJsonConfig();
    void removeConfig(String configurationName);
    void buildAndSaveJsonConfig(Properties properties, String applicationProperties);
    void saveAppSettings(Properties properties);
    void saveToolkitSettings(Properties properties);
    Properties loadToolkitCredentials();
    void saveFileNameSetting(NameSetting fileNameSetting);
    Optional<NameSetting> loadFileNameSetting();
    void removeFileNameSetting();
}
