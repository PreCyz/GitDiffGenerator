package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dto.ApplicationConfig;
import pg.gipter.core.dto.Configuration;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.core.producer.command.UploadType;
import pg.gipter.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ApplicationConfigurationDao implements ConfigurationDao {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfigurationDao.class);
    private final JsonSerializer<Configuration> passwordSerializer;
    private final JsonDeserializer<Configuration> passwordDeserializer;

    private Configuration configuration;

    public ApplicationConfigurationDao() {
        passwordSerializer = new PasswordSerializer();
        passwordDeserializer = new PasswordDeserializer();
    }

    @Override
    public void saveRunConfig(RunConfig runConfig) {
        Configuration conf = readJsonConfig();
        if (conf == null) {
            conf = new Configuration();
            conf.addRunConfig(runConfig);
            logger.info("New configuration [{}] added.", runConfig.getConfigurationName());
        } else {
            List<RunConfig> runConfigs = conf.getRunConfigs();
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
                conf.setRunConfigs(runConfigs);
                logger.info("Existing configuration [{}] updated.", runConfig.getConfigurationName());
            } else {
                conf.addRunConfig(runConfig);
                logger.info("New configuration [{}] added to existing set.", runConfig.getConfigurationName());
            }
        }
        writeJsonConfig(conf, RunConfig.class);
    }

    Configuration readJsonConfig() {
        if (configuration == null) {
            try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
                 InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)
            ) {
                configuration = new GsonBuilder()
                        .registerTypeAdapter(Configuration.class, passwordDeserializer)
                        .create()
                        .fromJson(reader, Configuration.class);
            } catch (IOException | NullPointerException e) {
                logger.warn("Warning when loading {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            }
        }
        return configuration;
    }

    void writeJsonConfig(Configuration configuration, Class<?> clazz) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Configuration.class, passwordSerializer)
                .create();
        String json = gson.toJson(configuration, Configuration.class);
        try (OutputStream os = new FileOutputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
            logger.info("File {} updated with {}.", DaoConstants.APPLICATION_PROPERTIES_JSON, clazz.getSimpleName());
            this.configuration = configuration;
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }

    @Override
    public ToolkitConfig loadToolkitConfig() {
        Configuration configuration = readJsonConfig();
        ToolkitConfig result = new ToolkitConfig();
        if (configuration != null) {
            result = Optional.ofNullable(configuration.getToolkitConfig()).orElseGet(ToolkitConfig::new);
        }
        return result;
    }

    @Override
    public void saveToolkitConfig(ToolkitConfig toolkitConfig) {
        Configuration configuration = readJsonConfig();
        if (configuration == null) {
            configuration = new Configuration();
        }
        configuration.setToolkitConfig(toolkitConfig);
        writeJsonConfig(configuration, ToolkitConfig.class);
    }

    @Override
    public ApplicationConfig loadApplicationConfig() {
        Configuration configuration = readJsonConfig();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        if (configuration != null) {
            applicationConfig = configuration.getAppConfig();
        }
        return applicationConfig;
    }

    @Override
    public void saveApplicationConfig(ApplicationConfig applicationConfig) {
        Configuration configuration = readJsonConfig();
        if (configuration == null) {
            configuration = new Configuration();
        }
        configuration.setAppConfig(applicationConfig);
        writeJsonConfig(configuration, ApplicationConfig.class);
    }

    @Override
    public Map<String, RunConfig> loadRunConfigMap() {
        Map<String, RunConfig> runConfigMap = new HashMap<>();
        Configuration configuration = readJsonConfig();
        boolean useDefault = false;
        if (configuration != null) {
            if (configuration.getRunConfigs() != null) {
                runConfigMap = configuration.getRunConfigs().stream().collect(toMap(RunConfig::getConfigurationName, rc -> rc));
            } else {
                useDefault = true;
            }
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
        final String errMsg = "Can not remove the configuration, because it does not exists!";
        Configuration configuration = readJsonConfig();
        if (configuration == null) {
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        } else {


            if (!configuration.getRunConfigs().isEmpty()) {
                configuration.removeRunConfig(configurationName);
                logger.info("Removing run config [{}].", configurationName);
                writeJsonConfig(configuration, ApplicationConfig.class);
            } else {
                logger.error(errMsg);
                throw new IllegalStateException(errMsg);
            }
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
                } else if (ArgName.uploadType.name().equals(split[0])) {
                    runConfig.setUploadType(UploadType.valueFor(split[1]));
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
