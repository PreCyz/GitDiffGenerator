package pg.gipter.producer.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import pg.gipter.MockitoExtension;
import pg.gipter.settings.ApplicationProperties;

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
class MercurialDiffCommandTest {

    @Mock
    private ApplicationProperties applicationProperties;
    private MercurialDiffCommand command;

    @Test
    void given_codeProtectionNONE_when_getInitialCommand_then_returnInitialCommandForProtectionNONE() {
        when(applicationProperties.codeProtection()).thenReturn(CodeProtection.NONE);
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("hg", "log", "--patch");
    }

    @Test
    void given_codeProtectionSIMPLE_when_getInitialCommand_then_returnInitialCommandForProtectionSIMPLE() {
        when(applicationProperties.codeProtection()).thenReturn(CodeProtection.SIMPLE);
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("hg", "log", "--style", "changelog");
    }

    @Test
    void given_allPossibleParameters_when_commandAsList_thenReturnCommand() {
        String author = "testAuthor";
        String committerEmail="test@email.com";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        CodeProtection codeProtection = CodeProtection.NONE;

        when(applicationProperties.mercurialAuthor()).thenReturn(author);
        when(applicationProperties.committerEmail()).thenReturn(committerEmail);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.codeProtection()).thenReturn(codeProtection);
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("hg", "log", "--patch",
                "--user", author,
                "--user", committerEmail,
                "--date", "\"" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " to " +
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\""
        );
    }

    @Test
    void given_allParametersButCommitterEmail_when_commandAsList_thenReturnCommand() {
        String author = "testAuthor";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        CodeProtection codeProtection = CodeProtection.NONE;

        when(applicationProperties.mercurialAuthor()).thenReturn(author);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.codeProtection()).thenReturn(codeProtection);
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("hg", "log", "--patch",
                "--user", author,
                "--date", "\"" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " to " +
                        endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\""
        );
    }

    @Test
    void given_authors_when_authors_thenReturnAuthors() {
        Set<String> authors = Stream.of("author1", "author2").collect(toCollection(LinkedHashSet::new));

        when(applicationProperties.authors()).thenReturn(authors);
        when(applicationProperties.mercurialAuthor()).thenReturn("");
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--user", "author1", "--user", "author2");
    }

    @Test
    void given_authorsAndMercurialAuthor_when_authors_thenReturnMercurialAuthor() {
        Set<String> authors = Stream.of("author1", "author2").collect(toCollection(LinkedHashSet::new));

        when(applicationProperties.authors()).thenReturn(authors);
        when(applicationProperties.mercurialAuthor()).thenReturn("mercurialAuthor");
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--user", "mercurialAuthor");
    }

    @Test
    void given_mercurialAuthorAndCommitterEmail_when_authors_thenReturnMercurialAuthorAndCommitterEmail() {
        when(applicationProperties.mercurialAuthor()).thenReturn("mercurialAuthor");
        when(applicationProperties.committerEmail()).thenReturn("committerEmail");
        command = new MercurialDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--user", "mercurialAuthor", "--user", "committerEmail");
    }

}