package pg.gipter.core.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration {
    private ApplicationConfig appConfig;
    private List<RunConfig> runConfigs;
    private ToolkitConfig toolkitConfig;
    private CipherDetails cipherDetails;

    public Configuration() { }

    public Configuration(ApplicationConfig appConfig, ToolkitConfig toolkitConfig, List<RunConfig> runConfigs,
                         CipherDetails cipherDetails) {
        this.appConfig = appConfig;
        this.runConfigs = runConfigs;
        this.toolkitConfig = toolkitConfig;
        this.cipherDetails = cipherDetails;
    }

    public ApplicationConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    public List<RunConfig> getRunConfigs() {
        return runConfigs;
    }

    public void setRunConfigs(List<RunConfig> runConfigs) {
        this.runConfigs = runConfigs;
    }

    public ToolkitConfig getToolkitConfig() {
        return toolkitConfig;
    }

    public void setToolkitConfig(ToolkitConfig toolkitConfig) {
        this.toolkitConfig = toolkitConfig;
    }

    public void addRunConfig(RunConfig runConfig) {
        if (runConfigs == null) {
            runConfigs = new LinkedList<>();
        }
        runConfigs = runConfigs.stream()
                .filter(rc -> !rc.getConfigurationName().equals(runConfig.getConfigurationName()))
                .collect(Collectors.toCollection(LinkedList::new));
        runConfigs.add(runConfig);
    }

    public void removeRunConfig(String configurationName) {
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
