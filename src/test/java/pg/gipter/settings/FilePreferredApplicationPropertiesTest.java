package pg.gipter.settings;

import org.junit.jupiter.api.Test;
import pg.gipter.producer.command.CodeProtection;
import pg.gipter.util.PropertiesHelper;

import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pg.gipter.settings.FilePreferredApplicationProperties.yyyy_MM_dd;

class FilePreferredApplicationPropertiesTest {

    private ApplicationProperties appProps;

    private PropertiesHelper mockPropertiesLoader(Properties properties) {
        PropertiesHelper loader = mock(PropertiesHelper.class);
        when(loader.loadPropertiesFromFile()).thenReturn(Optional.of(properties));
        return loader;
    }

    @Test
    void given_propertiesFromFile_when_hasProperties_then_returnTrue() {
        PropertiesHelper loader = mockPropertiesLoader(new Properties());
        appProps = new FilePreferredApplicationProperties(new String[]{});
        appProps.init(new String[]{}, loader);

        assertThat(appProps.hasProperties()).isTrue();
    }

    @Test
    void given_noPropertiesFromFile_when_hasProperties_then_returnFalse() {
        appProps = new FilePreferredApplicationProperties(new String[]{});

        assertThat(appProps.hasProperties()).isFalse();
    }

    @Test
    void given_authorFromCommandLine_when_authors_then_returnThatAuthor() {
        appProps = new FilePreferredApplicationProperties(new String[]{"author=author1"});

        Set<String> actual = appProps.authors();

        assertThat(actual).hasSize(1);
        assertThat(actual).containsExactly("author1");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_authors_then_returnAuthorsFromProperties() {
        String[] args = {"author=testAuthor"};
        Properties props = new Properties();
        props.put("author", "propsAuthor1,propsAuthor2");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        Set<String> actual = appProps.authors();

        assertThat(actual).hasSize(2);
        assertThat(actual).containsExactly("propsAuthor1", "propsAuthor2");
    }

    @Test
    void given_authorFromCommandLine_when_gitAuthor_then_returnThatAuthor() {
        appProps = new FilePreferredApplicationProperties(new String[]{"gitAuthor=testAuthor"});

        String actual = appProps.gitAuthor();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_gitAuthor_then_returnAuthorFromProperties() {
        String[] args = {"gitAuthor=testAuthor"};
        Properties props = new Properties();
        props.put("gitAuthor", "propsAuthor");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.gitAuthor();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void given_authorFromCommandLine_when_mercurialAuthor_then_returnThatAuthor() {
        appProps = new FilePreferredApplicationProperties(new String[]{"mercurialAuthor=testAuthor"});

        String actual = appProps.mercurialAuthor();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_mercurialAuthor_then_returnAuthorFromProperties() {
        String[] args = {"mercurialAuthor=testAuthor"};
        Properties props = new Properties();
        props.put("mercurialAuthor", "propsAuthor");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.mercurialAuthor();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void given_authorFromCommandLine_when_svnAuthor_then_returnThatAuthor() {
        appProps = new FilePreferredApplicationProperties(new String[]{"svnAuthor=testAuthor"});

        String actual = appProps.svnAuthor();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_authorFromPropertiesAndCommandLine_when_svnAuthor_then_returnAuthorFromProperties() {
        String[] args = {"svnAuthor=testAuthor"};
        Properties props = new Properties();
        props.put("svnAuthor", "propsAuthor");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.svnAuthor();

        assertThat(actual).isEqualTo("propsAuthor");
    }

    @Test
    void given_itemPathFromCommandLine_when_itemPath_then_returnThatItemPath() {
        appProps = new FilePreferredApplicationProperties(new String[]{"itemPath=testItemPath"});

        String actual = appProps.itemPath();

        assertThat(actual).startsWith("testItemPath" + File.separator);
    }

    @Test
    void given_itemPathFromPropertiesAndCommandLine_when_itemPath_then_returnItemPathFromProperties() {
        String[] args = {"itemPath=testItemPath"};
        Properties props = new Properties();
        props.put("itemPath", "propertiesItemPath");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.itemPath();

        assertThat(actual).startsWith("propertiesItemPath" + File.separator);
    }

    @Test
    void given_defaultParams_when_fileName_then_returnThatFileName() {
        appProps = new FilePreferredApplicationProperties(new String[]{});
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo(fileName);
    }

    @Test
    void given_statement_when_fileName_then_returnFileNameForStatement() {
        appProps = new FilePreferredApplicationProperties(new String[]{
                "codeProtection=statement"
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
        appProps = new FilePreferredApplicationProperties(new String[]{
                "startDate=2018-09-19",
                "endDate=2018-10-19"
        });
        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("2018-october-20180919-20181019.txt");
    }

    @Test
    void given_propertiesFileAndEndDateFromDeepPast_when_fileName_then_returnWellBuildFileName() {
        String[] args = {};
        Properties props = new Properties();
        props.put("codeProtection", "statement");
        props.put("startDate", "2017-10-19");
        props.put("endDate", "2017-12-20");
        props.put("itemFileNamePrefix", "custom");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("custom-2017-december-20171019-20171220.txt");
    }

    @Test
    void given_commandLinePropsAndEndDateFromDeepPast_when_fileName_then_returnWellBuildFileName() {
        String[] args = {
                "codeProtection=STATEMENT",
                "startDate=2017-10-19",
                "endDate=2017-12-20",
                "itemFileNamePrefix=custom",
                "toolkitUsername=xxx",
        };
        appProps = new FilePreferredApplicationProperties(args);

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("custom-2017-december-20171019-20171220.txt");
    }

    @Test
    void given_itemFileNameFromCommandLine_when_fileName_then_returnThatFileName() {
        appProps = new FilePreferredApplicationProperties(new String[]{"itemFileNamePrefix=fileName"});

        String actual = appProps.fileName();

        assertThat(actual).startsWith("fileName");
    }

    @Test
    void given_itemFileNameAndStartDateAndEndDateFromCommandLine_when_fileName_then_returnBuildFileName() {
        appProps = new FilePreferredApplicationProperties(new String[]{"itemFileNamePrefix=fileName", "startDate=2018-10-07", "endDate=2018-10-14"});

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("fileName-2018-october-20181007-20181014.txt");
    }

    @Test
    void given_periodInDaysAndStartDate_when_startDate_then_returnStartDate() {
        appProps = new FilePreferredApplicationProperties(new String[]{"periodInDays=16","startDate=2018-10-18"});

        LocalDate actual = appProps.startDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-18");
    }

    @Test
    void given_onlyPeriodInDays_when_startDate_then_returnNowMinusPeriodInDays() {
        appProps = new FilePreferredApplicationProperties(new String[]{"periodInDays=16"});

        LocalDate actual = appProps.startDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo(LocalDate.now().minusDays(16).format(yyyy_MM_dd));
    }

    @Test
    void given_startDateFromPropertiesAndCommandLine_when_startDate_then_returnStartDateFromProperties() {
        String[] args = {"startDate=2018-10-18"};
        Properties props = new Properties();
        props.put("startDate", "2018-10-19");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        LocalDate actual = appProps.startDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-19");
    }

    @Test
    void given_itemFileNamePrefix_when_itemFileNamePrefix_then_returnThatItemFileNamePrefix() {
        appProps = new FilePreferredApplicationProperties(new String[]{"itemFileNamePrefix=testItemFileNamePrefix"});

        String actual = appProps.itemFileNamePrefix();

        assertThat(actual).isEqualTo("testItemFileNamePrefix");
    }

    @Test
    void given_itemFileNamePrefixFromPropertiesAndCommandLine_when_startDate_then_returnItemFileNamePrefixFromProperties() {
        String[] args = {"itemFileNamePrefix=testItemFileNamePrefix"};
        Properties props = new Properties();
        props.put("itemFileNamePrefix","propsItemFileNamePrefix");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.itemFileNamePrefix();

        assertThat(actual).isEqualTo("propsItemFileNamePrefix");
    }

    @Test
    void given_toolkitUsername_when_toolkitUsername_then_returnNowMinusPeriodInDays() {
        appProps = new FilePreferredApplicationProperties(new String[]{"toolkitUsername=userName"});

        String actual = appProps.toolkitUsername();

        assertThat(actual).isEqualTo("USERNAME");
    }

    @Test
    void given_endDate_when_endDate_then_returnThatEndDate() {
        appProps = new FilePreferredApplicationProperties(new String[]{"endDate=2018-10-19"});

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-19");
    }

    @Test
    void given_noEndDateInAppPropertiesAndEndDateFromCliArgs_when_endDate_then_returnEndDateFromCliArgs() {
        String[] args = {"startDate=2018-09-19", "endDate=2018-10-15"};
        Properties props = new Properties();
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-15");
    }

    @Test
    void given_endDateInAppPropertiesAndInCommandLineArgs_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {"startDate=2018-09-19", "endDate=2018-10-15"};
        Properties props = new Properties();
        props.put("endDate", "2018-10-19");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-19");
    }

    @Test
    void given_emptyEndDateInAppPropertiesAndInCommandLineArgs_when_endDate_then_returnEndDateFromProperties() {
        String[] args = {"startDate=2018-09-19", "endDate=2018-10-15"};
        Properties props = new Properties();
        props.put("endDate", "");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo("2018-10-15");
    }

    @Test
    void given_noEndDate_when_endDate_then_returnNow() {
        appProps = new FilePreferredApplicationProperties(new String[]{});

        LocalDate actual = appProps.endDate();

        assertThat(actual.format(yyyy_MM_dd)).isEqualTo(LocalDate.now().format(yyyy_MM_dd));
    }

    @Test
    void given_wrongEndDate_when_endDate_then_throwDateTimeException() {
        try {
            appProps = new FilePreferredApplicationProperties(new String[]{"endDate=2018-02-30"});

            appProps.endDate();
            fail("Should throw DateTimeException.");
        } catch (DateTimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid date 'FEBRUARY 30'");
        }
    }

    @Test
    void given_wrongStartDate_when_startDate_then_throwDateTimeException() {
        try {
            appProps = new FilePreferredApplicationProperties(new String[]{"endDate=2018-02-30"});

            appProps.endDate();
            fail("Should throw DateTimeException.");
        } catch (DateTimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid date 'FEBRUARY 30'");
        }
    }

    @Test
    void given_projectPath_when_projectPaths_then_returnSetWithThatProjectPath() {
        appProps = new FilePreferredApplicationProperties(new String[]{"projectPath=Proj1"});

        Set<String> actual = appProps.projectPaths();

        assertThat(actual).containsExactly("Proj1");
    }

    @Test
    void given_projectPathFromPropertiesAndCommandLine_when_projectPaths_then_returnSetWithProjectPathFromProperties() {
        String[] args = {"projectPath=Proj1,Proj2"};
        Properties props = new Properties();
        props.put("projectPath", "Proj3");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        Set<String> actual = appProps.projectPaths();

        assertThat(actual).containsExactly("Proj3");
    }

    @Test
    void given_noPeriodInDaysFromCommandLine_when_periodInDays_then_returnDefaultValue7() {
        appProps = new FilePreferredApplicationProperties(new String[]{});

        int actual = appProps.periodInDays();

        assertThat(actual).isEqualTo(7);
    }

    @Test
    void given_periodInDaysFromCommandLine_when_periodInDays_then_returnThatPeriodInDays() {
        appProps = new FilePreferredApplicationProperties(new String[]{"periodInDays=1"});

        int actual = appProps.periodInDays();

        assertThat(actual).isEqualTo(1);
    }

    @Test
    void given_periodInDaysFromPropertiesAndCommandLine_when_periodInDays_then_returnPeriodInDaysFromProperties() {
        String[] args = {"periodInDays=1"};
        Properties props = new Properties();
        props.put("periodInDays", "2");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        int actual = appProps.periodInDays();

        assertThat(actual).isEqualTo(2);
    }

    @Test
    void given_committerEmailFromCommandLine_when_committerEmail_then_returnThatCommitterEmail() {
        appProps = new FilePreferredApplicationProperties(new String[]{"committerEmail=testCommitterEmail"});

        String actual = appProps.committerEmail();

        assertThat(actual).isEqualTo("testCommitterEmail");
    }

    @Test
    void given_committerEmailFromPropertiesAndCommandLine_when_committerEmail_then_returnCommitterEmailFromProperties() {
        String[] args = {"committerEmail=testCommitterEmail"};
        Properties props = new Properties();
        props.put("committerEmail", "propsCommitterEmail");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        String actual = appProps.committerEmail();

        assertThat(actual).isEqualTo("propsCommitterEmail");
    }

    @Test
    void given_noCodeProtection_when_codeProtection_then_returnDefaultValueNONE() {
        appProps = new FilePreferredApplicationProperties(new String[]{});

        CodeProtection actual = appProps.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.NONE);
    }

    @Test
    void given_codeProtection_when_codeProtection_then_returnThatCodeProtection() {
        appProps = new FilePreferredApplicationProperties(new String[]{"codeProtection=simple"});

        CodeProtection actual = appProps.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.SIMPLE);
    }

    @Test
    void given_codeProtectionFromPropertiesAndCommandLine_when_codeProtection_then_returnCodeProtectionFromProperties() {
        String[] args = {"codeProtection=Simple"};
        Properties props = new Properties();
        props.put("codeProtection", "statement");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        CodeProtection actual = appProps.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.STATEMENT);
    }

    @Test
    void given_noToolkitUsernameAndPassword_when_isToolkitPropertiesSet_then_returnFalse() {
        String[] args = new String[]{};
        appProps = new FilePreferredApplicationProperties(args);

        boolean actual = appProps.isToolkitCredentialsSet();

        assertThat(actual).isFalse();
    }

    @Test
    void given_emptyToolkitUsernameAndPassword_when_isToolkitPropertiesSet_then_returnFalse() {
        String[] args = {"toolkitUsername=", "toolkitPassword="};
        appProps = new FilePreferredApplicationProperties(args);

        boolean actual = appProps.isToolkitCredentialsSet();

        assertThat(actual).isFalse();
    }

    @Test
    void given_toolkitUsernameAndPassword_when_isToolkitPropertiesSet_then_returnTrue() {
        String[] args = {"toolkitPassword=yui"};
        Properties props = new Properties();
        props.put("toolkitUsername", "cvb");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isToolkitCredentialsSet();

        assertThat(actual).isTrue();
    }

    @Test
    void given_emptyConfirmationWindow_when_isConfirmation_then_returnFalse() {
        String[] args = {""};
        Properties props = new Properties();
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isConfirmationWindow();

        assertThat(actual).isFalse();
    }

    @Test
    void given_confirmationWindowSetN_when_isConfirmation_then_returnFalse() {
        String[] args = {"confirmationWindow=Y"};
        Properties props = new Properties();
        props.put("confirmationWindow", "N");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isConfirmationWindow();

        assertThat(actual).isFalse();
    }

    @Test
    void given_confirmationWindowSetY_when_isConfirmation_then_returnTrue() {
        String[] args = {""};
        Properties props = new Properties();
        props.put("confirmationWindow", "Y");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isConfirmationWindow();

        assertThat(actual).isTrue();
    }

    @Test
    void given_emptySkipRemote_when_isSkipRemote_then_returnTrue() {
        String[] args = {""};
        Properties props = new Properties();
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isSkipRemote();

        assertThat(actual).isTrue();
    }

    @Test
    void given_skipRemotePropertiesSetNAndCliSetY_when_isSkipRemote_then_returnFalse() {
        String[] args = {"skipRemote=Y"};
        Properties props = new Properties();
        props.put("skipRemote", "N");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isSkipRemote();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noSkipRemoteCliAndPropertySetY_when_isSkipRemote_then_returnTrue() {
        String[] args = {""};
        Properties props = new Properties();
        props.put("skipRemote", "Y");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isSkipRemote();

        assertThat(actual).isTrue();
    }

    @Test
    void given_emptyUseUI_when_isUseUI_then_returnFalse() {
        String[] args = {""};
        Properties props = new Properties();
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isUseUI();

        assertThat(actual).isFalse();
    }

    @Test
    void given_useUIPropertiesSetNAndCliSetY_when_isUseUI_then_returnFalse() {
        String[] args = {"useUI=Y"};
        Properties props = new Properties();
        props.put("useUI", "N");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isUseUI();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noUseUICliAndPropertySetY_when_isUseUI_then_returnTrue() {
        String[] args = {""};
        Properties props = new Properties();
        props.put("useUI", "Y");
        appProps = new FilePreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isUseUI();

        assertThat(actual).isTrue();
    }
}