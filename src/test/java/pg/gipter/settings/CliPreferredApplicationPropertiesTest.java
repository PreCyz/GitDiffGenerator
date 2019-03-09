package pg.gipter.settings;

import org.junit.jupiter.api.Test;
import pg.gipter.producer.command.CodeProtection;
import pg.gipter.util.PropertiesHelper;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CliPreferredApplicationPropertiesTest {

    private ApplicationProperties applicationProperties;

    private PropertiesHelper mockPropertiesLoader(Properties properties) {
        PropertiesHelper loader = mock(PropertiesHelper.class);
        when(loader.loadApplicationProperties()).thenReturn(Optional.of(properties));
        return loader;
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
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=cliAuthor"});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("cliAuthor"));
    }

    @Test
    void given_noCliAuthorAndFileAuthor_when_author_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("fileAuthor"));
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_author_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
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
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"gitAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliGitAuthorAndFileAuthor_when_gitAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_gitAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
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
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"mercurialAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliMercurialAuthorAndFileAuthor_when_mercurialAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_mercurialAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
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
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"svnAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliSvnAuthorAndFileAuthor_when_svnAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_svnAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesHelper loader = mockPropertiesLoader(properties);
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
        String[] args = {"codeProtection=statement"};
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
        String[] args = {"codeProtection=statement"};
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
        String[] args = {"codeProtection=statement"};
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
        String[] args = {"codeProtection=statement"};
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
        String[] args = {"codeProtection=statement"};
        Properties props = new Properties();
        props.put("startDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.startDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
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
        String[] args = {"codeProtection=statement"};
        Properties props = new Properties();
        props.put("endDate", "2019-02-09");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        LocalDate actual = applicationProperties.endDate();

        assertThat(actual.format(ApplicationProperties.yyyy_MM_dd)).isEqualTo("2019-02-09");
    }

    @Test
    void given_noCodeProtection_when_codeProtection_then_returnCodeProtection() {
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});

        CodeProtection actual = applicationProperties.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.NONE);
    }

    @Test
    void given_codeProtectionFromCLI_when_codeProtection_then_returnCliCodeProtection() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"codeProtection=simple"}
        );

        CodeProtection actual = applicationProperties.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.SIMPLE);
    }

    @Test
    void given_codeProtectionFileAndCLI_when_codeProtection_then_returnCliCodeProtection() {
        String[] args = {"codeProtection=SIMPLE"};
        Properties props = new Properties();
        props.put("codeProtection", "statement");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        CodeProtection actual = applicationProperties.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.SIMPLE);
    }

    @Test
    void given_codeProtectionFromProperties_when_codeProtection_then_returnCodeProtectionFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("codeProtection", "STATEMENT");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        CodeProtection actual = applicationProperties.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.STATEMENT);
    }

    @Test
    void given_codeProtectionFromPropertiesAndOtherArgs_when_codeProtection_then_returnCodeProtectionFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("codeProtection", "statement");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        CodeProtection actual = applicationProperties.codeProtection();

        assertThat(actual).isEqualTo(CodeProtection.STATEMENT);
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
        String[] args = {"codeProtection=statement"};
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
        String[] args = {"codeProtection=statement"};
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
        String[] args = {"codeProtection=statement"};
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

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY");
    }

    @Test
    void given_toolkitUrlFromCLI_when_toolkitUrl_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitUrl=cliUrl"}
        );

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY");
    }

    @Test
    void given_toolkitUrlFileAndCLI_when_toolkitUrl_then_returnDefault() {
        String[] args = {"toolkitUrl=cliUrl"};
        Properties props = new Properties();
        props.put("toolkitUrl", "propertiesUrl");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY");
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
        String[] args = {"codeProtection=statement"};
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

        String actual = applicationProperties.toolkitListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromCLI_when_toolkitListName_then_returnDefault() {
        applicationProperties = new CliPreferredApplicationProperties(
                new String[]{"toolkitListName=cliListName"}
        );

        String actual = applicationProperties.toolkitListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFileAndCLI_when_toolkitListName_then_returnDefault() {
        String[] args = {"toolkitListName=cliListName"};
        Properties props = new Properties();
        props.put("toolkitListName", "propertiesListName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromProperties_when_toolkitListName_then_returnToolkitListNameFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("toolkitListName", "propertiesListName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitListName();

        assertThat(actual).isEqualTo("propertiesListName");
    }

    @Test
    void given_toolkitListNameFromPropertiesAndOtherArgs_when_toolkitListName_then_returnToolkitListNameFromProperties() {
        String[] args = {"codeProtection=statement"};
        Properties props = new Properties();
        props.put("toolkitListName", "propertiesListName");
        applicationProperties = new CliPreferredApplicationProperties(args);
        applicationProperties.init(args, mockPropertiesLoader(props));

        String actual = applicationProperties.toolkitListName();

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
        String[] args = {"codeProtection=statement"};
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

        assertThat(actual).isFalse();
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
}