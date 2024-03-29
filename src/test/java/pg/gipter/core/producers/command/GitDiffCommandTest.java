package pg.gipter.core.producers.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import pg.gipter.MockitoExtension;
import pg.gipter.core.ApplicationProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitDiffCommandTest {

    @Mock
    private ApplicationProperties applicationProperties;
    private GitDiffCommand command;

    @Test
    void given_codeProtectionNONE_when_getInitialCommand_then_returnInitialCommandForNotProtectionNONE() {
        when(applicationProperties.itemType()).thenReturn(ItemType.SIMPLE);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("git", "log", "--remotes=origin*", "--patch", "--all");
    }

    @Test
    void given_codeProtectionSIMPLE_when_getInitialCommand_then_returnInitialCommandForProtectionSIMPLE() {
        when(applicationProperties.itemType()).thenReturn(ItemType.PROTECTED);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("git", "log", "--remotes=origin*", "--oneline", "--all");
    }

    @Test
    void given_skipRemoteTrue_when_getInitialCommand_then_returnInitialCommandWithoutRemotes() {
        when(applicationProperties.itemType()).thenReturn(ItemType.PROTECTED);
        when(applicationProperties.isSkipRemote()).thenReturn(true);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("git", "log", "--oneline", "--all");
    }

    @Test
    void given_allPossibleParameters_when_commandAsList_thenReturnCommand() {
        String author = "testAuthor";
        String committerEmail="test@email.com";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        ItemType uploadType = ItemType.SIMPLE;

        when(applicationProperties.gitAuthor()).thenReturn(author);
        when(applicationProperties.committerEmail()).thenReturn(committerEmail);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.itemType()).thenReturn(uploadType);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("git", "log", "--remotes=origin*", "--patch", "--all",
                "--author='" + author + "'",
                "--author=" + committerEmail,
                "--since", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                "--until", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }

    @Test
    void given_allParametersButCommitterEmail_when_commandAsList_thenReturnCommand() {
        String author = "testAuthor";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        ItemType uploadType = ItemType.SIMPLE;
        boolean skipRemote = true;

        when(applicationProperties.gitAuthor()).thenReturn(author);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.itemType()).thenReturn(uploadType);
        when(applicationProperties.isSkipRemote()).thenReturn(skipRemote);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("git", "log", "--patch", "--all",
                "--author='" + author + "'",
                "--since", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                "--until", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }

    @Test
    void givenAuthors_whenAuthors_thenReturnAuthors() {
        Set<String> authors = Stream.of("author1 lastname", "author2").collect(toCollection(LinkedHashSet::new));

        when(applicationProperties.authors()).thenReturn(authors);
        when(applicationProperties.gitAuthor()).thenReturn("");
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--author='author1 lastname'", "--author='author2'");
    }

    @Test
    void given_authorsAndGitAuthor_when_authors_thenReturnGitAuthor() {
        Set<String> authors = Stream.of("author1", "author2").collect(toCollection(LinkedHashSet::new));

        when(applicationProperties.authors()).thenReturn(authors);
        when(applicationProperties.gitAuthor()).thenReturn("gitAuthor");
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--author='gitAuthor'");
    }

    @Test
    void given_gitAuthorAndCommitterEmail_when_authors_thenReturnGitAuthorAndCommitterEmail() {
        when(applicationProperties.gitAuthor()).thenReturn("gitAuthor GitLastname");
        when(applicationProperties.committerEmail()).thenReturn("committerEmail");
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--author='gitAuthor GitLastname'", "--author=committerEmail");
    }

    @Test
    void givenGitDiffCommand_whenUpdateRepositoriesCommand_thenReturnProperList() {
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.updateRepositoriesCommand();

        assertThat(actual).containsExactly("git", "fetch", "--all");
    }
}