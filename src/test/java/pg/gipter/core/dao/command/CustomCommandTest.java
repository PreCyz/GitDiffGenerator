package pg.gipter.core.dao.command;

import org.junit.jupiter.api.Test;
import pg.gipter.core.*;
import pg.gipter.core.producers.command.VersionControlSystem;

import java.util.Arrays;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

class CustomCommandTest {

    @Test
    void givenNoCustomCommand_whenFullCommand_returnEmptyList() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{}
        );
        CustomCommand customCommand = new CustomCommand();

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).hasSize(0);
    }

    @Test
    void givenCustomCommandWithPlaceholders_whenFullCommand_returnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.author + "=precyz",
                        ArgName.startDate + "=2020-06-05",
                        ArgName.preferredArgSource + "=" + ArgName.preferredArgSource.defaultValue(),
                        ArgName.useUI + "=N"
                }
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommand("git log --author=${author} --oneline 'startDate ${startDate}'");

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).containsExactly("git", "log", "--author='precyz'", "--oneline", "startDate 2020-06-05");
    }

    @Test
    void givenCustomCommandAsListWithPlaceholders_whenFullCommand_returnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.author + "=precyz",
                        ArgName.startDate + "=2020-06-05",
                        ArgName.preferredArgSource + "=" + ArgName.preferredArgSource.defaultValue(),
                        ArgName.useUI + "=N"
                }
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommandList(Arrays.asList(
                "git", "log", "--author=${author}", "--oneline", "startDate ${startDate}"
        ));

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).containsExactly("git", "log", "--author='precyz'", "--oneline", "startDate 2020-06-05");
    }

    @Test
    void givenStringCustomCommand_whenContainsCommand_thenReturnTrue() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.GIT);
        customCommand.setCommand("git log");

        assertThat(customCommand.containsCommand(VersionControlSystem.GIT)).isTrue();
    }

    @Test
    void givenListCustomCommand_whenContainsCommand_thenReturnTrue() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.GIT);
        customCommand.setCommandList(Arrays.asList("git", "log"));

        assertThat(customCommand.containsCommand(VersionControlSystem.GIT)).isTrue();
    }

    @Test
    void givenStringCustomCommandAndWrongVcs_whenContainsCommand_thenReturnFalse() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.SVN);
        customCommand.setCommand("git log");

        assertThat(customCommand.containsCommand(VersionControlSystem.SVN)).isTrue();
    }

    @Test
    void givenListCustomCommandAndWrongVcs_whenContainsCommand_thenReturnFalse() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.SVN);
        customCommand.setCommandList(Arrays.asList("git", "log"));

        assertThat(customCommand.containsCommand(VersionControlSystem.SVN)).isTrue();
    }

    @Test
    void givenStringCustomCommandAndNoVcs_whenContainsCommand_thenReturnTrue() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommand("git log");

        assertThat(customCommand.containsCommand(VersionControlSystem.GIT)).isTrue();
    }

    @Test
    void givenListCustomCommandAndNoVcs_whenContainsCommand_thenReturnTrue() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommandList(Arrays.asList("git", "log"));

        assertThat(customCommand.containsCommand(VersionControlSystem.GIT)).isTrue();
    }

    @Test
    void givenStringCustomCommandAndNoVcsAndWrongVcsParam_whenContainsCommand_thenReturnFalse() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommand("git log");

        assertThat(customCommand.containsCommand(VersionControlSystem.MERCURIAL)).isFalse();
    }

    @Test
    void givenListCustomCommandAndNoVcsAndWrongVcsParam_whenContainsCommand_thenReturnFalse() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommandList(Arrays.asList("git", "log"));

        assertThat(customCommand.containsCommand(VersionControlSystem.MERCURIAL)).isFalse();
    }

    @Test
    void givenCommandDifferentFromCommandList_whenFullCommand_thenReturnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.author + "=precyz", ArgName.startDate + "=2020-06-05"}
        );

        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommand("git log");
        customCommand.setCommandList(Arrays.asList("git", "status"));

        assertThat(customCommand.fullCommand(applicationProperties)).containsExactly("git", "log");
    }

    @Test
    void givenCustomCommandAsListWithPlaceholdersAndNoAuthor_whenFullCommand_returnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.startDate + "=2020-06-05"}
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommandList(Arrays.asList(
                "git", "log", "--author=${author}", "--oneline", "startDate ${startDate}"
        ));

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).containsExactly("git", "log", "--author='" + ArgName.author.defaultValue() + "'", "--oneline", "startDate 2020-06-05");
    }

    @Test
    void givenCustomCommandWithPlaceholdersAndNoAuthor_whenFullCommand_returnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.startDate + "=2020-06-05",
                        ArgName.preferredArgSource + "=" + ArgName.preferredArgSource.defaultValue(),
                        ArgName.useUI + "=N"
                }
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommand("git log --author=${author} --oneline 'startDate ${startDate}'");

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).containsExactly("git", "log", "--author='" + ArgName.author.defaultValue() + "'", "--oneline", "startDate 2020-06-05");
    }

    @Test
    void givenCustomCommandAsListWithPlaceholdersAndEscapedAuthor_whenFullCommand_returnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.author + "=Obiwan Kenobi",
                        ArgName.startDate + "=2020-06-05",
                        ArgName.preferredArgSource + "=" + ArgName.preferredArgSource.defaultValue(),
                        ArgName.useUI + "=N"
                }
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommandList(Arrays.asList(
                "git", "log", "--author=${author}", "--oneline", "startDate ${startDate}"
        ));

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).containsExactly("git", "log", "--author='Obiwan Kenobi'", "--oneline", "startDate 2020-06-05");
    }

    @Test
    void givenCustomCommandWithPlaceholdersAndEscapedAuthor_whenFullCommand_returnCommand() {
        final ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.author + "=Obiwan Kenobi",
                        ArgName.startDate + "=2020-06-05",
                        ArgName.preferredArgSource + "=" + ArgName.preferredArgSource.defaultValue(),
                        ArgName.useUI + "=N"
                }
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setCommand("git log --author=${author} --oneline 'startDate ${startDate}'");

        final LinkedList<String> actual = customCommand.fullCommand(applicationProperties);

        assertThat(actual).containsExactly("git", "log", "--author='Obiwan Kenobi'", "--oneline", "startDate 2020-06-05");
    }
}