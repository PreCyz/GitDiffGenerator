package pg.gipter.settings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.dao.DaoConstants;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.settings.dto.NameSetting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static pg.gipter.TestUtils.mockPropertiesLoader;

class CliPreferredApplicationPropertiesTest {

    private CliPreferredApplicationProperties applicationProperties;

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void given_noAuthor_when_author_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("NO_AUTHORS_GIVEN"));
    }

    @Test
    void given_cliAuthor_when_author_then_returnCliAuthor() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"author=cliAuthor"}
        );

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("cliAuthor"));
    }

    @Test
    void given_cliAuthorAndFileAuthor_when_author_then_returnCliAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=cliAuthor"});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("cliAuthor"));
    }

    @Test
    void given_noCliAuthorAndFileAuthor_when_author_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("fileAuthor"));
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_author_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"startDate=2019-02-16"});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("fileAuthor"));
    }

    @Test
    void given_noGitAuthor_when_gitAuthor_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_cliGitAuthor_when_gitAuthor_then_returnCliAuthor() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"gitAuthor=cliAuthor"}
        );

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_cliGitAuthorAndFileGitAuthor_when_gitAuthor_then_returnCliAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"gitAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliGitAuthorAndFileAuthor_when_gitAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_gitAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=test"});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_noMercurialAuthor_when_mercurialAuthor_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_cliMercurialAuthor_when_mercurialAuthor_then_returnCliAuthor() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"mercurialAuthorAuthor=cliAuthor"}
        );

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_cliMercurialAuthorAndFileGitAuthor_when_mercurialAuthor_then_returnCliAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"mercurialAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliMercurialAuthorAndFileAuthor_when_mercurialAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_mercurialAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=test"});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_noSvnAuthor_when_svnAuthor_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_cliSvnAuthor_when_svnAuthor_then_returnCliAuthor() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"svnAuthor=cliAuthor"}
        );

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_cliSvnAuthorAndFileGitAuthor_when_svnAuthor_then_returnCliAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"svnAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliSvnAuthorAndFileAuthor_when_svnAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_svnAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=test"});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_noItemPath_when_itemPath_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("NO_ITEM_PATH_GIVEN");
    }

    @Test
    void given_itemPathFromCLI_when_itemPath_then_returnThatItemPath() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"itemPath=testItemPath"});

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("testItemPath" + File.separator);
    }

    @Test
    void given_itemPathFromPropertiesAndCLI_when_itemPath_then_returnItemPathFromCLI() {
        String[] args = {"itemPath=cliItemPath"};
        Properties props = new Properties();
        props.put("itemPath", "propertiesItemPath");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("cliItemPath" + File.separator);
    }

    @Test
    void given_itemPathFromProperties_when_itemPath_then_returnItemPathFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("itemPath", "propertiesItemPath");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("propertiesItemPath" + File.separator);
    }

    @Test
    void given_itemPathFromPropertiesAndOtherArgs_when_itemPath_then_returnItemPathFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("itemPath", "propertiesItemPath");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.itemPath();

        assertThat(actual).startsWith("propertiesItemPath");
    }

    @Test
    void given_noItemFileNamePrefix_when_itemFileNamePrefix_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_itemFileNamePrefixFromCLI_when_itemFileNamePrefix_then_returnThatCliItemFileNamePrefix() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"itemFileNamePrefix=cliItemFileNamePrefix"}
        );

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEqualTo("cliItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromFileAndCLI_when_itemPath_then_returnCliItemFileNamePrefix() {
        String[] args = {"itemFileNamePrefix=cliItemFileNamePrefix"};
        Properties props = new Properties();
        props.put("itemFileNamePrefix", "propertiesItemPath");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEqualTo("cliItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromProperties_when_itemFileNamePrefix_then_returnItemFileNamePrefixFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("itemFileNamePrefix", "propertiesItemFileNamePrefix");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.itemPath();

        assertThat(actual).contains("propertiesItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromPropertiesAndOtherArgs_when_itemFileNamePrefix_then_returnItemPathFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("itemFileNamePrefix", "propertiesItemFileNamePrefix");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.itemFileNamePrefix();

        assertThat(actual).isEqualTo("propertiesItemFileNamePrefix");
    }

    @Test
    void given_noProjectPaths_when_projectPaths_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsOnly("NO_PROJECT_PATH_GIVEN");
    }

    @Test
    void given_projectPathsFromCLI_when_projectPaths_then_returnCliProjectPaths() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"projectPath=cliProjectPath1,cliProjectPath2"}
        );

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("cliProjectPath1", "cliProjectPath2");
    }

    @Test
    void given_projectPathFileAndCLI_when_projectPaths_then_returnCliProjectPath() {
        String[] args = {"projectPath=cliProjectPath1,cliProjectPath2"};
        Properties props = new Properties();
        props.put("projectPath", "propertiesProjectPath1,propertiesProjectPath2");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("cliProjectPath1", "cliProjectPath2");
    }

    @Test
    void given_projectPathFromProperties_when_projectPaths_then_returnProjectPathFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("projectPath", "propertiesProjectPath1,propertiesProjectPath2");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("propertiesProjectPath1", "propertiesProjectPath2");
    }

    @Test
    void given_projectPathFromPropertiesAndOtherArgs_when_projectPaths_then_returnProjectPathFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("projectPath", "propertiesProjectPath1,propertiesProjectPath2");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        Set<String> actual = applicationProperties.projectPaths();

        assertThat(actual).containsExactly("propertiesProjectPath1", "propertiesProjectPath2");
    }

    @Test
    void given_noCommitterEmail_when_committerEmail_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_committerEmailFromCLI_when_committerEmail_then_returnCliCommitterEmail() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"committerEmail=test@email.cli"}
        );

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.cli");
    }

    @Test
    void given_committerEmailFileAndCLI_when_committerEmail_then_returnCliCommitterEmail() {
        String[] args = {"committerEmail=test@email.cli"};
        Properties props = new Properties();
        props.put("committerEmail", "test@email.properties");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.cli");
    }

    @Test
    void given_committerEmailFromProperties_when_committerEmail_then_returnCommitterEmailFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("committerEmail", "test@email.properties");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.properties");
    }

    @Test
    void given_committerEmailFromPropertiesAndOtherArgs_when_committerEmail_then_returnCommitterEmailFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("committerEmail", "test@email.properties");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.committerEmail();

        assertThat(actual).isEqualTo("test@email.properties");
    }

    @Test
    void given_noStartDate_when_startDate_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(7));
    }

    @Test
    void given_startDateFromCLI_when_startDate_then_returnCliStartDate() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"startDate=2019-02-01"}
        );

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_startDateFileAndCLI_when_startDate_then_returnCliStartDate() {
        String[] args = {"startDate=2019-02-01"};
        Properties props = new Properties();
        props.put("startDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_startDateFromProperties_when_startDate_then_returnStartDateFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("startDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_startDateFromPropertiesAndOtherArgs_when_startDate_then_returnStartDateFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("startDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void givenPeriodInDaysFromCli_when_startDate_then_returnStartDateBasedOnPeriodInDays() {
        String[] args = {"periodInDays=5"};
        applicationProperties = new CliPreferredApplicationProperties(args);

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(5));
    }

    @Test
    void givenPeriodInDaysFromCliAndProperties_when_startDate_then_returnStartDateBasedOnCliPeriodInDays() {
        String[] args = {"periodInDays=5"};
        Properties props = new Properties();
        props.put("periodInDays", "6");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(5));
    }

    @Test
    void givenPeriodInDaysFromProperties_when_startDate_then_returnStartDateBasedOnPropertiesPeriodInDays() {
        String[] args = {};
        Properties props = new Properties();
        props.put("periodInDays", "6");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(6));
    }

    @Test
    void given_noEndDate_when_endDate_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual).isEqualTo(LocalDate.now());
    }

    @Test
    void given_endDateFromCLI_when_endDate_then_returnCliEndDate() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"endDate=2019-02-01"}
        );

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_endDateFileAndCLI_when_endDate_then_returnCliEndDate() {
        String[] args = {"endDate=2019-02-01"};
        Properties props = new Properties();
        props.put("endDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-01");
    }

    @Test
    void given_endDateFromProperties_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("endDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_endDateFromPropertiesAndOtherArgs_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("endDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_noCodeProtection_when_uploadType_then_returnCodeProtection() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        UploadType actual = applicationProperties.uploadType();

        assertThat(actual).isEqualTo(UploadType.SIMPLE);
    }

    @Test
    void given_uploadTypeFromCLI_when_uploadType_then_returnCliCodeProtection() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"uploadType=protected"}
        );

        UploadType actual = applicationProperties.uploadType();

        assertThat(actual).isEqualTo(UploadType.PROTECTED);
    }

    @Test
    void given_uploadTypeFileAndCLI_when_uploadType_then_returnCliCodeProtection() {
        String[] args = {"uploadType=PROTECTED"};
        Properties props = new Properties();
        props.put("uploadType", "statement");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        UploadType actual = applicationProperties.uploadType();

        assertThat(actual).isEqualTo(UploadType.PROTECTED);
    }

    @Test
    void given_uploadTypeFromProperties_when_uploadType_then_returnCodeProtectionFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("uploadType", "STATEMENT");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        UploadType actual = applicationProperties.uploadType();

        assertThat(actual).isEqualTo(UploadType.STATEMENT);
    }

    @Test
    void given_uploadTypeFromPropertiesAndOtherArgs_when_uploadType_then_returnCodeProtectionFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("uploadType", "statement");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        UploadType actual = applicationProperties.uploadType();

        assertThat(actual).isEqualTo(UploadType.STATEMENT);
    }

    @Test
    void given_noConfirmationWindow_when_isConfirmationWindow_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isFalse();
    }

    @Test
    void given_confirmationWindowFromCLI_when_isConfirmationWindow_then_returnCliConfirmationWindow() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"confirmationWindow=y"}
        );

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowFileAndCLI_when_isConfirmationWindow_then_returnCliConfirmationWindow() {
        String[] args = {"confirmationWindow=y"};
        Properties props = new Properties();
        props.put("confirmationWindow", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowFromProperties_when_isConfirmationWindow_then_returnConfirmationWindowFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("confirmationWindow", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowFromPropertiesAndOtherArgs_when_isConfirmationWindow_then_returnConfirmationWindowFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("confirmationWindow", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_noToolkitUsername_when_toolkitUsername_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("NO_TOOLKIT_USERNAME_GIVEN");
    }

    @Test
    void given_toolkitUsernameFromCLI_when_toolkitUsername_then_returnCliToolkitUsername() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitUsername=cliUserName"}
        );

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("cliUserName".toUpperCase());
    }

    @Test
    void given_toolkitUsernameFileAndCLI_when_toolkitUsername_then_returnCliToolkitUsername() {
        String[] args = {"toolkitUsername=cliUserName"};
        Properties props = new Properties();
        props.put("toolkitUsername", "propertiesUserName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("cliUserName".toUpperCase());
    }

    @Test
    void given_toolkitUsernameFromProperties_when_toolkitUsername_then_returnToolkitUsernameFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitUsername", "propertiesUserName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("propertiesUserName".toUpperCase());
    }

    @Test
    void given_toolkitUsernameFromPropertiesAndOtherArgs_when_toolkitUsername_then_returnToolkitUsernameFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitUsername", "propertiesUserName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUsername();

        assertThat(actual).isEqualTo("propertiesUserName".toUpperCase());
    }

    @Test
    void given_noToolkitPassword_when_toolkitPassword_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.toolkitPassword();

        assertThat(actual).isEqualTo("NO_TOOLKIT_PASSWORD_GIVEN");
    }

    @Test
    void given_toolkitPasswordFromCLI_when_toolkitPassword_then_returnCliToolkitPassword() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitPassword=cliPassword"}
        );

        String actual = applicationProperties.toolkitPassword();

        assertThat(actual).isEqualTo("cliPassword");
    }

    @Test
    void given_toolkitPasswordFileAndCLI_when_toolkitPassword_then_returnCliToolkitPassword() {
        String[] args = {"toolkitPassword=cliPassword"};
        Properties props = new Properties();
        props.put("toolkitPassword", "propertiesPassword");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitPassword();

        assertThat(actual).isEqualTo("cliPassword");
    }

    @Test
    void given_toolkitPasswordFromProperties_when_toolkitPassword_then_returnToolkitPasswordFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitPassword", "propertiesPassword");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitPassword();

        assertThat(actual).isEqualTo("propertiesPassword");
    }

    @Test
    void given_toolkitPasswordFromPropertiesAndOtherArgs_when_toolkitPassword_then_returnToolkitPasswordFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitPassword", "propertiesPassword");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitPassword();

        assertThat(actual).isEqualTo("propertiesPassword");
    }

    @Test
    void given_noToolkitDomain_when_toolkitDomain_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.toolkitDomain();

        assertThat(actual).isEqualTo("NCDMZ");
    }

    @Test
    void given_toolkitDomainFromCLI_when_toolkitDomain_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitDomain=cliDomain"}
        );

        String actual = applicationProperties.toolkitDomain();

        assertThat(actual).isEqualTo("NCDMZ");
    }

    @Test
    void given_toolkitDomainFileAndCLI_when_toolkitDomain_then_returnDefault() {
        String[] args = {"toolkitDomain=cliDomain"};
        Properties props = new Properties();
        props.put("toolkitDomain", "propertiesDomain");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitDomain();

        assertThat(actual).isEqualTo("NCDMZ");
    }

    @Test
    void given_toolkitDomainFromProperties_when_toolkitDomain_then_returnToolkitDomainFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitDomain", "propertiesDomain");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitDomain();

        assertThat(actual).isEqualTo("propertiesDomain");
    }

    @Test
    void given_toolkitDomainFromPropertiesAndOtherArgs_when_toolkitDomain_then_returnToolkitDomainFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitDomain", "propertiesDomain");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitDomain();

        assertThat(actual).isEqualTo("propertiesDomain");
    }

    @Test
    void given_noToolkitUrl_when_toolkitUrl_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com");
    }

    @Test
    void given_toolkitUrlFromCLI_when_toolkitUrl_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitUrl=cliUrl"}
        );

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com");
    }

    @Test
    void given_toolkitUrlFileAndCLI_when_toolkitUrl_then_returnDefault() {
        String[] args = {"toolkitUrl=cliUrl"};
        Properties props = new Properties();
        props.put("toolkitUrl", "propertiesUrl");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com");
    }

    @Test
    void given_toolkitUrlFromProperties_when_toolkitUrl_then_returnToolkitUrlFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitUrl", "propertiesUrl");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("propertiesUrl");
    }

    @Test
    void given_toolkitUrlFromPropertiesAndOtherArgs_when_toolkitUrl_then_returnToolkitUrlFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitUrl", "propertiesUrl");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("propertiesUrl");
    }

    @Test
    void given_noToolkitListName_when_toolkitListName_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromCLI_when_toolkitListName_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitCopyListName=cliListName"}
        );

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFileAndCLI_when_toolkitListName_then_returnDefault() {
        String[] args = {"toolkitCopyListName=cliListName"};
        Properties props = new Properties();
        props.put("toolkitCopyListName", "propertiesListName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromProperties_when_toolkitListName_then_returnToolkitListNameFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitCopyListName", "propertiesListName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("propertiesListName");
    }

    @Test
    void given_toolkitListNameFromPropertiesAndOtherArgs_when_toolkitListName_then_returnToolkitListNameFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitCopyListName", "propertiesListName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitCopyListName();

        assertThat(actual).isEqualTo("propertiesListName");
    }

    @Test
    void given_noToolkitUserFolder_when_toolkitUserFolder_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/NO_TOOLKIT_USERNAME_GIVEN");
    }

    @Test
    void given_toolkitUserNameFromCLI_when_toolkitUserFolder_then_returnProperFolder() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitUsername=cliUserName"}
        );

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/CLIUSERNAME");
    }

    @Test
    void given_toolkitUserFolderFileAndCLI_when_toolkitUserFolder_then_returnWithCliUser() {
        String[] args = {"toolkitUsername=cliUserName"};
        Properties props = new Properties();
        props.put("toolkitUsername", "propertiesUserName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/CLIUSERNAME");
    }

    @Test
    void given_toolkitUsernameFromProperties_when_toolkitUserFolder_then_returnWithToolkitUsernameFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitUsername", "propertiesUserName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/PROPERTIESUSERNAME");
    }

    @Test
    void given_toolkitUserNameFromPropertiesAndOtherArgs_when_toolkitUserFolder_then_returnProperWithUserNameFromProperties() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitUsername", "propertiesUsername");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/PROPERTIESUSERNAME");
    }

    @Test
    void given_noSkipRemote_when_isSkipRemote_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isTrue();
    }

    @Test
    void given_skipRemoteFromCLI_when_isSkipRemote_then_returnCliSkipRemote() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"skipRemote=N"});

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_skipRemoteFileAndCLI_when_isSkipRemote_then_returnCliSkipRemote() {
        String[] args = {"skipRemote=n"};
        Properties props = new Properties();
        props.put("skipRemote", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_skipRemoteFromProperties_when_isSkipRemote_then_returnSkipRemoteFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("skipRemote", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_skipRemoteFromPropertiesAndOtherArgs_when_isSkipRemote_then_returnSkipRemoteFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("skipRemote", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noPreferredArgSource_when_preferredArgSource_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.CLI);
    }

    @Test
    void given_preferredArgSourceFromCLI_when_preferredArgSource_then_returnCliPreferredArgSource() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"preferredArgSource=file"});

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.FILE);
    }

    @Test
    void given_preferredArgSourceFileAndCLI_when_preferredArgSource_then_returnCliPreferredArgSource() {
        String[] args = {"preferredArgSource=FILE"};
        Properties props = new Properties();
        props.put("preferredArgSource", "cli");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.FILE);
    }

    @Test
    void given_preferredArgSourceFromProperties_when_preferredArgSource_then_returnPreferredArgSourceCLI() {
        String[] args = {};
        Properties props = new Properties();
        props.put("preferredArgSource", "FILE");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.CLI);
    }

    @Test
    void given_preferredArgSourceFromPropertiesAndOtherArgs_when_preferredArgSource_then_returnPreferredArgSourceCLI() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("preferredArgSource", "FILE");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        PreferredArgSource actual = applicationProperties.preferredArgSource();

        assertThat(actual).isEqualTo(PreferredArgSource.CLI);
    }

    @Test
    void given_versionTxt_when_version_then_returnVersion() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.version();

        assertThat(actual).isNotEmpty();
        assertThat(actual).isNotBlank();
    }

    @Test
    void given_noUseUI_when_isUseUI_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFromCLI_when_isUseUI_then_returnCliUseUI() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"useUI=T"});

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFileAndCLI_when_isUseUI_then_returnCliUseUI() {
        String[] args = {"useUI=t"};
        Properties props = new Properties();
        props.put("useUI", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFromProperties_when_isUseUI_then_returnUseUIFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("useUI", "t");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIFromPropertiesAndOtherArgs_when_isUseUI_then_returnUseUIFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("useUI", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNoToolkitUserCliAndNoFileToolkitUser_whenToolkitUserFolder_then_returnDefault() {
        String[] args = {};
        applicationProperties = new CliPreferredApplicationProperties(args);

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + ArgName.toolkitUsername.defaultValue());
    }

    @Test
    void givenToolkitUserCliAndNoFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUser() {
        String[] args = {"toolkitUsername=xxx"};
        applicationProperties = new CliPreferredApplicationProperties(args);

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "XXX");
    }

    @Test
    void givenToolkitUserCliAndFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUser() {
        String[] args = {"toolkitUsername=xxx"};
        Properties props = new Properties();
        props.put("toolkitUsername", "aaa");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "XXX");
    }

    @Test
    void givenNoToolkitUserCliAndFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUser() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitUsername", "aaa");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "AAA");
    }

    @Test
    void givenToolkitUserCliAndFileCustomUserFolder_whenToolkitUserFolder_then_returnUserFolderWithCliCustomUserFolder() {
        String[] args = {"toolkitUsername=qqq"};
        Properties props = new Properties();
        props.put("toolkitCustomUserFolder", "aaa");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "QQQ");
    }

    @Test
    void givenNoProjectPaths_whenToolkitProjectListNames_thenReturnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsOnly("Deliverables");
    }

    @Test
    void givenToolkitProjectListNamesFromCLI_whenToolkitProjectListNames_thenReturnCliToolkitProjectListNames() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitProjectListNames=name1,name2"}
        );

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("name1", "name2");
    }

    @Test
    void givenToolkitProjectListNamesFileAndCLI_whenToolkitProjectListNames_thenReturnCliToolkitProjectListNames() {
        String[] args = {"toolkitProjectListNames=cli1,cli2"};
        Properties props = new Properties();
        props.put("toolkitProjectListNames", "properties1,properties2");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("cli1", "cli2");
    }

    @Test
    void givenToolkitProjectListNamesFromProperties_whenToolkitProjectListNames_thenReturnPropertiesToolkitProjectListNames() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitProjectListNames", "properties1,properties2");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("properties1", "properties2");
    }

    @Test
    void givenToolkitProjectListNamesFromPropertiesAndOtherArgs_whenToolkitProjectListNames_thenReturnPropertiesToolkitProjectListNames() {
        String[] args = {"uploadType=statement"};
        Properties props = new Properties();
        props.put("toolkitProjectListNames", "properties1,properties2");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        Set<String> actual = applicationProperties.toolkitProjectListNames();

        assertThat(actual).containsExactly("properties1", "properties2");
    }

    @Test
    void givenNoDeleteDownloadedFiles_whenIsDeleteDownloadedFiles_thenReturnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenDeleteDownloadedFilesFromCLI_whenIsDeleteDownloadedFiles_thenReturnCliDeleteDownloadedFiles() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"deleteDownloadedFiles=y"}
        );

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenDeleteDownloadedFilesFileAndCLI_whenIsDeleteDownloadedFiles_thenReturnCliDeleteDownloadedFiles() {
        String[] args = {"deleteDownloadedFiles=y"};
        Properties props = new Properties();
        props.put("deleteDownloadedFiles", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenDeleteDownloadedFilesFromProperties_whenIsDeleteDownloadedFiles_thenReturnDeleteDownloadedFilesFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("deleteDownloadedFiles", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isFalse();
    }

    @Test
    void givenDeleteDownloadedFilesFromPropertiesAndOtherArgs_whenIsDeleteDownloadedFiles_thenReturnDeleteDownloadedFilesFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("deleteDownloadedFiles", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isDeleteDownloadedFiles();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNoUseAsFileName_whenIsUseAsFileName_thenReturnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        boolean actual = applicationProperties.isUseAsFileName();

        assertThat(actual).isFalse();
    }

    @Test
    void givenUseAsFileNameFromCLI_whenIsUseAsFileName_thenReturnCliUseAsFileName() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"useAsFileName=y"}
        );

        boolean actual = applicationProperties.isUseAsFileName();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUseAsFileNameFileAndCLI_whenIsUseAsFileName_thenReturnCliUseAsFileName() {
        String[] args = {"useAsFileName=y"};
        Properties props = new Properties();
        props.put("useAsFileName", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUseAsFileName();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUseAsFileNameFromProperties_whenIsUseAsFileName_thenReturnUseAsFileNameFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("useAsFileName", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUseAsFileName();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUseAsFileNameFromPropertiesAndOtherArgs_whenIsUseAsFileName_thenReturnUseAsFileNameFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("useAsFileName", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUseAsFileName();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNoConfigurationName_whenConfigurationName_thenReturnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo(ArgName.configurationName.defaultValue());
    }

    @Test
    void givenCliConfigurationName_whenConfigurationName_thenReturnCliConfigurationName() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"configurationName=cliAuthor"}
        );

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void givenCliConfigurationNameAndFileConfigurationName_whenConfigurationName_then_returnCliConfigurationName() {
        Properties properties = new Properties();
        properties.setProperty("configurationName", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"configurationName=cliAuthor"});
        applicationProperties.init(new String[]{"configurationName=fileAuthor"}, loader);

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void givenNoCliConfigurationNameAndFileConfigurationName_whenConfigurationName_thenReturnFileConfigurationName() {
        Properties properties = new Properties();
        properties.setProperty("configurationName", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"configurationName=fileAuthor"}, loader);

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void givenFileConfigurationNameAndOtherArgs_whenConfigurationName_thenReturnFileConfigurationName() {
        Properties properties = new Properties();
        properties.setProperty("configurationName", "fileAuthor");
        PropertiesDao loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=test"});
        applicationProperties.init(new String[]{"configurationName=fileAuthor"}, loader);

        String actual = applicationProperties.configurationName();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void givenNoNamePatternValue_whenValueFromPattern_thenReturnEmptyString() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.valueFromPattern(null);

        assertThat(actual).isEmpty();
    }

    @Test
    void givenCURRENT_DATE_whenValueFromPattern_thenReturnNow() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_DATE);

        assertThat(actual).isEqualTo(LocalDate.now().format(ApplicationProperties.yyyy_MM_dd));
    }

    @Test
    void givenCURRENT_YEAR_whenValueFromPattern_thenReturnYear() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_YEAR);

        assertThat(actual).isEqualTo(String.valueOf(LocalDate.now().getYear()));
    }

    @Test
    void givenCURRENT_MONTH_whenValueFromPattern_thenReturnMonthName() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_MONTH_NAME);

        assertThat(actual).isEqualTo(LocalDate.now().getMonth().name());
    }

    @Test
    void givenCURRENT_MONTH_NUMBER_whenValueFromPattern_thenReturnMonthNumber() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_MONTH_NUMBER);

        assertThat(actual).isEqualTo(String.valueOf(LocalDate.now().getMonthValue()));
    }

    @Test
    void givenCURRENT_WEEK_NUMBER_whenValueFromPattern_thenReturnCurrentWeekNumber() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.CURRENT_WEEK_NUMBER);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        assertThat(actual).isEqualTo(String.valueOf(LocalDate.now().get(weekFields.weekOfWeekBasedYear())));
    }

    @Test
    void givenSTART_DATE_whenValueFromPattern_thenReturnStartDate() {
        String startDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"startDate=" + startDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE);

        assertThat(actual).isEqualTo(startDate);
    }

    @Test
    void givenSTART_DATE_YEAR_whenValueFromPattern_thenReturnStartDateYear() {
        String startDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"startDate=" + startDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_YEAR);

        assertThat(actual).isEqualTo("2018");
    }

    @Test
    void givenSTART_DATE_MONTH_NAME_whenValueFromPattern_thenReturnStartDateMonthName() {
        String startDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"startDate=" + startDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_MONTH_NAME);

        assertThat(actual).isEqualTo(Month.JUNE.name());
    }

    @Test
    void givenSTART_DATE_MONTH_NUMBER_whenValueFromPattern_thenReturnStartDateMonthNumber() {
        String startDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"startDate=" + startDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_MONTH_NUMBER);

        assertThat(actual).isEqualTo(String.valueOf(Month.JUNE.getValue()));
    }

    @Test
    void givenSTART_DATE_WEEK_NUMBER_whenValueFromPattern_thenReturnStartDateMonthName() {
        String startDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"startDate=" + startDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.START_DATE_WEEK_NUMBER);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        assertThat(actual).isEqualTo(String.valueOf(LocalDate.parse(startDate, ApplicationProperties.yyyy_MM_dd).get(weekFields.weekOfWeekBasedYear())));
    }

    @Test
    void givenEND_DATE_whenValueFromPattern_thenReturnEndDate() {
        String endDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE);

        assertThat(actual).isEqualTo(endDate);
    }

    @Test
    void givenEND_DATE_YEAR_whenValueFromPattern_thenReturnEndDateYear() {
        String endDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_YEAR);

        assertThat(actual).isEqualTo("2018");
    }

    @Test
    void givenEND_DATE_MONTH_NAME_whenValueFromPattern_thenReturnEndDateMonthName() {
        String endDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_MONTH_NAME);

        assertThat(actual).isEqualTo(Month.JUNE.name());
    }

    @Test
    void givenEND_DATE_MONTH_NUMBER_whenValueFromPattern_thenReturnEndDateMonthNumber() {
        String endDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_MONTH_NUMBER);

        assertThat(actual).isEqualTo(String.valueOf(Month.JUNE.getValue()));
    }

    @Test
    void givenEND_DATE_WEEK_NUMBER_whenValueFromPattern_thenReturnEndDateMonthName() {
        String endDate = "2018-06-12";
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"endDate=" + endDate});

        String actual = applicationProperties.valueFromPattern(NamePatternValue.END_DATE_WEEK_NUMBER);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        assertThat(actual).isEqualTo(String.valueOf(LocalDate.parse(endDate, ApplicationProperties.yyyy_MM_dd).get(weekFields.weekOfWeekBasedYear())));
    }

    @Test
    void givenFileNameSettingsAndFileNamePrefix_whenFileName_thenReturnFileNameFromPattern() {
        NameSetting fns = new NameSetting();
        fns.addSetting("{%1}", NamePatternValue.START_DATE_MONTH_NAME);
        fns.addSetting("{%2}", NamePatternValue.START_DATE_YEAR);
        DaoFactory.getPropertiesDao().saveFileNameSetting(fns);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{
                ArgName.itemFileNamePrefix + "=" + "my-{%1}-{%2}-name",
                ArgName.startDate + "=2018-06-12"
        });

        String actual = applicationProperties.fileName();

        assertThat(actual).startsWith("my-JUNE-2018-name");
    }

    @Test
    void givenNoUpgradeFinished_whenIsUpgradeFinished_thenReturnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isFalse();
    }

    @Test
    void givenUpgradeFinishedFromCLI_whenIsUpgradeFinished_thenReturnCliUpgradeFinished() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"upgradeFinished=y"}
        );

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedFileAndCLI_whenIsUpgradeFinished_thenReturnCliUpgradeFinished() {
        String[] args = {"upgradeFinished=y"};
        Properties props = new Properties();
        props.put("upgradeFinished", "n");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedFromProperties_whenIsUpgradeFinished_thenReturnUpgradeFinishedFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("upgradeFinished", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedFromPropertiesAndOtherArgs_whenIsUpgradeFinished_thenReturnUseAsFileNameFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("upgradeFinished", "y");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        boolean actual = applicationProperties.isUpgradeFinished();

        assertThat(actual).isTrue();
    }
}