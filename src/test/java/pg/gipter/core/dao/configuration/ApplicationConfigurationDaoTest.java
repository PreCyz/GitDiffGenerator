package pg.gipter.core.dao.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producer.command.ItemType;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationConfigurationDaoTest {

    private ApplicationConfiguration dao;

    @BeforeEach
    void setUp() {
        try {
            dao = ApplicationConfiguration.getInstance();
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

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAppConfig()).isNotNull();
        assertThat(configuration.getRunConfigs()).hasSize(1);
        assertThat(configuration.getToolkitConfig()).isNotNull();
    }

    @Test
    void whenSaveRunConfig_thenNewJsonFileCreated() {
        RunConfig runConfig = new RunConfig();
        runConfig.setConfigurationName("test");

        dao.saveRunConfig(runConfig);

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration.getRunConfigs()).hasSize(1);
        assertThat(configuration.getRunConfigs().get(0).getConfigurationName()).isEqualTo(runConfig.getConfigurationName());
    }

    @Test
    void givenExistingRunConfig_whenSaveRunConfig_thenAddNewRunConfig() {
        RunConfig runConfig = new RunConfig();
        runConfig.setConfigurationName("test");
        dao.saveRunConfig(runConfig);

        runConfig = new RunConfig();
        runConfig.setConfigurationName("test2");
        dao.saveRunConfig(runConfig);

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration.getRunConfigs()).hasSize(2);
        Collection<String> actualConfigNames = configuration.getRunConfigs()
                .stream()
                .map(RunConfig::getConfigurationName)
                .collect(toSet());
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

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration.getRunConfigs()).hasSize(1);
        assertThat(configuration.getRunConfigs().get(0).getCommitterEmail()).isEqualTo(runConfig.getCommitterEmail());
    }

    @Test
    void whenSaveToolkitConfig_thenFileWithDataSaved() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("ququ");
        toolkitConfig.setToolkitPassword("ququ");

        dao.saveToolkitConfig(toolkitConfig);

        Configuration configuration = dao.readJsonConfig();
        ToolkitConfig actual = configuration.getToolkitConfig();
        assertThat(actual.getToolkitUsername()).isEqualTo("ququ");
        assertThat(actual.getToolkitPassword()).isEqualTo("ququ");
    }

    @Test
    void givenToolkitConfig_whenSaveToolkitConfig_thenOverrideExistingOne() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("ququ");
        toolkitConfig.setToolkitPassword("ququ");
        dao.saveToolkitConfig(toolkitConfig);
        toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("se");
        toolkitConfig.setToolkitPassword("test");
        dao.saveToolkitConfig(toolkitConfig);

        Configuration configuration = dao.readJsonConfig();
        ToolkitConfig actual = configuration.getToolkitConfig();

        assertThat(actual.getToolkitUsername()).isEqualTo("se");
        assertThat(actual.getToolkitPassword()).isEqualTo("test");
    }

    @Test
    void givenNoToolkitConfig_whenLoadToolkitConfig_thenReturnDefault() {
        dao = ApplicationConfiguration.getInstance();

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
        runConfig = new RunConfig();
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

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration.getRunConfigs()).hasSize(0);
        assertThat(configuration.getAppConfig()).isNotNull();
        assertThat(configuration.getToolkitConfig()).isNotNull();
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
        assertThat(actual.getItemType()).isEqualTo(ItemType.SIMPLE);
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