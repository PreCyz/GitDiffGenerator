package pg.gipter.producer.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import pg.gipter.MockitoExtension;
import pg.gipter.settings.ApplicationProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitDiffCommandTest {

    @Mock
    private ApplicationProperties applicationProperties;
    private GitDiffCommand command;

    @Test
    void given_codeProtectionNONE_when_getInitialCommand_then_returnInitialCommandForNotProtectionNONE() {
        when(applicationProperties.codeProtection()).thenReturn(CodeProtection.NONE);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("git", "log", "--patch", "--all");
    }

    @Test
    void given_codeProtectionSIMPLE_when_getInitialCommand_then_returnInitialCommandForProtectionSIMPLE() {
        when(applicationProperties.codeProtection()).thenReturn(CodeProtection.SIMPLE);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("git", "log", "--decorate");
    }

    @Test
    void given_allPossibleParameters_when_commandAsList_thenReturnCommand() {
        String author = "testAuthor";
        String committerEmail="test@email.com";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        CodeProtection codeProtection = CodeProtection.NONE;

        when(applicationProperties.author()).thenReturn(author);
        when(applicationProperties.committerEmail()).thenReturn(committerEmail);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.codeProtection()).thenReturn(codeProtection);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("git", "log", "--patch", "--all",
                "--author=" + author,
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
        CodeProtection codeProtection = CodeProtection.NONE;

        when(applicationProperties.author()).thenReturn(author);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.codeProtection()).thenReturn(codeProtection);
        command = new GitDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("git", "log", "--patch", "--all",
                "--author=" + author,
                "--since", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                "--until", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }
}