package pg.gipter.core.producer.command;

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
class SvnDiffCommandTest {

    @Mock
    private ApplicationProperties applicationProperties;
    private SvnDiffCommand command;

    @Test
    void given_codeProtectionNONE_when_getInitialCommand_then_returnInitialCommandForProtectionNONE() {
        when(applicationProperties.uploadType()).thenReturn(UploadType.SIMPLE);
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.getInitialCommand();

        assertThat(actual).containsExactly("svn", "log", "--diff");
    }

    @Test
    void given_codeProtectionSIMPLE_when_getInitialCommand_then_returnInitialCommandForProtectionSIMPLE() {
        when(applicationProperties.uploadType()).thenReturn(UploadType.PROTECTED);
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
        UploadType uploadType = UploadType.SIMPLE;

        when(applicationProperties.svnAuthor()).thenReturn(author);
        when(applicationProperties.committerEmail()).thenReturn(committerEmail);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.uploadType()).thenReturn(uploadType);
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("svn", "log", "--diff",
                "--search", "\"" + author + "\"",
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
        UploadType uploadType = UploadType.SIMPLE;

        when(applicationProperties.svnAuthor()).thenReturn(author);
        when(applicationProperties.startDate()).thenReturn(startDate);
        when(applicationProperties.endDate()).thenReturn(endDate);
        when(applicationProperties.uploadType()).thenReturn(uploadType);
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.commandAsList();

        assertThat(actual).containsExactly("svn", "log", "--diff",
                "--search", "\"" + author + "\"",
                "--revision", "{" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "}:{" +
                        endDate.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "}"
        );
    }

    @Test
    void given_authors_when_authors_thenReturnAuthors() {
        Set<String> authors = Stream.of("author1", "author2").collect(toCollection(LinkedHashSet::new));

        when(applicationProperties.authors()).thenReturn(authors);
        when(applicationProperties.svnAuthor()).thenReturn("");
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--search", "\"author1\"", "--search", "\"author2\"");
    }

    @Test
    void given_authorsAndMercurialAuthor_when_authors_thenReturnMercurialAuthor() {
        Set<String> authors = Stream.of("author1", "author2").collect(toCollection(LinkedHashSet::new));

        when(applicationProperties.authors()).thenReturn(authors);
        when(applicationProperties.svnAuthor()).thenReturn("svnAuthor");
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--search", "\"svnAuthor\"");
    }

    @Test
    void given_mercurialAuthorAndCommitterEmail_when_authors_thenReturnMercurialAuthorAndCommitterEmail() {
        when(applicationProperties.svnAuthor()).thenReturn("svnAuthor");
        when(applicationProperties.committerEmail()).thenReturn("committerEmail");
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.authors();

        assertThat(actual).containsExactly("--search", "\"svnAuthor\"", "--search", "committerEmail");
    }

    @Test
    void givenMercurialDiffCommand_whenUpdateRepositoriesCommand_thenReturnProperList() {
        command = new SvnDiffCommand(applicationProperties);

        List<String> actual = command.updateRepositoriesCommand();

        assertThat(actual).containsExactly("svn", "update");
    }

}