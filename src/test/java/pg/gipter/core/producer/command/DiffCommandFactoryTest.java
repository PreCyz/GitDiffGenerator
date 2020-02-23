package pg.gipter.core.producer.command;

import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationPropertiesFactory;

import static org.assertj.core.api.Assertions.assertThat;

class DiffCommandFactoryTest {

    @Test
    void givenCodeProtectionSTATEMENT_whenGetInstance_thenReturnEmptyDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.GIT,
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE", "uploadType=statement"})
        );
        assertThat(instance).isInstanceOf(EmptyDiffCommand.class);
    }

    @Test
    void given_codeProtectionDefault_when_getInstance_then_returnGitDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.GIT,
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );
        assertThat(instance).isInstanceOf(GitDiffCommand.class);
    }

    @Test
    void given_codeProtectionDefaultAndVcsSVN_when_getInstance_then_returnSvnDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.SVN,
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );
        assertThat(instance).isInstanceOf(SvnDiffCommand.class);
    }

    @Test
    void given_codeProtectionDefaultAndVcsMercurial_when_getInstance_then_returnMercurialDiffCommand() {
        DiffCommand instance = DiffCommandFactory.getInstance(
                VersionControlSystem.MERCURIAL,
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );
        assertThat(instance).isInstanceOf(MercurialDiffCommand.class);
    }

}