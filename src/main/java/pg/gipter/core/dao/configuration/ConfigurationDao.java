package pg.gipter.core.dao.configuration;

import pg.gipter.core.dto.ApplicationConfig;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.core.dto.ToolkitConfig;

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
