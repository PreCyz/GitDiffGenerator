package pg.gipter.services.vcs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs(OS.LINUX)
class GitServiceTest {

    @Test
    void givenGitProjectPath_whenGetUserName_thenReturnUserName() {
        GitService gitService = new GitService();
        gitService.setProjectPath(".");
        Optional<String> actual = gitService.getUserName();
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isNotBlank();
    }

    @Test
    void givenGitProjectPath_whenGetUserEmail_thenReturnUserEmail() {
        GitService gitService = new GitService();
        gitService.setProjectPath(".");
        Optional<String> actual = gitService.getUserEmail();
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isNotBlank();
    }
}