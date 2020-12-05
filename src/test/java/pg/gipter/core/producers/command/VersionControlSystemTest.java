package pg.gipter.core.producers.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class VersionControlSystemTest {

    private final Path TEST_PATH = Paths.get(".", "src", "test", "java", "resources");

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(TEST_PATH.toString(), VersionControlSystem.GIT.dirName()));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        try {
            Files.deleteIfExists(Paths.get(TEST_PATH.toString(), VersionControlSystem.SVN.dirName()));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        try {
            Files.deleteIfExists(Paths.get(TEST_PATH.toString(), VersionControlSystem.MERCURIAL.dirName()));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    void givenCurrentGitProjectPath_whenValueFrom_thenReturnGIT() throws IOException {
        final Path currentDir = Paths.get(TEST_PATH.toString(), VersionControlSystem.GIT.dirName());
        Files.createDirectory(currentDir);

        final VersionControlSystem actual = VersionControlSystem.valueFrom(TEST_PATH);

        assertThat(actual).isEqualByComparingTo(VersionControlSystem.GIT);
    }

    @Test
    void givenCurrentSvnProjectPath_whenValueFrom_thenReturnSVN() throws IOException {
        final Path currentDir = Paths.get(TEST_PATH.toString(), VersionControlSystem.SVN.dirName());
        Files.createDirectory(currentDir);

        final VersionControlSystem actual = VersionControlSystem.valueFrom(TEST_PATH);

        assertThat(actual).isEqualByComparingTo(VersionControlSystem.SVN);
        Files.deleteIfExists(currentDir);
    }

    @Test
    void givenCurrentMercurialProjectPath_whenValueFrom_thenReturnMERCURIAL() throws IOException {
        final Path currentDir = Paths.get(TEST_PATH.toString(), VersionControlSystem.MERCURIAL.dirName());
        Files.createDirectory(currentDir);

        final VersionControlSystem actual = VersionControlSystem.valueFrom(TEST_PATH);

        assertThat(actual).isEqualByComparingTo(VersionControlSystem.MERCURIAL);
        Files.deleteIfExists(currentDir);
    }

    @Test
    void givenNoVCS_whenValueFrom_thenThrowIllegalArgumentException() {
        try {
            VersionControlSystem.valueFrom(TEST_PATH);
            fail("Should throw IllegalArgumentException");
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        }
    }
}