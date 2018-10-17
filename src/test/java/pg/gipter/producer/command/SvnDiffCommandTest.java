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
class SvnDiffCommandTest {

    @Mock
    private ApplicationProperties applicationProperties;
    private SvnDiffCommand command;

    @Test
    void given_codeProtectionNONE_when_getInitialCommand_then_returnInitialCommandForProtectionNONE() {
        when(applicationProperties.codeProtection()).thenReturn(CodeProtection.NONE);
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("svn", "log", "--diff");
    }

    @Test
    void given_codeProtectionSIMPLE_when_getInitialCommand_then_returnInitialCommandForProtectionSIMPLE() {
        when(applicationProperties.codeProtection()).thenReturn(CodeProtection.SIMPLE);
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("svn", "log", "--verbose");
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
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("svn", "log", "--diff",
                "--search", author,
                "--search", committerEmail,
                "--revision", "{" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "}:{" +
                        endDate.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "}"
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
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("svn", "log", "--diff",
                "--search", author,
                "--revision", "{" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "}:{" +
                        endDate.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "}"
        );
    }

}