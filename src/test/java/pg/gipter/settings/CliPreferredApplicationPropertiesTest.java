package pg.gipter.settings;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CliPreferredApplicationPropertiesTest {

    private ApplicationProperties applicationProperties;

    private PropertiesLoader mockPropertiesLoader(Properties properties) {
        PropertiesLoader loader = mock(PropertiesLoader.class);
        when(loader.loadPropertiesFromFile()).thenReturn(Optional.of(properties));
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
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"author=cliAuthor"});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("cliAuthor"));
    }

    @Test
    void given_noCliAuthorAndFileAuthor_when_author_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"author=fileAuthor"}, loader);

        Set<String> authors = applicationProperties.authors();

        assertThat(authors).hasSameElementsAs(Collections.singletonList("fileAuthor"));
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_author_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("author", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
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
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"gitAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliGitAuthorAndFileAuthor_when_gitAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"gitAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.gitAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_gitAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("gitAuthor", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
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
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"mercurialAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliMercurialAuthorAndFileAuthor_when_mercurialAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"mercurialAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.mercurialAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_mercurialAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("mercurialAuthor", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
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
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{"svnAuthor=cliAuthor"});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("cliAuthor");
    }

    @Test
    void given_noCliSvnAuthorAndFileAuthor_when_svnAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
        applicationProperties = new CliPreferredApplicationProperties(new String[]{});
        applicationProperties.init(new String[]{"svnAuthor=fileAuthor"}, loader);

        String actual = applicationProperties.svnAuthor();

        assertThat(actual).isEqualTo("fileAuthor");
    }

    @Test
    void given_fileAuthorAndOtherArgs_when_svnAuthor_then_returnFileAuthor() {
        Properties properties = new Properties();
        properties.setProperty("svnAuthor", "fileAuthor");
        PropertiesLoader loader = mockPropertiesLoader(properties);
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

        assertThat(actual).startsWith("propertiesItemPath" + File.separator);
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
}