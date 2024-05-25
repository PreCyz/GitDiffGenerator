package pg.gipter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pg.gipter.TestUtils;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.CookiesService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static pg.gipter.core.FileApplicationProperties.yyyy_MM_dd;

class FilePreferredApplicationPropertiesTest {

    private FileApplicationProperties appProps;

    @BeforeEach
    void setUp() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
            DaoFactory.getCachedConfiguration().resetCache();
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void given_authorFromCommandLine_when_authors_then_returnThatAuthor() {
        appProps = new FileApplicationProperties(new String[]{"author=author1"});

        Set<String> actual = appProps.authors();

        assertThat(actual).hasSize(1);
        assertThat(actual).containsExactly("author1");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_authors_then_returnAuthorsFromProperties() {
        String[] args = {"author=testAuthor"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withAuthor("propsAuthor1,propsAuthor2").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = appProps.authors();

        assertThat(actual).hasSize(2);
        assertThat(actual).containsExactly("propsAuthor1", "propsAuthor2");
    }

    @Test
    void givenCommitterEmailCommandLineAndNoAuthor_whenAuthors_thenReturnEmptyCollection() {
        String[] args = {ArgName.committerEmail.name() + "=testAuthor"};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        Set<String> actual = appProps.authors();

        assertThat(actual).isEmpty();
    }

    @Test
    void givengitAuthorCommandLineAndNoAuthor_whenAuthors_thenReturnEmptyCollection() {
        String[] args = {ArgName.gitAuthor.name() + "=testAuthor"};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        Set<String> actual = appProps.authors();

        assertThat(actual).isEmpty();
    }

    @Test
    void givenMercurialAuthorCommandLineAndNoAuthor_whenAuthors_thenReturnEmptyCollection() {
        String[] args = {ArgName.mercurialAuthor.name() + "=testAuthor"};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        Set<String> actual = appProps.authors();

        assertThat(actual).isEmpty();
    }

    @Test
    void givenSvnAuthorCommandLineAndNoAuthor_whenAuthors_thenReturnEmptyCollection() {
        String[] args = {ArgName.svnAuthor.name() + "=testAuthor"};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        Set<String> actual = appProps.authors();

        assertThat(actual).isEmpty();
    }

    @Test
    void givenAllOtherAuthorsCommandLineAndNoAuthor_whenAuthors_thenReturnEmptyCollection() {
        String[] args = {
                ArgName.committerEmail.name() + "=testAuthor",
                ArgName.gitAuthor.name() + "=testAuthor",
                ArgName.mercurialAuthor.name() + "=testAuthor",
                ArgName.svnAuthor.name() + "=testAuthor",
        };
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        Set<String> actual = appProps.authors();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_authorFromCommandLine_when_gitAuthor_then_returnThatAuthor() {
        appProps = new FileApplicationProperties(new String[]{"gitAuthor=testAuthor"});

        String actual = appProps.gitAuthor();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_gitAuthor_then_returnAuthorFromProperties() {
        String[] args = {"gitAuthor=testAuthor"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withGitAuthor("propsAuthor").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.gitAuthor();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void given_authorFromCommandLine_when_mercurialAuthor_then_returnThatAuthor() {
        appProps = new FileApplicationProperties(new String[]{"mercurialAuthor=testAuthor"});

        String actual = appProps.mercurialAuthor();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_mercurialAuthor_then_returnAuthorFromProperties() {
        String[] args = {"mercurialAuthor=testAuthor"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withMercurialAuthor("propsAuthor").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.mercurialAuthor();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void given_authorFromCommandLine_when_svnAuthor_then_returnThatAuthor() {
        appProps = new FileApplicationProperties(new String[]{"svnAuthor=testAuthor"});

        String actual = appProps.svnAuthor();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_svnAuthor_then_returnAuthorFromProperties() {
        String[] args = {"svnAuthor=testAuthor"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withSvnAuthor("propsAuthor").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.svnAuthor();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void given_itemPathFromCommandLine_when_itemPath_then_returnThatItemPath() {
        appProps = new FileApplicationProperties(new String[]{"itemPath=testItemPath"});

        String actual = appProps.itemPath();

        assertThat(actual).startsWith(Paths.get("testItemPath").toString());
    }

    @Test
    void given_itemPathFromPropertiesAndCommandLine_when_itemPath_then_returnItemPathFromProperties() {
        String[] args = {"itemPath=testItemPath"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withItemPath("propertiesItemPath").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.itemPath();

        assertThat(actual).startsWith(Paths.get("propertiesItemPath").toString());
    }

    @Test
    void given_defaultParams_when_fileName_then_returnThatFileName() {
        appProps = new FileApplicationProperties(new String[]{});
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo(fileName);
    }

    @Test
    void given_statement_when_fileName_then_returnFileNameForStatement() {
        appProps = new FileApplicationProperties(new String[]{
                "uploadType=statement"
        });
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo(fileName);
    }

    @Test
    void given_endDateNotNow_when_fileName_then_returnYearMonthPeriod() {
        appProps = new FileApplicationProperties(new String[]{
                "startDate=2018-09-19",
                "endDate=2018-10-19"
        });
        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("2018-october-20180919-20181019.txt");
    }

    @Test
    void givenPropertiesFileAndEndDateFromDeepPast_whenFileName_thenReturnWellBuildFileName() {
        String[] args = {};
        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.STATEMENT)
                .withStartDate(LocalDate.of(2017, 10, 19))
                .withEndDate(LocalDate.of(2017, 12, 20))
                .withItemFileNamePrefix("custom")
                .create();
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("custom.txt");
    }

    @Test
    void given_commandLinePropsAndEndDateFromDeepPast_when_fileName_then_returnWellBuildFileName() {
        String[] args = {
                "uploadType=STATEMENT",
                "startDate=2017-10-19",
                "endDate=2017-12-20",
                "itemFileNamePrefix=custom",
                "toolkitUsername=xxx",
        };
        appProps = new FileApplicationProperties(args);

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("custom.txt");
    }

    @Test
    void given_itemFileNameFromCommandLine_when_fileName_then_returnThatFileName() {
        appProps = new FileApplicationProperties(new String[]{"itemFileNamePrefix=fileName"});

        String actual = appProps.fileName();

        assertThat(actual).startsWith("fileName");
    }

    @Test
    void given_itemFileNameAndStartDateAndEndDateFromCommandLine_when_fileName_then_returnBuildFileName() {
        appProps = new FileApplicationProperties(new String[]{"itemFileNamePrefix=fileName", "startDate=2018-10-07", "endDate=2018-10-14"});

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("fileName.txt");
    }

    @Test
    void given_uploadTypeToolkitDocs_whenFileName_thenReturnFileNameWithZip() {
        appProps = new FileApplicationProperties(new String[]{"itemType=toolkit_docs"});

        String actual = appProps.fileName();

        assertThat(actual).endsWith("zip");
    }

    @Test
    void givenUseAsFileNameYAndFileNamePrefixAndToolkitDocs_whenFileName_thenReturnFileNamePrefixAsNameWithZip() {
        appProps = new FileApplicationProperties(new String[]{
                ArgName.itemType.name() + "=" + ItemType.TOOLKIT_DOCS.name(),
                ArgName.itemFileNamePrefix + "=my_custom_name",
        });

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("my_custom_name.zip");
    }

    @Test
    void givenUseAsFileNameYAndFileNamePrefix_whenFileName_thenReturnFileNamePrefixAsNameWithTxt() {
        appProps = new FileApplicationProperties(new String[]{
                ArgName.itemFileNamePrefix + "=my_custom_name",
        });

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("my_custom_name.txt");
    }

    @Test
    void given_periodInDaysAndStartDate_when_startDate_then_returnStartDate() {
        appProps = new FileApplicationProperties(new String[]{"periodInDays=16","startDate=2018-10-18"});

        LocalDate actual = appProps.startDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-18");
    }

    @Test
    void given_onlyPeriodInDays_when_startDate_then_returnNowMinusPeriodInDays() {
        appProps = new FileApplicationProperties(new String[]{"periodInDays=16"});

        LocalDate actual = appProps.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(16));
    }

    @Test
    void given_startDateFromPropertiesAndCommandLine_when_startDate_then_returnStartDateFromProperties() {
        String[] args = {"startDate=2018-10-18"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withStartDate(LocalDate.of(2018, 10, 19)).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = appProps.startDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-19");
    }

    @Test
    void givenStartDateFromCliAndPeriodInDaysFromProperties_whenStartDate_thenReturnStartDateFromProperties() {
        String[] args = {"startDate=2018-10-18"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withPeriodInDays(12).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = appProps.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(12));
    }

    @Test
    void given_itemFileNamePrefix_when_itemFileNamePrefix_then_returnThatItemFileNamePrefix() {
        appProps = new FileApplicationProperties(new String[]{"itemFileNamePrefix=testItemFileNamePrefix"});

        String actual = appProps.itemFileNamePrefix();

        assertThat(actual).isEqualTo("testItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromPropertiesAndCommandLine_when_startDate_then_returnItemFileNamePrefixFromProperties() {
        String[] args = {"itemFileNamePrefix=testItemFileNamePrefix"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withItemFileNamePrefix("propsItemFileNamePrefix").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.itemFileNamePrefix();

        assertThat(actual).isEqualTo("propsItemFileNamePrefix");
    }

    @Test
    void given_toolkitUsername_when_toolkitUsername_then_returnNowMinusPeriodInDays() {
        appProps = new FileApplicationProperties(new String[]{"toolkitUsername=userName"});

        String actual = appProps.toolkitUsername();

        assertThat(actual).isEqualTo("USERNAME");
    }

    @Test
    void given_endDate_when_endDate_then_returnThatEndDate() {
        appProps = new FileApplicationProperties(new String[]{"endDate=2018-10-19"});

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-19");
    }

    @Test
    void given_noEndDateInAppPropertiesAndEndDateFromCliArgs_when_endDate_then_returnEndDateFromCliArgs() {
        String[] args = {"startDate=2018-09-19", "endDate=2018-10-15"};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-15");
    }

    @Test
    void given_endDateInAppPropertiesAndInCommandLineArgs_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {"startDate=2018-09-19", "endDate=2018-10-15"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withEndDate(LocalDate.of(2018, 10, 19)).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-19");
    }

    @Test
    void givenEmptyEndDateInAppPropertiesAndInCommandLineArgs_whenEndDate_thenReturnEndDateFromProperties() {
        String[] args = {"startDate=2018-09-19", "endDate=2018-10-15"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withEndDate(null).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-15");
    }

    @Test
    void given_noEndDate_when_endDate_then_returnNow() {
        appProps = new FileApplicationProperties(new String[]{});

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo(LocalDate.now().format(yyyy_MM_dd));
    }

    @Test
    void given_wrongEndDate_when_endDate_then_throwDateTimeException() {
        try {
            appProps = new FileApplicationProperties(new String[]{"endDate=2018-02-30"});

            appProps.endDate();
            fail("Should throw DateTimeException.");
        } catch (DateTimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid date 'FEBRUARY 30'");
        }
    }

    @Test
    void given_wrongStartDate_when_startDate_then_throwDateTimeException() {
        try {
            appProps = new FileApplicationProperties(new String[]{"endDate=2018-02-30"});

            appProps.endDate();
            fail("Should throw DateTimeException.");
        } catch (DateTimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid date 'FEBRUARY 30'");
        }
    }

    @Test
    void given_projectPath_when_projectPaths_then_returnSetWithThatProjectPath() {
        appProps = new FileApplicationProperties(new String[]{"projectPath=Proj1"});

        Set<String> actual = appProps.projectPaths();

        assertThat(actual).containsExactly("Proj1");
    }

    @Test
    void given_projectPathFromPropertiesAndCommandLine_when_projectPaths_then_returnSetWithProjectPathFromProperties() {
        String[] args = {"projectPath=Proj1,Proj2"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withProjectPath("Proj3").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        Set<String> actual = appProps.projectPaths();

        assertThat(actual).containsExactly("Proj3");
    }

    @Test
    void given_noPeriodInDaysFromCommandLine_when_periodInDays_then_returnDefaultValue7() {
        appProps = new FileApplicationProperties(new String[]{});

        int actual = appProps.periodInDays();

        assertThat(actual).isEqualTo(7);
    }

    @Test
    void given_periodInDaysFromCommandLine_when_periodInDays_then_returnThatPeriodInDays() {
        appProps = new FileApplicationProperties(new String[]{"periodInDays=1"});

        int actual = appProps.periodInDays();

        assertThat(actual).isEqualTo(1);
    }

    @Test
    void given_periodInDaysFromPropertiesAndCommandLine_when_periodInDays_then_returnPeriodInDaysFromProperties() {
        String[] args = {"periodInDays=1"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withPeriodInDays(2).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        int actual = appProps.periodInDays();

        assertThat(actual).isEqualTo(2);
    }

    @Test
    void given_committerEmailFromCommandLine_when_committerEmail_then_returnThatCommitterEmail() {
        appProps = new FileApplicationProperties(new String[]{"committerEmail=testCommitterEmail"});

        String actual = appProps.committerEmail();

        assertThat(actual).isEqualTo("testCommitterEmail");
    }

    @Test
    void given_committerEmailFromPropertiesAndCommandLine_when_committerEmail_then_returnCommitterEmailFromProperties() {
        String[] args = {"committerEmail=testCommitterEmail"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withCommitterEmail("propsCommitterEmail").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.committerEmail();

        assertThat(actual).isEqualTo("propsCommitterEmail");
    }

    @Test
    void given_noCodeProtection_when_uploadType_then_returnDefaultValueNONE() {
        appProps = new FileApplicationProperties(new String[]{});

        ItemType actual = appProps.itemType();

        assertThat(actual).isEqualTo(ItemType.SIMPLE);
    }

    @Test
    void givenItemType_whenItemType_thenReturnThatCodeProtection() {
        appProps = new FileApplicationProperties(new String[]{"itemType=protected"});

        ItemType actual = appProps.itemType();

        assertThat(actual).isEqualTo(ItemType.PROTECTED);
    }

    @Test
    void givenItemTypeFromPropertiesAndCommandLine_whenItemType_thenReturnCodeProtectionFromProperties() {
        String[] args = {"uploadType=Simple"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withItemType(ItemType.STATEMENT).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        ItemType actual = appProps.itemType();

        assertThat(actual).isEqualTo(ItemType.STATEMENT);
    }

    @Test
    void givenToolkitUsernameAndPassword_whenIsToolkitPropertiesSet_thenReturnTrue() {
        try (MockedStatic<CookiesService> utilities = Mockito.mockStatic(CookiesService.class)) {
            utilities.when(CookiesService::hasValidFedAuth).thenReturn(true);
            String[] args = {};
            appProps = new FileApplicationProperties(args);

            boolean actual = appProps.isToolkitCredentialsSet();

            assertThat(actual).isTrue();
        }
    }

    @Test
    void givenEmptyConfirmationWindow_whenIsConfirmation_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        boolean actual = appProps.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_confirmationWindowSetN_when_isConfirmation_then_returnFalse() {
        String[] args = {"confirmationWindow=Y"};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setConfirmationWindow(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isConfirmationWindow();

        assertThat(actual).isFalse();
    }

    @Test
    void given_confirmationWindowSetY_when_isConfirmation_then_returnTrue() {
        String[] args = {""};
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setConfirmationWindow(Boolean.TRUE);
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_emptySkipRemote_when_isSkipRemote_then_returnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        boolean actual = appProps.isSkipRemote();

        assertThat(actual).isTrue();
    }

    @Test
    void given_skipRemotePropertiesSetNAndCliSetY_when_isSkipRemote_then_returnFalse() {
        String[] args = {"skipRemote=Y"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withSkipRemote(Boolean.FALSE).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = appProps.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noSkipRemoteCliAndPropertySetY_when_isSkipRemote_then_returnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withSkipRemote(Boolean.TRUE).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = appProps.isSkipRemote();

        assertThat(actual).isTrue();
    }

    @Test
    void givenEmptyFetchAll_whenIsFetchAll_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        boolean actual = appProps.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFetchAllPropertiesSetNAndCliSetY_whenIsFetchAll_thenReturnFalse() {
        String[] args = {"fetchAll=Y"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withFetchAll(Boolean.FALSE).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = appProps.isFetchAll();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNoFetchAllCliAndPropertySetY_whenIsFetchAll_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withFetchAll(Boolean.TRUE).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = appProps.isFetchAll();

        assertThat(actual).isTrue();
    }

    @Test
    void given_emptyUseUI_when_isUseUI_then_returnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        boolean actual = appProps.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void given_useUIPropertiesSetNAndCliSetY_when_isUseUI_then_returnFalse() {
        String[] args = {"useUI=Y"};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUseUI(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isUseUI();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noUseUICliAndPropertySetY_when_isUseUI_then_returnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUseUI(Boolean.TRUE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isUseUI();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNoToolkitUserCliAndNoFileToolkitUser_whenToolkitUserFolder_then_returnDefault() {
        String[] args = {};
        appProps = new FileApplicationProperties(args);

        String actual = appProps.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + ArgName.toolkitUsername.defaultValue());
    }

    @Test
    void givenToolkitUserCliAndNoFileToolkitUser_whenToolkitUserFolder_then_returnUserFolderWithCliUser() {
        String[] args = {"toolkitUsername=xxx"};
        appProps = new FileApplicationProperties(args);

        String actual = appProps.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "XXX");
    }

    @Test
    void givenToolkitUserCliAndFileToolkitUser_whenToolkitUserFolder_then_returnFileCustomUserFolder() {
        String[] args = {"toolkitUsername=xxx"};
        appProps = new FileApplicationProperties(args);
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("aaa");
        appProps.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = appProps.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "AAA");
    }

    @Test
    void givenNoToolkitUserCliAndFileToolkitUser_whenToolkitUserFolder_then_returnFileCustomUserFolder() {
        String[] args = {};
        appProps = new FileApplicationProperties(args);
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("aaa");
        appProps.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = appProps.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "AAA");
    }

    @Test
    void givenToolkitCustomUserFolderCliAndFileToolkitUsername_whenToolkitUserFolder_then_returnUserFolderWithFileToolkitUsername() {
        String[] args = {"toolkitCustomUserFolder=qqq"};
        appProps = new FileApplicationProperties(args);
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername("aaa");
        appProps.init(TestUtils.mockConfigurationDao(toolkitConfig));

        String actual = appProps.toolkitUserFolder();

        assertThat(actual).isEqualTo(ArgName.toolkitUserFolder.defaultValue() + "AAA");
    }

    @Test
    void givenToolkitProjectListNames_whenToolkitProjectListNames_thenReturnSetWithThatToolkitProjectListNames() {
        appProps = new FileApplicationProperties(new String[]{"toolkitProjectListNames=name1"});

        Set<String> actual = appProps.toolkitProjectListNames();

        assertThat(actual).containsExactly("name1");
    }

    @Test
    void givenToolkitProjectListNamesFromPropertiesAndCLI_whenToolkitProjectListNames_thenReturnToolkitProjectListNamesFromProperties() {
        String[] args = {"toolkitProjectListNames=Proj1,Proj2"};
        appProps = new FileApplicationProperties(args);
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitProjectListNames("Proj3");
        appProps.init(TestUtils.mockConfigurationDao(toolkitConfig));

        Set<String> actual = appProps.toolkitProjectListNames();

        assertThat(actual).containsExactly("Proj3");
    }

    @Test
    void givenNoProperties_whenIsDeleteDownloadedFiles_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);

        boolean actual = appProps.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenEmptyDeleteDownloadedFiles_whenIsDeleteDownloadedFiles_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new RunConfig()));

        boolean actual = appProps.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenIsDeleteDownloadedFilesSetN_whenIsDeleteDownloadedFiles_thenReturnFalse() {
        String[] args = {"deleteDownloadedFiles=Y"};
        RunConfig runConfig = new RunConfigBuilder().withDeleteDownloadedFiles(Boolean.FALSE).create();
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = appProps.isDeleteDownloadedFiles();

        assertThat(actual).isFalse();
    }

    @Test
    void givenDeleteDownloadedFilesSetY_whenIsDeleteDownloadedFiles_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withDeleteDownloadedFiles(Boolean.TRUE).create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        boolean actual = appProps.isDeleteDownloadedFiles();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNoProperties_whenIsEnableOnStartup_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isTrue();
    }

    @Test
    void givenEmptyEnableOnStartupFiles_whenIsEnableOnStartup_thenReturnTrue() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isTrue();
    }

    @Test
    void givenEnableOnStartupSetN_whenIsEnableOnStartup_thenReturnFalse() {
        String[] args = {"enableOnStartup=Y"};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupSetY_whenIsEnableOnStartup_thenReturnFalse() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenConfigurationNameFromCommandLine_whenConfigurationName_thenReturnThatConfigurationName() {
        appProps = new FileApplicationProperties(new String[]{"configurationName=testAuthor"});

        String actual = appProps.configurationName();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void givenConfigurationNameFromPropertiesAndCommandLine_whenConfigurationName_thenReturnConfigurationNameFromProperties() {
        String[] args = {"configurationName=testAuthor"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfigBuilder().withConfigurationName("propsAuthor").create();
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        String actual = appProps.configurationName();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void givenNoProperties_whenIsUpgradeFinished_thenReturnFalse() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);

        boolean actual = appProps.isUpgradeFinished();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEmptyUpgradeFinishedFiles_whenIsUpgradeFinished_thenReturnFalse() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        boolean actual = appProps.isUpgradeFinished();

        assertThat(actual).isFalse();
    }

    @Test
    void givenUpgradeFinishedSetY_whenIsUpgradeFinished_thenReturnTrue() {
        String[] args = {"upgradeFinished=N"};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUpgradeFinished(Boolean.TRUE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isUpgradeFinished();

        assertThat(actual).isTrue();
    }

    @Test
    void givenUpgradeFinishedN_whenIsUpgradeFinished_thenReturnFalse() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUpgradeFinished(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isUpgradeFinished();

        assertThat(actual).isFalse();
    }

    @Test
    void givenFewDates_whenGetCurrentWeekNumber_thenReturnProperNumber() {
        Locale.setDefault(Locale.UK);
        String[] args = {""};
        appProps = new FileApplicationProperties(args);

        assertThat(appProps.getWeekNumber(LocalDate.of(2019, 6, 1))).isEqualTo(22);
        assertThat(appProps.getWeekNumber(LocalDate.of(2019, 10, 26))).isEqualTo(43);//Saturday
        assertThat(appProps.getWeekNumber(LocalDate.of(2019, 10, 27))).isEqualTo(43);//Sunday
        assertThat(appProps.getWeekNumber(LocalDate.of(2019, 10, 28))).isEqualTo(44);//Monday

    }

    @Test
    void givenNoFetchTimeoutFromCommandLine_whenFetchTimeout_thenReturnDefaultValue60() {
        appProps = new FileApplicationProperties(new String[]{});

        int actual = appProps.fetchTimeout();

        assertThat(actual).isEqualTo(60);
    }

    @Test
    void givenFetchTimeoutCommandLine_whenFetchTimeout_thenReturnThatFetchTimeout() {
        appProps = new FileApplicationProperties(new String[]{"fetchTimeout=1"});

        int actual = appProps.fetchTimeout();

        assertThat(actual).isEqualTo(1);
    }

    @Test
    void givenFetchTimeoutFromPropertiesAndCommandLine_whenFetchTimeout_then_returnFetchTimeoutFromProperties() {
        String[] args = {"fetchTimeout=1"};
        appProps = new FileApplicationProperties(args);
        RunConfig runConfig = new RunConfig();
        runConfig.setFetchTimeout(2);
        appProps.init(TestUtils.mockConfigurationDao(runConfig));

        int actual = appProps.fetchTimeout();

        assertThat(actual).isEqualTo(2);
    }

    @Test
    void givenEmptyGithubToken_whenGithubToken_thenThrowNPE() {
        String[] args = {""};
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        try {
            appProps.githubToken();
            fail("Should throw NPE");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void given_GithubTokenFromCliAndFile_when_githubToken_then_returnItFromFile() {
        String[] args = {"githubToken=cli"};
        appProps = new FileApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setGithubToken("file");
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        String actual = appProps.githubToken();

        assertThat(actual).isEqualTo("file");
    }

    @Test
    void given_GithubTokenFromFile_when_githubToken_then_returnIt() {
        String[] args = {""};
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setGithubToken("file");
        appProps = new FileApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        String actual = appProps.githubToken();

        assertThat(actual).isEqualTo("file");
    }
}