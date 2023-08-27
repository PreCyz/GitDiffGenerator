package pg.gipter.core.dao.configuration;

import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;

import java.util.Map;
import java.util.Optional;

public interface ConfigurationDao {

    void removeConfig(String configurationName);
    String[] loadArgumentArray(String configurationName);
    ToolkitConfig loadToolkitConfig();
    void saveToolkitConfig(ToolkitConfig toolkitConfig);
    ApplicationConfig loadApplicationConfig();
    Map<String, RunConfig> loadRunConfigMap();
    Optional<RunConfig> loadRunConfig(String configurationName);
    RunConfig getRunConfigFromArray(String[] args);
    void saveConfiguration(Configuration configuration);
}
