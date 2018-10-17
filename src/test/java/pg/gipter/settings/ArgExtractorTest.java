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

    @Test
    void given_noCodeProtected_when_codeProtected_then_returnFalse() {
        argExtractor = new ArgExtractor(new String[]{});

        boolean actual = argExtractor.codeProtected();

        assertThat(actual).isFalse();
    }

    @Test
    void given_codeProtectedSetAsNo_when_codeProtected_then_returnFalse() {
        argExtractor = new ArgExtractor(new String[]{"codeProtected=no"});

        boolean actual = argExtractor.codeProtected();

        assertThat(actual).isFalse();
    }

    @Test
    void given_codeProtectedSetAsYes_when_codeProtected_then_returnTrue() {
        argExtractor = new ArgExtractor(new String[]{"codeProtected=yes"});

        boolean actual = argExtractor.codeProtected();

        assertThat(actual).isTrue();
    }

    @Test
    void given_allPossibleValuesForCodeProtected_when_codeProtected_then_returnAlwaysTrue() {
        argExtractor = new ArgExtractor(new String[]{"codeProtected=yes"});
        boolean actual = argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=y"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=Yes"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=YES"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=t"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=true"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=T"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=TRUE"});
        actual &= argExtractor.codeProtected();
        argExtractor = new ArgExtractor(new String[]{"codeProtected=True"});
        actual &= argExtractor.codeProtected();

        assertThat(actual).isTrue();
    }

    @Test
    void given_noToolkitUsername_when_toolkitUsername_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.toolkitUsername();

        assertThat(actual).isEqualTo("NO_TOOLKIT_USERNAME_GIVEN");
    }

    @Test
    void given_toolkitUsername_when_toolkitUsername_then_returnThatUsernameUpperCased() {
        argExtractor = new ArgExtractor(new String[]{"toolkitUsername=username"});

        String actual = argExtractor.toolkitUsername();

        assertThat(actual).isEqualTo("USERNAME");
    }

    @Test
    void given_noToolkitPassword_when_toolkitPassword_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.toolkitPassword();

        assertThat(actual).isEqualTo("NO_TOOLKIT_PASSWORD_GIVEN");
    }

    @Test
    void given_toolkitPassword_when_toolkitPassword_then_returnThatPassword() {
        argExtractor = new ArgExtractor(new String[]{"toolkitPassword=password"});

        String actual = argExtractor.toolkitPassword();

        assertThat(actual).isEqualTo("password");
    }

    @Test
    void when_toolkitDomain_then_returnNCDMZ() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.toolkitDomain();

        assertThat(actual).isEqualTo("NCDMZ");
    }

    @Test
    void given_toolkitDomainFromCommandLine_when_toolkitDomain_then_returnNCDMZ() {
        argExtractor = new ArgExtractor(new String[]{"toolkitDomain=sthElse"});

        String actual = argExtractor.toolkitDomain();

        assertThat(actual).isEqualTo("NCDMZ");
    }

    @Test
    void when_toolkitUrl_then_returnDefaultUrl() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY");
    }

    @Test
    void given_toolkitUrlFromCommandLine_when_toolkitUrl_then_returnDefaultUrl() {
        argExtractor = new ArgExtractor(new String[]{"toolkitUrl=sthElse"});

        String actual = argExtractor.toolkitUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY");
    }

    @Test
    void given_toolkitWSUrlFromCommandLine_when_toolkitWSUrl_then_returnDefaultUrl() {
        argExtractor = new ArgExtractor(new String[]{"toolkitWSUrl=sthElse"});

        String actual = argExtractor.toolkitWSUrl();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx");
    }

    @Test
    void given_toolkitUserFolderFromCommandLine_when_toolkitUserFolder_then_returnDefaultValue() {
        argExtractor = new ArgExtractor(new String[]{"toolkitUserFolder=sthElse", "toolkitUsername=XXX"});

        String actual = argExtractor.toolkitUserFolder();

        assertThat(actual).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/XXX");
    }

    @Test
    void when_toolkitListName_then_returnDefaultListName() {
        argExtractor = new ArgExtractor(new String[]{});

        String actual = argExtractor.toolkitListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitListNameFromCommandLine_when_toolkitListName_then_returnDefaultListName() {
        argExtractor = new ArgExtractor(new String[]{"toolkitListName=sthElse"});

        String actual = argExtractor.toolkitListName();

        assertThat(actual).isEqualTo("WorkItems");
    }

    @Test
    void given_toolkitUserNameFromCommandLine_when_toolkitUserEmail_then_returnEmail() {
        argExtractor = new ArgExtractor(new String[]{"toolkitUsername=XXX"});

        String actual = argExtractor.toolkitUserEmail();

        assertThat(actual).isEqualTo("XXX@netcompany.com");
    }
}