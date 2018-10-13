package pg.gipter.settings;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ArgExtractorTest {

    private ArgExtractor argExtractor;

    @Test
    void given_noAuthor_when_author_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.author();

        assertThat(actual).isEqualTo("NO_AUTHOR_GIVEN");
    }

    @Test
    void given_author_when_author_then_returnThatAuthor() {
        argExtractor = new ArgExtractor(new String[]{"author=testAuthor"});

        String actual = argExtractor.author();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_noItemPath_when_itemPath_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.itemPath();

        assertThat(actual).isEqualTo("NO_ITEM_PATH_GIVEN");
    }

    @Test
    void given_itemPath_when_itemPath_then_returnThatItemPath() {
        argExtractor = new ArgExtractor(new String[]{"itemPath=testItemPath"});

        String actual = argExtractor.itemPath();

        assertThat(actual).isEqualTo("testItemPath");
    }

    @Test
    void given_noProjectPaths_when_projectPaths_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        Set<String> actual = argExtractor.projectPaths();

        assertThat(actual).containsExactly("NO_PROJECT_PATH_GIVEN");
    }

    @Test
    void given_projectPaths_when_projectPaths_then_returnThatProjectPath() {
        argExtractor = new ArgExtractor(new String[]{"projectPaths=testProjectPath1"});

        Set<String> actual = argExtractor.projectPaths();

        assertThat(actual).containsExactly("testProjectPath1");
    }

    @Test
    void given_moreThenOneProjectPath_when_projectPaths_then_returnAllProjectPath() {
        argExtractor = new ArgExtractor(new String[]{"projectPaths=testProjectPath1,testProjectPath2,testProjectPath3"});

        Set<String> actual = argExtractor.projectPaths();

        assertThat(actual).containsExactly("testProjectPath1", "testProjectPath2", "testProjectPath3");
    }

    @Test
    void given_theSameProjectPaths_when_projectPaths_then_returnOnlyOneProjectPath() {
        argExtractor = new ArgExtractor(new String[]{"projectPaths=testProjectPath1,testProjectPath1,testProjectPath1"});

        Set<String> actual = argExtractor.projectPaths();

        assertThat(actual).containsExactly("testProjectPath1");
    }
}