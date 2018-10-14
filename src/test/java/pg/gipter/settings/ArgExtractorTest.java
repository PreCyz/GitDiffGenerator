package pg.gipter.settings;

import org.junit.jupiter.api.Test;
import pg.gipter.producer.command.VersionControlSystem;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

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

    @Test
    void given_noMinusDays_when_days_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        int actual = argExtractor.days();

        assertThat(actual).isEqualTo(7);
    }

    @Test
    void given_minusDays_when_days_then_returnThatMinusDays() {
        argExtractor = new ArgExtractor(new String[]{"minusDays=16"});

        int actual = argExtractor.days();

        assertThat(actual).isEqualTo(16);
    }

    @Test
    void given_noCommitterEmail_when_committerEmail_then_returnEmptyString() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.committerEmail();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_committerEmail_when_committerEmail_then_returnThatCommitterEmail() {
        argExtractor = new ArgExtractor(new String[]{"committerEmail=testCommitterEmail"});

        String actual = argExtractor.committerEmail();

        assertThat(actual).isEqualTo("testCommitterEmail");
    }

    @Test
    void given_noStartDate_when_startDate_then_returnNowMinus7Days() {
        argExtractor = new ArgExtractor(new String[]{});

        LocalDate actual = argExtractor.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(7));
    }

    @Test
    void given_startDate_when_startDate_then_returnThatStartDate() {
        argExtractor = new ArgExtractor(new String[]{"startDate=2018-10-14"});

        LocalDate actual = argExtractor.startDate();

        assertThat(actual).isEqualTo(LocalDate.of(2018, 10, 14));
    }

    @Test
    void given_startDateInWrongFormat_when_startDate_then_throwException() {
        argExtractor = new ArgExtractor(new String[]{"startDate=2018/10/14"});
        try {
            argExtractor.startDate();
            fail("Should be NumberFormatException: For input string: 2018/10/14");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(NumberFormatException.class);
        }
    }

    @Test
    void given_noEndDate_when_endDate_then_returnNow() {
        argExtractor = new ArgExtractor(new String[]{});

        LocalDate actual = argExtractor.startDate();

        assertThat(actual).isEqualTo(LocalDate.now().minusDays(7));
    }

    @Test
    void given_endDate_when_endDate_then_returnThatEndDate() {
        argExtractor = new ArgExtractor(new String[]{"endDate=2018-10-14"});

        LocalDate actual = argExtractor.endDate();

        assertThat(actual).isEqualTo(LocalDate.of(2018, 10, 14));
    }

    @Test
    void given_endDateInWrongFormat_when_endDate_then_throwException() {
        argExtractor = new ArgExtractor(new String[]{"endDate=2018/10/14"});
        try {
            argExtractor.endDate();
            fail("Should be NumberFormatException: For input string: 2018/10/14");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(NumberFormatException.class);
        }
    }

    @Test
    void given_noItemFileName_when_itemFileName_then_returnEmptyString() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.itemFileName();

        assertThat(actual).isEmpty();
    }

    @Test
    void given_itemFileName_when_itemFileName_then_returnThatItemFileName() {
        argExtractor = new ArgExtractor(new String[]{"itemFileName=testItemFileName"});

        String actual = argExtractor.itemFileName();

        assertThat(actual).isEqualTo("testItemFileName");
    }

    @Test
    void given_noVersionControlSystem_when_versionControlSystem_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        VersionControlSystem actual = argExtractor.versionControlSystem();

        assertThat(actual).isEqualTo(VersionControlSystem.GIT);
    }

    @Test
    void given_gitVersionControlSystem_when_versionControlSystem_then_returnGitVersionControlSystem() {
        argExtractor = new ArgExtractor(new String[]{"versionControlSystem=git"});

        VersionControlSystem actual = argExtractor.versionControlSystem();

        assertThat(actual).isEqualTo(VersionControlSystem.GIT);
    }

    @Test
    void given_svnVersionControlSystem_when_versionControlSystem_then_returnSvnVersionControlSystem() {
        argExtractor = new ArgExtractor(new String[]{"versionControlSystem=svn"});

        VersionControlSystem actual = argExtractor.versionControlSystem();

        assertThat(actual).isEqualTo(VersionControlSystem.SVN);
    }

    @Test
    void given_mercurialVersionControlSystem_when_versionControlSystem_then_returnMercurialVersionControlSystem() {
        argExtractor = new ArgExtractor(new String[]{"versionControlSystem=mercurial"});

        VersionControlSystem actual = argExtractor.versionControlSystem();

        assertThat(actual).isEqualTo(VersionControlSystem.MERCURIAL);
    }

    @Test
    void given_notSupportedVersionControlSystem_when_versionControlSystem_then_throwException() {
        argExtractor = new ArgExtractor(new String[]{"versionControlSystem=cvs"});

        try {
            argExtractor.versionControlSystem();
            fail("Should throw exception.");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        }
    }
}