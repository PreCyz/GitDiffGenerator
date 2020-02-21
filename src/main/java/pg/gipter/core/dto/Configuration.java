package pg.gipter.core.dto;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration {
    public static final String TOOLKIT_CONFIG = "toolkitConfig";
    private ApplicationConfig appConfig;
    private List<RunConfig> runConfigs;
    private ToolkitConfig toolkitConfig;

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
        runConfigs.add(runConfig);
    }

    public void removeRunConfig(String configurationName) {
        if (runConfigs != null && configurationName != null) {
            runConfigs = runConfigs.stream()
                    .filter(rc -> !rc.getConfigurationName().equals(configurationName))
                    .collect(Collectors.toList());
        }
    }
}
