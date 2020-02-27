package pg.gipter.core.dao.configuration;

import org.springframework.util.CollectionUtils;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producer.command.ItemType;
import pg.gipter.utils.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ApplicationConfiguration extends ApplicationJsonReader implements ConfigurationDao {

    private ApplicationConfiguration() {
        super();
    }

    private static class ApplicationConfigurationHolder {
        private static final ApplicationConfiguration INSTANCE = new ApplicationConfiguration();
    }

    public static ApplicationConfiguration getInstance() {
        return ApplicationConfigurationHolder.INSTANCE;
    }

    @Override
    public void saveRunConfig(RunConfig runConfig) {
        Configuration configuration = readJsonConfig();
        if (configuration.getRunConfigs() == null) {
            configuration.addRunConfig(runConfig);
            logger.info("New configuration [{}] added.", runConfig.getConfigurationName());
        } else {
            List<RunConfig> runConfigs = configuration.getRunConfigs();
            if (runConfigs == null) {
                runConfigs = new ArrayList<>();
            }
            boolean runConfigExists = runConfigs.stream()
                    .anyMatch(rc -> rc.getConfigurationName().equals(runConfig.getConfigurationName()));
            if (runConfigExists) {
                runConfigs = runConfigs.stream()
                        .filter(rc -> !rc.getConfigurationName().equals(runConfig.getConfigurationName()))
                        .collect(toList());
                runConfigs.add(runConfig);
                configuration.setRunConfigs(runConfigs);
                logger.info("Existing configuration [{}] updated.", runConfig.getConfigurationName());
            } else {
                configuration.addRunConfig(runConfig);
                logger.info("New configuration [{}] added to existing set.", runConfig.getConfigurationName());
            }
        }
        writeJsonConfig(configuration, RunConfig.class);
    }

    @Override
    public ToolkitConfig loadToolkitConfig() {
        Configuration configuration = readJsonConfig();
        return Optional.ofNullable(configuration.getToolkitConfig()).orElseGet(ToolkitConfig::new);
    }

    @Override
    public void saveToolkitConfig(ToolkitConfig toolkitConfig) {
        Configuration configuration = readJsonConfig();
        configuration.setToolkitConfig(toolkitConfig);
        writeJsonConfig(configuration, ToolkitConfig.class);
    }

    @Override
    public ApplicationConfig loadApplicationConfig() {
        Configuration configuration = readJsonConfig();
        return Optional.ofNullable(configuration.getAppConfig()).orElseGet(ApplicationConfig::new);
    }

    @Override
    public void saveApplicationConfig(ApplicationConfig applicationConfig) {
        Configuration configuration = readJsonConfig();
        configuration.setAppConfig(applicationConfig);
        writeJsonConfig(configuration, ApplicationConfig.class);
    }

    @Override
    public Map<String, RunConfig> loadRunConfigMap() {
        Map<String, RunConfig> runConfigMap = new HashMap<>();
        Configuration configuration = readJsonConfig();
        boolean useDefault = false;
        if (configuration.getRunConfigs() != null) {
            runConfigMap = configuration.getRunConfigs().stream().collect(toMap(RunConfig::getConfigurationName, rc -> rc));
        } else {
            useDefault = true;
        }
        if (useDefault) {
            RunConfig runConfig = new RunConfig();
            runConfigMap.put(runConfig.getConfigurationName(), runConfig);
        }
        return runConfigMap.entrySet()
                .stream()
                .filter(entry -> !ArgName.configurationName.defaultValue().equals(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String[] loadArgumentArray(String configurationName) {
        Collection<String> arguments = new LinkedHashSet<>();
        Optional<RunConfig> runConfig = loadRunConfig(configurationName);
        runConfig.ifPresent(config -> arguments.addAll(Arrays.asList(config.toArgumentArray())));
        ApplicationConfig applicationConfig = loadApplicationConfig();
        if (applicationConfig != null) {
            arguments.addAll(Arrays.asList(applicationConfig.toArgumentArray()));
        }
        ToolkitConfig toolkitConfig = loadToolkitConfig();
        if (toolkitConfig != null) {
            arguments.addAll(Arrays.asList(toolkitConfig.toArgumentArray()));
        }
        return arguments.toArray(new String[0]);
    }

    @Override
    public void removeConfig(String configurationName) {
        Configuration configuration = readJsonConfig();
        if (!CollectionUtils.isEmpty(configuration.getRunConfigs())) {
            configuration.removeRunConfig(configurationName);
            logger.info("Removing run config [{}].", configurationName);
            writeJsonConfig(configuration, ApplicationConfig.class);
        } else {
            String errMsg = "Can not remove the configuration, because it does not exists!";
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        }
    }

    @Override
    public Optional<RunConfig> loadRunConfig(String configurationName) {
        Map<String, RunConfig> runConfigMap = loadRunConfigMap();
        return Optional.ofNullable(runConfigMap.get(configurationName));
    }

    @Override
    public RunConfig getRunConfigFromArray(String[] args) {
        RunConfig runConfig = new RunConfig();
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length > 1) {
                if (ArgName.author.name().equals(split[0])) {
                    runConfig.setAuthor(split[1]);
                } else if (ArgName.gitAuthor.name().equals(split[0])) {
                    runConfig.setGitAuthor(split[1]);
                } else if (ArgName.mercurialAuthor.name().equals(split[0])) {
                    runConfig.setMercurialAuthor(split[1]);
                } else if (ArgName.svnAuthor.name().equals(split[0])) {
                    runConfig.setSvnAuthor(split[1]);
                } else if (ArgName.committerEmail.name().equals(split[0])) {
                    runConfig.setCommitterEmail(split[1]);
                } else if (ArgName.itemType.name().equals(split[0])) {
                    runConfig.setItemType(ItemType.valueFor(split[1]));
                } else if (ArgName.skipRemote.name().equals(split[0])) {
                    runConfig.setSkipRemote(StringUtils.getBoolean(split[1]));
                } else if (ArgName.fetchAll.name().equals(split[0])) {
                    runConfig.setFetchAll(StringUtils.getBoolean(split[1]));
                } else if (ArgName.itemPath.name().equals(split[0])) {
                    runConfig.setItemPath(split[1]);
                } else if (ArgName.projectPath.name().equals(split[0])) {
                    runConfig.setProjectPath(split[1]);
                } else if (ArgName.itemFileNamePrefix.name().equals(split[0])) {
                    runConfig.setItemFileNamePrefix(split[1]);
                } else if (ArgName.periodInDays.name().equals(split[0])) {
                    runConfig.setPeriodInDays(Integer.valueOf(split[1]));
                } else if (ArgName.startDate.name().equals(split[0])) {
                    runConfig.setStartDate(LocalDate.parse(split[1], DateTimeFormatter.ISO_DATE));
                } else if (ArgName.endDate.name().equals(split[0])) {
                    runConfig.setEndDate(LocalDate.parse(split[1], DateTimeFormatter.ISO_DATE));
                } else if (ArgName.configurationName.name().equals(split[0])) {
                    runConfig.setConfigurationName(split[1]);
                } else if (ArgName.toolkitProjectListNames.name().equals(split[0])) {
                    runConfig.setToolkitProjectListNames(split[1]);
                } else if (ArgName.deleteDownloadedFiles.name().equals(split[0])) {
                    runConfig.setDeleteDownloadedFiles(StringUtils.getBoolean(split[1]));
                }
            }
        }
        return runConfig;
    }
}
