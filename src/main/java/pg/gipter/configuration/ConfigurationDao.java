package pg.gipter.configuration;

import pg.gipter.settings.dto.ApplicationConfig;
import pg.gipter.settings.dto.RunConfig;
import pg.gipter.settings.dto.ToolkitConfig;

import java.util.Map;
import java.util.Optional;

public interface ConfigurationDao {

    void saveRunConfig(RunConfig runConfig);
    void removeConfig(String configurationName);
    String[] loadArgumentArray(String configurationName);
    ToolkitConfig loadToolkitConfig();
    void saveToolkitConfig(ToolkitConfig toolkitConfig);
    ApplicationConfig loadApplicationConfig();
    void saveApplicationConfig(ApplicationConfig applicationConfig);
    Map<String, RunConfig> loadRunConfigMap();
    Optional<RunConfig> loadRunConfig(String configurationName);
    RunConfig getRunConfigFromArray(String[] args);
}
