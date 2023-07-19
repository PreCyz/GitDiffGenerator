package pg.gipter.core.dao.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
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
        dao.saveConfiguration(new Configuration(
                new ApplicationConfig(), new ToolkitConfig(), Collections.singletonList(new RunConfig()), null
        ));

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAppConfig()).isNotNull();
        assertThat(configuration.getRunConfigs()).hasSize(1);
        assertThat(configuration.getToolkitConfig()).isNotNull();
    }

    @Test
    void givenExistingRunConfig_whenSaveRunConfig_thenAddNewRunConfig() {
        dao.saveConfiguration(new Configuration(null, null, Stream.of(
                new RunConfigBuilder().withConfigurationName("test").create(),
                new RunConfigBuilder().withConfigurationName("test2").create()
        ).collect(toList()), null));

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration.getRunConfigs()).hasSize(2);
        Collection<String> actualConfigNames = configuration.getRunConfigs()
                .stream()
                .map(RunConfig::getConfigurationName)
                .collect(toSet());
        assertThat(actualConfigNames).containsExactlyInAnyOrder("test", "test2");
    }

    @Test
    void givenExistingRunConfig_whenSaveConfiguration_thenReplaceExistingWithNewOne() {
        dao.saveConfiguration(new Configuration(null, null, Stream.of(
                new RunConfigBuilder().withConfigurationName("test").create(),
                new RunConfigBuilder().withConfigurationName("test").withCommitterEmail("test").create()
        ).collect(toList()), null));

        Configuration configuration = dao.readJsonConfig();
        assertThat(configuration.getRunConfigs()).hasSize(2);
        assertThat(configuration.getRunConfigs().get(0).getCommitterEmail()).isNull();
        assertThat(configuration.getRunConfigs().get(1).getCommitterEmail()).isEqualTo("test");
    }

    @Test
    void whenSaveToolkitConfig_thenFileWithDataSaved() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("ququ");

        dao.saveToolkitConfig(toolkitConfig);

        Configuration configuration = dao.readJsonConfig();
        ToolkitConfig actual = configuration.getToolkitConfig();
        assertThat(actual.getToolkitUsername()).isEqualTo("ququ");
    }

    @Test
    void givenToolkitConfig_whenSaveToolkitConfig_thenOverrideExistingOne() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("ququ");
        dao.saveToolkitConfig(toolkitConfig);
        toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("se");
        dao.saveToolkitConfig(toolkitConfig);

        Configuration configuration = dao.readJsonConfig();
        ToolkitConfig actual = configuration.getToolkitConfig();

        assertThat(actual.getToolkitUsername()).isEqualTo("se");
    }

    @Test
    void givenNoToolkitConfig_whenLoadToolkitConfig_thenReturnDefault() {
        dao = ApplicationConfiguration.getInstance();

        ToolkitConfig actual = dao.loadToolkitConfig();

        assertThat(actual.getToolkitUsername()).isEqualTo(ArgName.toolkitUsername.defaultValue());
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
        dao.saveConfiguration(new Configuration(applicationConfig, null, null, null));

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
        dao.saveConfiguration(new Configuration(null, null, Stream.of(
                new RunConfigBuilder().withConfigurationName("conf1").create(),
                new RunConfigBuilder().withConfigurationName("conf2").create()
        ).collect(toList()), null));

        Map<String, RunConfig> actual = dao.loadRunConfigMap();

        assertThat(actual).hasSize(2);
        assertThat(actual.keySet()).containsExactlyInAnyOrder("conf1", "conf2");
    }

    @Test
    void givenApplicationProperties_whenRemoveConfig_thenRemoveThatConfigFromFile() {
        dao.saveConfiguration(new Configuration(
                new ApplicationConfig(), new ToolkitConfig(), Collections.singletonList(new RunConfig()), null
        ));
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
                "itemType=SIMPLE",
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