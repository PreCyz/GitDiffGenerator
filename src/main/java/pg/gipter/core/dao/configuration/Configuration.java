package pg.gipter.core.dao.configuration;

import pg.gipter.core.dto.ApplicationConfig;
import pg.gipter.core.dto.CipherDetails;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.core.dto.ToolkitConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class Configuration {
    private ApplicationConfig appConfig;
    private List<RunConfig> runConfigs;
    private ToolkitConfig toolkitConfig;
    private CipherDetails cipherDetails;

    ApplicationConfig getAppConfig() {
        return appConfig;
    }

    void setAppConfig(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    List<RunConfig> getRunConfigs() {
        return runConfigs;
    }

    void setRunConfigs(List<RunConfig> runConfigs) {
        this.runConfigs = runConfigs;
    }

    ToolkitConfig getToolkitConfig() {
        return toolkitConfig;
    }

    void setToolkitConfig(ToolkitConfig toolkitConfig) {
        this.toolkitConfig = toolkitConfig;
    }

    void addRunConfig(RunConfig runConfig) {
        if (runConfigs == null) {
            runConfigs = new LinkedList<>();
        }
        runConfigs.add(runConfig);
    }

    void removeRunConfig(String configurationName) {
        if (runConfigs != null && configurationName != null) {
            runConfigs = runConfigs.stream()
                    .filter(rc -> !rc.getConfigurationName().equals(configurationName))
                    .collect(Collectors.toList());
        }
    }

    public CipherDetails getCipherDetails() {
        return cipherDetails;
    }

    public void setCipherDetails(CipherDetails cipherDetails) {
        this.cipherDetails = cipherDetails;
    }
}
