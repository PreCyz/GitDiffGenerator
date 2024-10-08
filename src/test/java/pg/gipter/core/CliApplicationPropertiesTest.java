package pg.gipter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.TestUtils;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.SemanticVersioning;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.WeekFields;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class CliApplicationPropertiesTest {

    private ApplicationProperties applicationProperties;

    @BeforeEach
    void setup() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
            DaoFactory.getCachedConfiguration().resetCache();
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenNoAuthor_whenAuthors_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("NO_AUTHORS"));
    }

    @Test
    void given_cliAuthor_when_author_then_returnCliAuthor() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"author=cliAuthor"}
        ).init();

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("cliAuthor"));
    }

    @Test
    void given_cliAuthorAndFileAuthor_when_author_then_returnCliAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"author=cliAuthor"}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("cliAuthor"));
    }

    @Test
    void given_noCliAuthorAndFileAuthor_when_author_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("fileAuthor"));
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_author_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"startDate=2019-02-16"}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("fileAuthor"));
    }

    @Test
    void givenFileCommitterEmail_whenAuthors_thenReturnEmptyCollection() {
        RunConfig runConfig = new RunConfigBuilder()
                .withCommitterEmail("email")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).isEmpty();
    }

    @Test
    void givenFileGitAuthor_whenAuthors_thenReturnEmptyCollection() {
        RunConfig runConfig = new RunConfigBuilder()
                .withGitAuthor("author")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).isEmpty();
    }

    @Test
    void givenFileMercurialAuthor_whenAuthors_thenReturnEmptyCollection() {
        RunConfig runConfig = new RunConfigBuilder()
                .withMercurialAuthor("author")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).isEmpty();
    }

    @Test
    void givenFileSvnAuthor_whenAuthors_thenReturnEmptyCollection() {
        RunConfig runConfig = new RunConfigBuilder()
                .withSvnAuthor("author")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).isEmpty();
    }

    @Test
    void givenAllOtherAuthors_whenAuthors_thenReturnEmptyCollection() {
        RunConfig runConfig = new RunConfigBuilder()
                .withGitAuthor("author")
                .withCommitterEmail("author")
                .withMercurialAuthor("author")
                .withSvnAuthor("author")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).isEmpty();
    }

    @Test
    void given_noGitAuthor_when_gitAuthor_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_cliGitAuthor_when_gitAuthor_then_returnCliAuthor() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"gitAuthor=cliAuthor"}
        ).init();

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_cliGitAuthorAndFileGitAuthor_when_gitAuthor_then_returnCliAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withGitAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"gitAuthor=cliAuthor"}).init();
        applicationProperties.init(loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliGitAuthorAndFileAuthor_when_gitAuthor_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withGitAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_gitAuthor_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withGitAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"author=test"}).init();
        applicationProperties.init(loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_noMercurialAuthor_when_mercurialAuthor_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_cliMercurialAuthor_when_mercurialAuthor_then_returnCliAuthor() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"mercurialAuthorAuthor=cliAuthor"}
        );

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_cliMercurialAuthorAndFileGitAuthor_when_mercurialAuthor_then_returnCliAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withMercurialAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"mercurialAuthor=cliAuthor"});
        applicationProperties.init(loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliMercurialAuthorAndFileAuthor_when_mercurialAuthor_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withMercurialAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_mercurialAuthor_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withMercurialAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"author=test"});
        applicationProperties.init(loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_noSvnAuthor_when_svnAuthor_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_cliSvnAuthor_when_svnAuthor_then_returnCliAuthor() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"svnAuthor=cliAuthor"}
        );

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_cliSvnAuthorAndFileGitAuthor_when_svnAuthor_then_returnCliAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withSvnAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"svnAuthor=cliAuthor"});
        applicationProperties.init(loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliSvnAuthorAndFileAuthor_when_svnAuthor_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withSvnAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        applicationProperties.init(loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_svnAuthor_then_returnFileAuthor() {
        RunConfig runConfig = new RunConfigBuilder()
                .withSvnAuthor("fileAuthor")
                .create();
        ConfigurationDao loader = TestUtils.mockConfigurationDao(runConfig);
        applicationProperties = new CliApplicationProperties(new String[]{"author=test"});
        applicationProperties.init(loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void givenNoItemPath_whenItemPath_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("NO_ITEM_PATH");
    }

    @Test
    void given_itemPathFromCLI_when_itemPath_then_returnThatItemPath() {
        applicationProperties = new CliApplicationProperties(new String[]{"itemPath=testItemPath"}).init();

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith(Paths.get("testItemPath").toString());
    }

    @Test
    void given_itemPathFromPropertiesAndCLI_when_itemPath_then_returnItemPathFromCLI() {
        String[] args = {"itemPath=cliItemPath"};
        applicationProperties = new CliApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder()
                .withItemPath("propertiesItemPath")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith(Paths.get("cliItemPath").toString());
    }

    @Test
    void given_itemPathFromProperties_when_itemPath_then_returnItemPathFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder()
                .withItemPath("propertiesItemPath")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith(Paths.get("propertiesItemPath").toString());
    }

    @Test
    void given_itemPathFromPropertiesAndOtherArgs_when_itemPath_then_returnItemPathFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemPath("propertiesItemPath")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("propertiesItemPath");
    }

    @Test
    void given_noItemFileNamePrefix_when_itemFileNamePrefix_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_itemFileNamePrefixFromCLI_when_itemFileNamePrefix_then_returnThatCliItemFileNamePrefix() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"itemFileNamePrefix=cliItemFileNamePrefix"}
        );

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEqualTo("cliItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromFileAndCLI_when_itemPath_then_returnCliItemFileNamePrefix() {
        String[] args = {"itemFileNamePrefix=cliItemFileNamePrefix"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemFileNamePrefix("propertiesItemFileNamePrefix")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEqualTo("cliItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromProperties_when_itemFileNamePrefix_then_returnItemFileNamePrefixFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemFileNamePrefix("propertiesItemFileNamePrefix")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.itemPath();

        assertThat(actual).contains("propertiesItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromPropertiesAndOtherArgs_when_itemFileNamePrefix_then_returnItemPathFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemFileNamePrefix("propertiesItemFileNamePrefix")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEqualTo("propertiesItemFileNamePrefix");
    }

    @Test
    void givenNoProjectPaths_whenProjectPaths_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsOnly("NO_PROJECT_PATH");
    }

    @Test
    void given_projectPathsFromCLI_when_projectPaths_then_returnCliProjectPaths() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"projectPath=cliProjectPath1,cliProjectPath2"}
        );

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("cliProjectPath1", "cliProjectPath2");
    }

    @Test
    void given_projectPathFileAndCLI_when_projectPaths_then_returnCliProjectPath() {
        String[] args = {"projectPath=cliProjectPath1,cliProjectPath2"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withProjectPath("propertiesProjectPath1,propertiesProjectPath2")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("cliProjectPath1", "cliProjectPath2");
    }

    @Test
    void given_projectPathFromProperties_when_projectPaths_then_returnProjectPathFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withProjectPath("propertiesProjectPath1,propertiesProjectPath2")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("propertiesProjectPath1", "propertiesProjectPath2");
    }

    @Test
    void given_projectPathFromPropertiesAndOtherArgs_when_projectPaths_then_returnProjectPathFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withProjectPath("propertiesProjectPath1,propertiesProjectPath2")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("propertiesProjectPath1", "propertiesProjectPath2");
    }

    @Test
    void given_noCommitterEmail_when_committerEmail_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_committerEmailFromCLI_when_committerEmail_then_returnCliCommitterEmail() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"committerEmail=test@email.cli"}
        );

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.cli");
    }

    @Test
    void given_committerEmailFileAndCLI_when_committerEmail_then_returnCliCommitterEmail() {
        String[] args = {"committerEmail=test@email.cli"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withCommitterEmail("test@email.properties")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.cli");
    }

    @Test
    void given_committerEmailFromProperties_when_committerEmail_then_returnCommitterEmailFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withCommitterEmail("test@email.properties")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));
        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.properties");
    }

    @Test
    void given_committerEmailFromPropertiesAndOtherArgs_when_committerEmail_then_returnCommitterEmailFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withCommitterEmail("test@email.properties")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));
        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.properties");
    }

    @Test
    void given_noStartDate_when_startDate_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(7));
    }

    @Test
    void given_startDateFromCLI_when_startDate_then_returnCliStartDate() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"startDate=2019-02-01"}
        ).init();

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_startDateFileAndCLI_when_startDate_then_returnCliStartDate() {
        String[] args = {"startDate=2019-02-01"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withStartDate(LocalDate.of(2019, 2, 9))
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_startDateFromProperties_when_startDate_then_returnStartDateFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withStartDate(LocalDate.of(2019, 2, 9))
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_startDateFromPropertiesAndOtherArgs_when_startDate_then_returnStartDateFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withStartDate(LocalDate.of(2019, 2, 9))
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void givenPeriodInDaysFromCli_when_startDate_then_returnStartDateBasedOnPeriodInDays() {
        String[] args = {"periodInDays=5"};
        applicationProperties = new CliApplicationProperties(args).init();

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(5));
    }

    @Test
    void givenPeriodInDaysFromCliAndProperties_wheStartDate_thenReturnStartDateBasedOnCliPeriodInDays() {
        String[] args = {"periodInDays=5"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withPeriodInDays(6)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(5));
    }

    @Test
    void givenPeriodInDaysFromProperties_when_startDate_then_returnStartDateBasedOnPropertiesPeriodInDays() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withPeriodInDays(6)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(6));
    }

    @Test
    void given_noEndDate_when_endDate_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init().init();

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual).isEqualTo(LocalDate.now());
    }

    @Test
    void given_endDateFromCLI_when_endDate_then_returnCliEndDate() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"endDate=2019-02-01"}
        );

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_endDateFileAndCLI_when_endDate_then_returnCliEndDate() {
        String[] args = {"endDate=2019-02-01"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withEndDate(LocalDate.of(2019, 2, 9))
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_endDateFromProperties_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withEndDate(LocalDate.of(2019, 2, 9))
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_endDateFromPropertiesAndOtherArgs_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withEndDate(LocalDate.of(2019, 2, 9))
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_noCodeProtection_when_uploadType_then_returnCodeProtection() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        ItemType actual = applicationProperties.itemType();

        assertThat(actual).isEqualTo(ItemType.SIMPLE);
    }

    @Test
    void givenItemTypeFromCLI_whenItemType_thenReturnCliCodeProtection() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"itemType=protected"}
        );

        ItemType actual = applicationProperties.itemType();

        assertThat(actual).isEqualTo(ItemType.PROTECTED);
    }

    @Test
    void givenItemTypeFileAndCLI_whenItemType_thenReturnCliCodeProtection() {
        String[] args = {"itemType=PROTECTED"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.STATEMENT)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        ItemType actual = applicationProperties.itemType();

        assertThat(actual).isEqualTo(ItemType.PROTECTED);
    }

    @Test
    void given_uploadTypeFromProperties_when_uploadType_then_returnCodeProtectionFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.STATEMENT)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        ItemType actual = applicationProperties.itemType();

        assertThat(actual).isEqualTo(ItemType.STATEMENT);
    }

    @Test
    void given_uploadTypeFromPropertiesAndOtherArgs_when_uploadType_then_returnCodeProtectionFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.STATEMENT)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        ItemType actual = applicationProperties.itemType();

        assertThat(actual).isEqualTo(ItemType.STATEMENT);
    }

    @Test
    void given_noConfirmationWindow_when_isConfirmationWindow_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowFromCLI_when_isConfirmationWindow_then_returnCliConfirmationWindow() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"confirmationWindow=y"}
        );

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowFileAndCLI_when_isConfirmationWindow_then_returnCliConfirmationWindow() {
        String[] args = {"confirmationWindow=y"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setConfirmationWindow(Boolean.FALSE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowFromProperties_when_isConfirmationWindow_then_returnConfirmationWindowFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setConfirmationWindow(Boolean.TRUE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void givenConfirmationWindowFromPropertiesAndOtherArgs_whenIsConfirmationWindow_thenReturnConfirmationWindowFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setConfirmationWindow(Boolean.TRUE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNoToolkitUsername_whenToolkitUsername_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo(SystemUtils.userName().toUpperCase());
    }

    @Test
    void given_toolkitUsernameFromCLI_when_toolkitUsername_then_returnCliToolkitUsername() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"toolkitUsername=cliUserName"}
        );

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("cliUserName".toUpperCase());
    }

    @Test
    void given_toolkitUsernameFileAndCLI_when_toolkitUsername_then_returnCliToolkitUsername() {
        String[] args = {"toolkitUsername=cliUserName"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("propertiesUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("cliUserName".toUpperCase());
    }

    @Test
    void given_toolkitUsernameFromProperties_when_toolkitUsername_then_returnToolkitUsernameFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("propertiesUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("propertiesUserName".toUpperCase());
    }

    @Test
    void given_toolkitUsernameFromPropertiesAndOtherArgs_when_toolkitUsername_then_returnToolkitUsernameFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("propertiesUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("propertiesUserName".toUpperCase());
    }

    @Test
    void givenNoToolkitHostUrl_whenToolkitHostUrl_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.toolkitHostUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com");
    }

    @Test
    void givenToolkitHostFromCLI_whenToolkitHostUrl_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"toolkitHostUrl=cliUrl"}
        );

        String actual = applicationProperties.toolkitHostUrl();

        assertThat(actual).isEqualTo("cliUrl");
    }

    @Test
    void givenToolkitHostUrlFileAndCLI_whenToolkitHostUrl_thenReturnDefault() {
        String[] args = {"toolkitHostUrl=cliUrl"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitHostUrl("propertiesUrl");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitHostUrl();

        assertThat(actual).isEqualTo("cliUrl");
    }

    @Test
    void givenToolkitHostUrlFromProperties_whenToolkitHostUrl_thenReturnToolkitHostUrlFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitHostUrl("propertiesUrl");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitHostUrl();

        assertThat(actual).isEqualTo("propertiesUrl");
    }

    @Test
    void givenToolkitHostUrlFromPropertiesAndOtherArgs_whenToolkitHostUrl_thenReturnToolkitUrlFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitHostUrl("propertiesUrl");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitHostUrl();

        assertThat(actual).isEqualTo("propertiesUrl");
    }

    @Test
    void given_noToolkitListName_when_toolkitListName_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromCLI_when_toolkitListName_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"toolkitCopyListName=cliListName"}
        );

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFileAndCLI_when_toolkitListName_then_returnDefault() {
        String[] args = {"toolkitCopyListName=cliListName"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitCopyListName("propertiesListName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromProperties_when_toolkitListName_then_returnToolkitListNameFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitCopyListName("propertiesListName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("propertiesListName");
    }

    @Test
    void given_toolkitListNameFromPropertiesAndOtherArgs_when_toolkitListName_then_returnToolkitListNameFromProperties() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitCopyListName("propertiesListName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("propertiesListName");
    }

    @Test
    void givenNoToolkitUserFolder_whenToolkitFolderName_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualToIgnoringCase(ArgName.toolkitFolderName.defaultValue());
    }

    @Test
    void givenToolkitUserNameFromCLI_whenToolkitFolderName_then_returnThatFolderName() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"toolkitUsername=cliUserName"}
        ).init();

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualToIgnoringCase(System.getProperty("user.name"));
    }

    @Test
    void givenToolkitUserNameFileAndCLI_whenToolkitUserFolder_thenReturnSystemUserName() {
        String[] args = {"toolkitUsername=cliUserName"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("fileUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualToIgnoringCase(System.getProperty("user.name"));
    }

    //disregard file settings
    @Test
    void givenToolkitUsernameFromFile_whenToolkitFolderName_thenReturnWithDefault() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("fileUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualToIgnoringCase(ArgName.toolkitFolderName.defaultValue());
    }

    //disregard file settings
    @Test
    void givenToolkitUserNameFromFileAndOtherArgs_whenToolkitFolderName_thenReturnDefaultFolderName() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("fileUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualToIgnoringCase(ArgName.toolkitFolderName.defaultValue());
    }

    @Test
    void givenToolkitFolderNameFromCLI_whenToolkitFolderName_thenReturnCliFolderName() {
        String[] args = {"toolkitFolderName=cliFolderName"};
        applicationProperties = new CliApplicationProperties(args).init();

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualTo("CLIFOLDERNAME");
    }

    @Test
    void givenToolkitUserNameAndFolderNameFromCLI_whenToolkitFolderName_thenReturnCliFolderName() {
        String[] args = {"toolkitUsername=cliUserName", "toolkitFolderName=cliFolderName"};
        applicationProperties = new CliApplicationProperties(args).init();

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualTo("CLIFOLDERNAME");
    }

    @Test
    void givenToolkitUserNameFileAndCLIAndFolderNameFileAndCli_whenToolkitUserFolder_thenReturnCliFolderName() {
        String[] args = {"toolkitUsername=cliUserName", "toolkitFolderName=cliFolderName"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("fileUserName");
        toolkitConfig.setToolkitFolderName("fileFolderName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualTo("CLIFOLDERNAME");
    }

    @Test
    void givenToolkitUserNameFileAndCLIAndFolderNameCli_whenToolkitUserFolder_thenReturnCliFolderName() {
        String[] args = {"toolkitUsername=cliUserName", "toolkitFolderName=cliFolderName"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("fileUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualTo("CLIFOLDERNAME");
    }

    @Test
    void givenToolkitUserNameAndFolderNameFromFileAndOtherArgs_whenToolkitFolderName_thenReturnFileFolderName() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("fileUserName");
        toolkitConfig.setToolkitFolderName("fileFolderName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitFolderName();

        assertThat(actual).isEqualTo("FILEFOLDERNAME");
    }

    @Test
    void givenNoToolkitUserFolder_whenToolkitUserFolder_Link_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo(
                "https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/" + SystemUtils.userName().toUpperCase()
        );
    }

    @Test
    void given_toolkitUserNameFromCLI_when_toolkitUserFolder_then_returnProperFolderLink() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"toolkitUsername=cliUserName"}
        );

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/CLIUSERNAME");
    }

    @Test
    void given_toolkitUserFolderFileAndCLI_when_toolkitUserFolder_then_returnWithCliUserLink() {
        String[] args = {"toolkitUsername=cliUserName"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("propertiesUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/CLIUSERNAME");
    }

    @Test
    void given_toolkitUsernameFromProperties_when_toolkitUserFolder_then_returnWithToolkitUsernameFromPropertiesLink() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("propertiesUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/PROPERTIESUSERNAME");
    }

    @Test
    void given_toolkitUserNameFromPropertiesAndOtherArgs_when_toolkitUserFolder_then_returnProperWithUserNameFromPropertiesLink() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("propertiesUserName");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/PROPERTIESUSERNAME");
    }

    @Test
    void given_noSkipRemote_when_isSkipRemote_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isTrue();
    }

    @Test
    void given_skipRemoteFromCLI_when_isSkipRemote_then_returnCliSkipRemote() {
        applicationProperties = new CliApplicationProperties(new String[]{"skipRemote=N"});

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_skipRemoteFileAndCLI_when_isSkipRemote_then_returnCliSkipRemote() {
        String[] args = {"skipRemote=n"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withSkipRemote(Boolean.TRUE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_skipRemoteFromProperties_when_isSkipRemote_then_returnSkipRemoteFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withSkipRemote(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_skipRemoteFromPropertiesAndOtherArgs_when_isSkipRemote_then_returnSkipRemoteFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withSkipRemote(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAll_whenIsFetchAll_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFromCLI_whenIsFetchAll_thenReturnCliFetchAll() {
        applicationProperties = new CliApplicationProperties(new String[]{"fetchAll=N"});

        boolean actual = applicationProperties.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFileAndCLI_whenIsFetchAll_thenReturnCliFetchAll() {
        String[] args = {"fetchAll=n"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withFetchAll(Boolean.TRUE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFromProperties_whenIsFetchAll_thenReturnFetchAllFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withFetchAll(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFromPropertiesAndOtherArgs_whenIsFetchAll_thenReturnFetchAllFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withFetchAll(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noPreferredArgSource_when_preferredArgSource_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.CLI);
    }

    @Test
    void given_preferredArgSourceFromCLI_when_preferredArgSource_then_returnCliPreferredArgSource() {
        applicationProperties = new CliApplicationProperties(new String[]{"preferredArgSource=file"});

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.FILE);
    }

    @Test
    void given_preferredArgSourceFileAndCLI_when_preferredArgSource_then_returnCliPreferredArgSource() {
        String[] args = {"preferredArgSource=FILE"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withPreferredArgSource(PreferredArgSource.CLI)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.FILE);
    }

    @Test
    void given_preferredArgSourceFromProperties_when_preferredArgSource_then_returnPreferredArgSourceCLI() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withPreferredArgSource(PreferredArgSource.FILE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.CLI);
    }

    @Test
    void given_preferredArgSourceFromPropertiesAndOtherArgs_when_preferredArgSource_then_returnPreferredArgSourceCLI() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withPreferredArgSource(PreferredArgSource.FILE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.CLI);
    }

    @Test
    void given_versionTxt_when_version_then_returnVersion() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        SemanticVersioning actual = applicationProperties.version();

        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isNotEmpty();
        assertThat(actual.getVersion()).isNotBlank();
    }

    @Test
    void given_noUseUI_when_isUseUI_then_returnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFromCLI_when_isUseUI_then_returnCliUseUI() {
        applicationProperties = new CliApplicationProperties(new String[]{"useUI=T"});

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFileAndCLI_when_isUseUI_then_returnCliUseUI() {
        String[] args = {"useUI=t"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUseUI(Boolean.FALSE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFromProperties_when_isUseUI_then_returnUseUIFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUseUI(Boolean.TRUE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFromPropertiesAndOtherArgs_when_isUseUI_then_returnUseUIFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUseUI(Boolean.TRUE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNoToolkitUserCliAndNoFileToolkitUser_whenToolkitUserFolder_Link_then_returnDefault() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolderUrl.defaultValue() + ArgName.toolkitUsername.defaultValue().toUpperCase());
    }

    @Test
    void givenToolkitUserCliAndNoFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUserLink() {
        String[] args = {"toolkitUsername=xxx"};
        applicationProperties = new CliApplicationProperties(args).init();

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolderUrl.defaultValue() + "XXX");
    }

    @Test
    void givenToolkitUserCliAndFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUserLink() {
        String[] args = {"toolkitUsername=xxx"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("aaa");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolderUrl.defaultValue() + "XXX");
    }

    @Test
    void givenNoToolkitUserCliAndFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUserLink() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("aaa");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolderUrl.defaultValue() + "AAA");
    }

    @Test
    void givenToolkitUserCliAndFileCustomUserFolder_whenToolkitUserFolder_then_returnUserFolderWithCliCustomUserFolderLink() {
        String[] args = {"toolkitUsername=qqq"};
        applicationProperties = new CliApplicationProperties(args).init();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("aaa");
        applicationProperties.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = applicationProperties.toolkitUserFolderUrl();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolderUrl.defaultValue() + "QQQ");
    }

    @Test
    void givenNoProjectPaths_whenToolkitProjectListNames_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsOnly("Deliverables");
    }

    @Test
    void givenToolkitProjectListNamesFromCLI_whenToolkitProjectListNames_thenReturnCliToolkitProjectListNames() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"toolkitProjectListNames=name1,name2"}
        );

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("name1", "name2");
    }

    @Test
    void givenToolkitProjectListNamesFileAndCLI_whenToolkitProjectListNames_thenReturnCliToolkitProjectListNames() {
        String[] args = {"toolkitProjectListNames=cli1,cli2"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withToolkitProjectListNames("properties1,properties2")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("cli1", "cli2");
    }

    @Test
    void givenToolkitProjectListNamesFromProperties_whenToolkitProjectListNames_thenReturnPropertiesToolkitProjectListNames() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withToolkitProjectListNames("properties1,properties2")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("properties1", "properties2");
    }

    @Test
    void givenToolkitProjectListNamesFromPropertiesAndOtherArgs_whenToolkitProjectListNames_thenReturnPropertiesToolkitProjectListNames() {
        String[] args = {"uploadType=statement"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withToolkitProjectListNames("properties1,properties2")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("properties1", "properties2");
    }

    @Test
    void givenNoDeleteDownloadedFiles_whenIsDeleteDownloadedFiles_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenDeleteDownloadedFilesFromCLI_whenIsDeleteDownloadedFiles_thenReturnCliDeleteDownloadedFiles() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"deleteDownloadedFiles=y"}
        );

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenDeleteDownloadedFilesFileAndCLI_whenIsDeleteDownloadedFiles_thenReturnCliDeleteDownloadedFiles() {
        String[] args = {"deleteDownloadedFiles=y"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withDeleteDownloadedFiles(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenDeleteDownloadedFilesFromProperties_whenIsDeleteDownloadedFiles_thenReturnDeleteDownloadedFilesFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withDeleteDownloadedFiles(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isFalse();
    }

    @Test
    void givenDeleteDownloadedFilesFromPropertiesAndOtherArgs_whenIsDeleteDownloadedFiles_thenReturnDeleteDownloadedFilesFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withDeleteDownloadedFiles(Boolean.FALSE)
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNoConfigurationName_whenConfigurationName_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo(ArgName.configurationName.defaultValue());
    }

    @Test
    void givenCliConfigurationName_whenConfigurationName_thenReturnCliConfigurationName() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"configurationName=cliAuthor"}
        );

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void givenCliConfigurationNameAndFileConfigurationName_whenConfigurationName_then_returnCliConfigurationName() {
        applicationProperties = new CliApplicationProperties(new String[]{"configurationName=cliAuthor"});
        RunConfig runConfig = new RunConfigBuilder()
                .withConfigurationName("fileAuthor")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void givenNoCliConfigurationNameAndFileConfigurationName_whenConfigurationName_thenReturnFileConfigurationName() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();
        RunConfig runConfig = new RunConfigBuilder()
                .withConfigurationName("fileAuthor")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void givenFileConfigurationNameAndOtherArgs_whenConfigurationName_thenReturnFileConfigurationName() {
        applicationProperties = new CliApplicationProperties(new String[]{"author=test"});
        RunConfig runConfig = new RunConfigBuilder()
                .withConfigurationName("fileAuthor")
                .create();
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void givenNoNamePatternValue_whenValueFromPattern_thenReturnEmptyString() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(null);

        assertThat(actual).isEmpty();
    }

    @Test
    void givenCURRENT_DATE_whenValueFromPattern_thenReturnNow() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_DATE);

        assertThat(actual).isEqualTo(LocalDate.now().format(ApplicationProperties.yyyy_MM_dd));
    }

    @Test
    void givenCURRENT_YEAR_whenValueFromPattern_thenReturnYear() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_YEAR);

        assertThat(actual).isEqualTo(String.valueOf(LocalDate.now().getYear()));
    }

    @Test
    void givenCURRENT_MONTH_whenValueFromPattern_thenReturnMonthName() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_MONTH_NAME);

        assertThat(actual).isEqualTo(LocalDate.now().getMonth().name());
    }

    @Test
    void givenCURRENT_MONTH_NUMBER_whenValueFromPattern_thenReturnMonthNumber() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_MONTH_NUMBER);

        assertThat(actual.length()).isEqualTo(2);
        assertThat(actual).contains(String.valueOf(LocalDate.now().getMonthValue()));
    }

    @Test
    void givenCURRENT_WEEK_NUMBER_whenValueFromPattern_thenReturnCurrentWeekNumber() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_WEEK_NUMBER);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        assertThat(actual).isEqualTo(String.valueOf(LocalDate.now().get(weekFields.weekOfWeekBasedYear())));
    }

    @Test
    void givenSTART_DATE_whenValueFromPattern_thenReturnStartDate() {
        String startDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"startDate=" + startDate}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE);

        assertThat(actual).isEqualTo(startDate);
    }

    @Test
    void givenSTART_DATE_YEAR_whenValueFromPattern_thenReturnStartDateYear() {
        String startDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"startDate=" + startDate}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_YEAR);

        assertThat(actual).isEqualTo("2018");
    }

    @Test
    void givenSTART_DATE_MONTH_NAME_whenValueFromPattern_thenReturnStartDateMonthName() {
        String startDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"startDate=" + startDate}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_MONTH_NAME);

        assertThat(actual).isEqualTo(Month.JUNE.name());
    }

    @Test
    void givenSTART_DATE_MONTH_NUMBER_whenValueFromPattern_thenReturnStartDateMonthNumber() {
        String startDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"startDate=" + startDate}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_MONTH_NUMBER);

        assertThat(actual.length()).isEqualTo(2);
        assertThat(actual).contains(String.valueOf(Month.JUNE.getValue()));
    }

    @Test
    void givenSTART_DATE_WEEK_NUMBER_whenValueFromPattern_thenReturnStartDateMonthName() {
        String startDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"startDate=" + startDate}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_WEEK_NUMBER);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        assertThat(actual).isEqualTo(String.valueOf(LocalDate.parse(startDate, ApplicationProperties.yyyy_MM_dd).get(weekFields.weekOfWeekBasedYear())));
    }

    @Test
    void givenEND_DATE_whenValueFromPattern_thenReturnEndDate() {
        String endDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE);

        assertThat(actual).isEqualTo(endDate);
    }

    @Test
    void givenEND_DATE_YEAR_whenValueFromPattern_thenReturnEndDateYear() {
        String endDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_YEAR);

        assertThat(actual).isEqualTo("2018");
    }

    @Test
    void givenEND_DATE_MONTH_NAME_whenValueFromPattern_thenReturnEndDateMonthName() {
        String endDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_MONTH_NAME);

        assertThat(actual).isEqualTo(Month.JUNE.name());
    }

    @Test
    void givenEND_DATE_MONTH_NUMBER_whenValueFromPattern_thenReturnEndDateMonthNumber() {
        String endDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_MONTH_NUMBER);

        assertThat(actual.length()).isEqualTo(2);
        assertThat(actual).contains(String.valueOf(Month.JUNE.getValue()));
    }

    @Test
    void givenEND_DATE_WEEK_NUMBER_whenValueFromPattern_thenReturnEndDateMonthName() {
        String endDate = "2018-06-12";
        applicationProperties = new CliApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_WEEK_NUMBER);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        assertThat(actual).isEqualTo(String.valueOf(LocalDate.parse(endDate, ApplicationProperties.yyyy_MM_dd).get(weekFields.weekOfWeekBasedYear())));
    }

    @Test
    void givenFileNameSettingsAndFileNamePrefix_whenFileName_thenReturnFileNameFromPattern() {
        applicationProperties = new CliApplicationProperties(new String[]{
                ArgName.itemFileNamePrefix + "=" + "my-{START_DATE_MONTH_NAME}-{START_DATE_YEAR}-name",
                ArgName.startDate + "=2018-06-12"
        }).init();

        String actual = applicationProperties.fileName();

        assertThat(actual).startsWith("my-JUNE-2018-name");
    }

    @Test
    void givenNoUpgradeFinished_whenIsUpgradeFinished_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isFalse();
    }

    @Test
    void givenUpgradeFinishedFromCLI_whenIsUpgradeFinished_thenReturnCliUpgradeFinished() {
        applicationProperties = new CliApplicationProperties(
                new String[]{"upgradeFinished=y"}
        );

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedFileAndCLI_whenIsUpgradeFinished_thenReturnCliUpgradeFinished() {
        String[] args = {"upgradeFinished=y"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUpgradeFinished(Boolean.FALSE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedFromProperties_whenIsUpgradeFinished_thenReturnUpgradeFinishedFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUpgradeFinished(Boolean.TRUE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedFromPropertiesAndOtherArgs_whenIsUpgradeFinished_thenReturnUseAsFileNameFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUpgradeFinished(Boolean.TRUE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenCurrent_month_name_whenValueFromPattern_thenReturnMonthNameLowerCase() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.current_month_name);

        assertThat(actual).isEqualTo(LocalDate.now().getMonth().name().toLowerCase());
    }

    @Test
    void givenStartDateFromCLI_whenValueFromPattern_thenReturnMonthNameLowerCaseTakenFromCliArg() {
        applicationProperties = new CliApplicationProperties(new String[]{
                ArgName.startDate.name() + "=2020-06-01"
        }).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.start_date_month_name);

        assertThat(actual).isEqualTo(Month.JUNE.name().toLowerCase());
    }

    @Test
    void givenEnd_date_month_name_whenValueFromPattern_thenReturnMonthNameLowerCase() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        String actual = applicationProperties.valueFromPattern(NamePatternValue.end_date_month_name);

        assertThat(actual).isEqualTo(LocalDate.now().getMonth().name().toLowerCase());
    }

    @Test
    void givenNoFetchTimeout_whenFetchTimeout_thenReturnDefault60() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        int actual = applicationProperties.fetchTimeout();

        assertThat(actual).isEqualTo(60);
    }

    @Test
    void givenFetchTimeoutFromCLI_whenFetchTimeout_thenReturnCliFetchTimeout() {
        applicationProperties = new CliApplicationProperties(new String[]{"fetchTimeout=12"});

        int actual = applicationProperties.fetchTimeout();

        assertThat(actual).isEqualTo(12);
    }

    @Test
    void givenFetchTimeoutFileAndCLI_whenFetchTimeout_thenReturnCliFetchTimeout() {
        String[] args = {"fetchTimeout=2"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfig();
        runConfig.setFetchTimeout(45);
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        int actual = applicationProperties.fetchTimeout();

        assertThat(actual).isEqualTo(2);
    }

    @Test
    void givenFetchTimeoutFromFile_whenFetchTimeout_thenReturnFetchTimeoutFromFile() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfig();
        runConfig.setFetchTimeout(13);
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        int actual = applicationProperties.fetchTimeout();

        assertThat(actual).isEqualTo(13);
    }

    @Test
    void givenFetchTimeoutFromFileAndOtherArgs_whenFetchTimeout_thenReturnFetchTimeoutFromFile() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        RunConfig runConfig = new RunConfig();
        runConfig.setFetchTimeout(13);
        applicationProperties.init(TestUtils.mockConfigurationDao(runConfig));

        int actual = applicationProperties.fetchTimeout();

        assertThat(actual).isEqualTo(13);
    }

    @Test
    void givenFetchAll_whenIsUploadItem_thenReturnDefault() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        boolean actual = applicationProperties.isUploadItem();

        assertThat(actual).isTrue();
    }

    @Test
    void givenFetchAllFromCLI_whenIsUploadItem_thenReturnCliUploadItem() {
        applicationProperties = new CliApplicationProperties(new String[]{"uploadItem=N"});

        boolean actual = applicationProperties.isUploadItem();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFileAndCLI_whenIsUploadItem_thenReturnCliUploadItem() {
        String[] args = {"uploadItem=n"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUploadItem(Boolean.TRUE);

        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUploadItem();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFromProperties_whenIsUploadItem_thenReturnUploadItemFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUploadItem(Boolean.FALSE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUploadItem();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllFromPropertiesAndOtherArgs_whenIsUploadItem_thenReturnUploadItemFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUploadItem(Boolean.FALSE);
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = applicationProperties.isUploadItem();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noGithubToken_when_githubToken_then_returnThrowNullPointerException() {
        applicationProperties = new CliApplicationProperties(new String[]{}).init();

        try {
            applicationProperties.githubToken();
            fail("Should throw NPE");
        } catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void given_githubTokenFromCLI_when_githubToken_then_returnCliGithubToken() {
        applicationProperties = new CliApplicationProperties(new String[]{"githubToken=1"});

        String actual = applicationProperties.githubToken();

        assertThat(actual).isEqualTo("1");
    }

    @Test
    void given_githubTokenFileAndCLI_when_githubToken_then_returnCliGithubToken() {
        String[] args = {"githubToken=cli"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setGithubToken("file");
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        String actual = applicationProperties.githubToken();

        assertThat(actual).isEqualTo("cli");
    }

    @Test
    void given_githubTokenFromProperties_when_githubToken_then_returnGithubTokenFromProperties() {
        String[] args = {};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setGithubToken("file");
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        String actual = applicationProperties.githubToken();

        assertThat(actual).isEqualTo("file");
    }

    @Test
    void given_githubTokenFromPropertiesAndOtherArgs_when_githubToken_then_returnGithubTokenFromProperties() {
        String[] args = {"author=test"};
        applicationProperties = new CliApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setGithubToken("file");
        applicationProperties.init(TestUtils.mockConfigurationDao(applicationConfig));

        String actual = applicationProperties.githubToken();

        assertThat(actual).isEqualTo("file");
    }
}