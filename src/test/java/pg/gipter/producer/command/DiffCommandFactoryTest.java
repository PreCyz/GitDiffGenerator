package pg.gipter.producer.command;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.FilePreferredApplicationProperties;

import static org.assertj.core.api.Assertions.assertThat;

class DiffCommandFactoryTest {

    @Test
    void given_codeProtectionSTATEMENT_when_getInstance_then_returnEmptyDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.GIT, new FilePreferredApplicationProperties(new String[]{"codeProtection=statement"})
        );
        assertThat(instance).isInstanceOf(EmptyDiffCommand.class);
    }

    @Test
    void given_codeProtectionDefault_when_getInstance_then_returnGitDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.GIT, new FilePreferredApplicationProperties(new String[]{})
        );
        assertThat(instance).isInstanceOf(GitDiffCommand.class);
    }

    @Test
    void given_codeProtectionDefaultAndVcsSVN_when_getInstance_then_returnSvnDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.SVN, new FilePreferredApplicationProperties(new String[]{})
        );
        assertThat(instance).isInstanceOf(SvnDiffCommand.class);
    }

    @Test
    void given_codeProtectionDefaultAndVcsMercurial_when_getInstance_then_returnMercurialDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.MERCURIAL, new FilePreferredApplicationProperties(new String[]{})
        );
        assertThat(instance).isInstanceOf(MercurialDiffCommand.class);
    }

}