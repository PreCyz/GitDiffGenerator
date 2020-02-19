package pg.gipter.core.dao.configuration;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dto.*;
import pg.gipter.core.producer.command.UploadType;
import pg.gipter.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ApplicationConfigurationDao implements ConfigurationDao {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfigurationDao.class);
    final static String APP_CONFIG = "appConfig";
    final static String TOOLKIT_CONFIG = "toolkitConfig";
    final static String RUN_CONFIGS = "runConfigs";
    final static int NO_CONFIGURATION_FOUND = -1;

    private JsonObject appSettings;

    @Override
    public void saveRunConfig(RunConfig runConfig) {
        JsonObject jsonObject = readJsonConfig();
        Gson gson = new Gson();
        if (jsonObject == null) {
            jsonObject = new JsonObject();
            JsonElement runConfigArray = gson.toJsonTree(Stream.of(runConfig).collect(toList()));
            jsonObject.add(RUN_CONFIGS, runConfigArray);
            logger.info("New configuration [{}] added.", runConfig.getConfigurationName());
        } else {
            boolean isNewRunConfig = true;
            JsonElement runConfigs = jsonObject.get(RUN_CONFIGS);
            if (runConfigs == null) {
                runConfigs = new JsonArray();
            }
            JsonArray runConfigsArray = runConfigs.getAsJsonArray();
            for (int i = 0; i < runConfigsArray.size() && isNewRunConfig; ++i) {
                RunConfig existingRunConfig = gson.fromJson(runConfigsArray.get(i), RunConfig.class);
                if (existingRunConfig.getConfigurationName().equals(runConfig.getConfigurationName())) {
                    runConfigsArray.set(i, gson.toJsonTree(runConfig));
                    isNewRunConfig = false;
                    logger.info("Existing configuration [{}] updated.", runConfig.getConfigurationName());
                }
            }
            if (isNewRunConfig) {
                runConfigsArray.add(gson.toJsonTree(runConfig));
                logger.info("New configuration [{}] added to existing set.", runConfig.getConfigurationName());
            }
            jsonObject.add(RUN_CONFIGS, runConfigsArray);
        }
        writeJsonConfig(jsonObject, RunConfig.class);
    }

    JsonObject readJsonConfig() {
        if (appSettings == null) {
            try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
                 InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)
            ) {
                appSettings = new GsonBuilder().create().fromJson(reader, JsonObject.class);
            } catch (IOException | NullPointerException e) {
                logger.warn("Warning when loading {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            }
        }
        return appSettings;
    }

    void writeJsonConfig(JsonElement jsonElement, Class<?> clazz) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonElement);
        try (OutputStream os = new FileOutputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
            logger.info("File {} updated with {}.", DaoConstants.APPLICATION_PROPERTIES_JSON, clazz.getSimpleName());
            appSettings = jsonElement.getAsJsonObject();
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}", DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }

    @Override
    public ToolkitConfig loadToolkitConfig() {
        JsonObject jsonObject = readJsonConfig();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        if (jsonObject != null) {
            toolkitConfig = new Gson().fromJson(jsonObject.get(TOOLKIT_CONFIG), ToolkitConfig.class);
        }
        return toolkitConfig;
    }

    @Override
    public void saveToolkitConfig(ToolkitConfig toolkitConfig) {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }
        jsonObject.add(TOOLKIT_CONFIG, new Gson().toJsonTree(toolkitConfig));
        writeJsonConfig(jsonObject, ToolkitConfig.class);
    }

    @Override
    public ApplicationConfig loadApplicationConfig() {
        JsonObject jsonObject = readJsonConfig();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        if (jsonObject != null) {
            applicationConfig = new Gson().fromJson(jsonObject.get(APP_CONFIG), ApplicationConfig.class);
        }
        return applicationConfig;
    }

    @Override
    public void saveApplicationConfig(ApplicationConfig applicationConfig) {
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }
        jsonObject.add(APP_CONFIG, new Gson().toJsonTree(applicationConfig));
        writeJsonConfig(jsonObject, ApplicationConfig.class);
    }

    @Override
    public Map<String, RunConfig> loadRunConfigMap() {
        Map<String, RunConfig> runConfigMap = new HashMap<>();
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject != null) {
            JsonArray runConfigsArray = jsonObject.get(RUN_CONFIGS).getAsJsonArray();
            Gson gson = new Gson();
            for (JsonElement element : runConfigsArray) {
                runConfigMap.put(
                        element.getAsJsonObject().get(ArgName.configurationName.name()).getAsString(),
                        gson.fromJson(element, RunConfig.class)
                );
            }
        } else {
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
        JsonObject jsonObject = readJsonConfig();
        if (jsonObject == null) {
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        } else {
            JsonElement jsonElement = jsonObject.get(RUN_CONFIGS);

            int existingConfIdx = getIndexOfExistingConfig(jsonElement, configurationName);
            if (existingConfIdx > NO_CONFIGURATION_FOUND) {
                JsonArray runConfigs = jsonElement.getAsJsonArray();
                runConfigs.remove(existingConfIdx);
                jsonObject.add(RUN_CONFIGS, runConfigs);
                logger.info("Removing run config [{}].", configurationName);
                writeJsonConfig(jsonObject, ApplicationConfig.class);
            } else {
                logger.error(errMsg);
                throw new IllegalStateException(errMsg);
            }
        }
    }

    int getIndexOfExistingConfig(JsonElement jsonElement, String configurationName) {
        JsonArray runConfigs = jsonElement.getAsJsonArray();
        for (int i = 0; i < runConfigs.size(); ++i) {
            JsonObject jObj = runConfigs.get(i).getAsJsonObject();
            String existingConfName = jObj.get(ArgName.configurationName.name()).getAsString();
            if (existingConfName.equals(configurationName)) {
                return i;
            }
        }
        return NO_CONFIGURATION_FOUND;
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
