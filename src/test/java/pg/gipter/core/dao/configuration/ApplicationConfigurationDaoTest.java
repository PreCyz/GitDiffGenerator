package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dto.ApplicationConfig;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.core.producer.command.UploadType;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static pg.gipter.core.dao.configuration.ApplicationConfigurationDao.*;

class ApplicationConfigurationDaoTest {

    private ApplicationConfigurationDao dao;

    @BeforeEach
    void setUp() {
        dao = new ApplicationConfigurationDao();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenGeneratedProperty_whenAddAndSave_thenCreateNewJsonFile() {
        dao.saveRunConfig(new RunConfig());
        dao.saveToolkitConfig(new ToolkitConfig());
        dao.saveApplicationConfig(new ApplicationConfig());

        JsonObject actual = dao.readJsonConfig();
        assertThat(actual).isNotNull();
        assertThat(actual.getAsJsonObject(APP_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonObject(TOOLKIT_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonArray(RUN_CONFIGS)).hasSize(1);
    }

    @Test
    void whenSaveRunConfig_thenNewJsonFileCreated() {
        RunConfig runConfig = new RunConfig();
        runConfig.setConfigurationName("test");

        dao.saveRunConfig(runConfig);

        JsonObject jsonObject = dao.readJsonConfig();
        JsonArray asJsonArray = jsonObject.get(RUN_CONFIGS).getAsJsonArray();
        assertThat(asJsonArray).hasSize(1);
        assertThat(asJsonArray.get(0).getAsJsonObject().get(ArgName.configurationName.name()).getAsString())
                .isEqualTo(runConfig.getConfigurationName());
    }

    @Test
    void givenExistingRunConfig_whenSaveRunConfig_thenAddNewRunConfig() {
        RunConfig runConfig = new RunConfig();
        runConfig.setConfigurationName("test");
        dao.saveRunConfig(runConfig);

        runConfig.setConfigurationName("test2");
        dao.saveRunConfig(runConfig);

        JsonObject jsonObject = dao.readJsonConfig();
        JsonArray asJsonArray = jsonObject.get(RUN_CONFIGS).getAsJsonArray();
        assertThat(asJsonArray).hasSize(2);
        Collection<String> actualConfigNames = new HashSet<>();
        for (JsonElement next : asJsonArray) {
            actualConfigNames.add(next.getAsJsonObject().get(ArgName.configurationName.name()).getAsString());
        }
        assertThat(actualConfigNames).containsExactlyInAnyOrder("test", "test2");
    }

    @Test
    void givenExistingRunConfig_whenSaveRunConfig_thenReplaceExistingWithNewOne() {
        RunConfig runConfig = new RunConfig();
        runConfig.setConfigurationName("test");
        dao.saveRunConfig(runConfig);

        runConfig.setConfigurationName("test");
        runConfig.setCommitterEmail("test");
        dao.saveRunConfig(runConfig);

        JsonObject jsonObject = dao.readJsonConfig();
        JsonArray asJsonArray = jsonObject.get(RUN_CONFIGS).getAsJsonArray();
        assertThat(asJsonArray).hasSize(1);
        assertThat(asJsonArray.get(0).getAsJsonObject().get(ArgName.committerEmail.name()).getAsString())
                .isEqualTo(runConfig.getCommitterEmail());
    }

    @Test
    void whenSaveToolkitConfig_thenFileWithDataSaved() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("ququ");
        toolkitConfig.setToolkitPassword("ququ");

        dao.saveToolkitConfig(toolkitConfig);
        JsonObject jsonObject = dao.readJsonConfig();
        ToolkitConfig actual = new Gson().fromJson(jsonObject.get(TOOLKIT_CONFIG), ToolkitConfig.class);

        assertThat(actual.getToolkitUsername()).isEqualTo("ququ");
        assertThat(actual.getToolkitPassword()).isEqualTo("ququ");
    }

    @Test
    void givenToolkitCOnfig_whenSaveToolkitConfig_thenOverrideExistingOne() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("ququ");
        toolkitConfig.setToolkitPassword("ququ");
        dao.saveToolkitConfig(toolkitConfig);
        toolkitConfig.setToolkitUsername("se");
        toolkitConfig.setToolkitPassword("test");
        dao.saveToolkitConfig(toolkitConfig);

        JsonObject jsonObject = dao.readJsonConfig();
        ToolkitConfig actual = new Gson().fromJson(jsonObject.get(TOOLKIT_CONFIG), ToolkitConfig.class);

        assertThat(actual.getToolkitUsername()).isEqualTo("se");
        assertThat(actual.getToolkitPassword()).isEqualTo("test");
    }

    @Test
    void givenNoToolkitConfig_whenLoadToolkitConfig_thenReturnDefault() {
        dao = new ApplicationConfigurationDao();

        ToolkitConfig actual = dao.loadToolkitConfig();

        assertThat(actual.getToolkitUsername()).isEqualTo(ArgName.toolkitUsername.defaultValue());
        assertThat(actual.getToolkitPassword()).isEqualTo(ArgName.toolkitPassword.defaultValue());
    }

    @Test
    void givenNoApplicationConfig_whenLoadApplicationConfig_thenReturnDefault() {
        ApplicationConfig actual = dao.loadApplicationConfig();

        assertThat(actual.getConfirmationWindow()).isEqualTo(StringUtils.getBoolean(ArgName.confirmationWindow.defaultValue()));
    }

    @Test
    void givenApplicationConfig_whenLoadApplicationConfig_thenReturnDefault() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setPreferredArgSource(PreferredArgSource.FILE);
        dao.saveApplicationConfig(applicationConfig);

        ApplicationConfig actual = dao.loadApplicationConfig();

        assertThat(actual.getPreferredArgSource()).isEqualTo(PreferredArgSource.FILE);
    }

    @Test
    void givenNoRunConfigs_whenLoadRunConfigMap_thenReturnEmptyMap() {
        Map<String, RunConfig> actual = dao.loadRunConfigMap();

        assertThat(actual).hasSize(0);
    }

    @Test
    void givenRunConfigs_whenLoadRunConfigMap_thenReturnMapWithRunConfigs() {
        RunConfig runConfig = new RunConfig();
        runConfig.setConfigurationName("conf1");
        dao.saveRunConfig(runConfig);
        runConfig.setConfigurationName("conf2");
        dao.saveRunConfig(runConfig);

        Map<String, RunConfig> actual = dao.loadRunConfigMap();

        assertThat(actual).hasSize(2);
        assertThat(actual.keySet()).containsExactlyInAnyOrder("conf1", "conf2");
    }

    @Test
    void givenApplicationProperties_whenRemoveConfig_thenRemoveThatConfigFromFile() {
        dao.saveRunConfig(new RunConfig());
        dao.saveToolkitConfig(new ToolkitConfig());
        dao.saveApplicationConfig(new ApplicationConfig());

        dao.removeConfig("");

        JsonObject actual = dao.readJsonConfig();
        assertThat(actual.getAsJsonArray(RUN_CONFIGS)).hasSize(0);
        assertThat(actual.getAsJsonObject(APP_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonObject(TOOLKIT_CONFIG)).isNotNull();
    }

    @Test
    void givenArgsArray_whenGetRunConfigFromArray_thenReturnRunConfig() {
        String[] args = new String[]{
                "author=author",
                "gitAuthor=gitAuthor",
                "mercurialAuthor=mercurialAuthor",
                "svnAuthor=svnAuthor",
                "committerEmail=committerEmail",
                "uploadType=SIMPLE",
                "skipRemote=Y",
                "fetchAll=N",
                "itemPath=itemPath",
                "projectPath=project1,project2",
                "itemFileNamePrefix=123",
                "periodInDays=6",
                "startDate=2020-01-18",
                "endDate=2020-01-11",
                "configurationName=TestConfigName",
                "toolkitProjectListNames=Dudu",
                "deleteDownloadedFiles=N"
        };

        RunConfig actual = dao.getRunConfigFromArray(args);

        assertThat(actual.getAuthor()).isEqualTo("author");
        assertThat(actual.getGitAuthor()).isEqualTo("gitAuthor");
        assertThat(actual.getMercurialAuthor()).isEqualTo("mercurialAuthor");
        assertThat(actual.getSvnAuthor()).isEqualTo("svnAuthor");
        assertThat(actual.getCommitterEmail()).isEqualTo("committerEmail");
        assertThat(actual.getUploadType()).isEqualTo(UploadType.SIMPLE);
        assertThat(actual.getSkipRemote()).isTrue();
        assertThat(actual.getFetchAll()).isFalse();
        assertThat(actual.getItemPath()).isEqualTo("itemPath");
        assertThat(actual.getProjectPath()).isEqualTo("project1,project2");
        assertThat(actual.getItemFileNamePrefix()).isEqualTo("123");
        assertThat(actual.getPeriodInDays()).isEqualTo(6);
        assertThat(actual.getStartDate()).isEqualTo(LocalDate.of(2020, 1, 18));
        assertThat(actual.getEndDate()).isEqualTo(LocalDate.of(2020, 1, 11));
        assertThat(actual.getConfigurationName()).isEqualTo("TestConfigName");
        assertThat(actual.getToolkitProjectListNames()).isEqualTo("Dudu");
        assertThat(actual.getDeleteDownloadedFiles()).isFalse();
    }
}