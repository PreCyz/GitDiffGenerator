package pg.gipter.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Created by Pawel Gawedzki on 02-Aug-2019. */
class GithubServiceTest {

    private ApplicationProperties mockAppProps = mock(ApplicationProperties.class);
    private GithubService spyGithubService = spy(new GithubService(mockAppProps));

    @Test
    void givenNewerVersionInGithub_whenIsNewVersion_thenReturnTrue() {
        doReturn(Optional.of("3.6.2")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("3.6");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewerVersionInGithubV2_whenIsNewVersion_thenReturnTrue() {
        doReturn(Optional.of("3.6")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("3.5.5");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    @Disabled("This case should not be possible.")
    void givenOlderVersionInGithub_whenIsNewVersion_thenReturnFalse() {
        doReturn(Optional.of("3.6")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("3.6.1");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNewVersionInGithub_whenIsNewVersion_thenReturnTrue() {
        doReturn(Optional.of("3.7")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("3.6.12");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewVersionInGithub2_whenIsNewVersion_thenReturnTrue() {
        doReturn(Optional.of("3.7")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("1.3");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    void givenTheSameVersion_whenIsNewVersion_thenReturnFalse() {
        doReturn(Optional.of("3.6.2")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("3.6.2");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenVersionWithLetters_whenIsNewVersion_thenReturnFalse() {
        doReturn(Optional.of("3.6.5")).when(spyGithubService).getLatestVersion();
        when(mockAppProps.version()).thenReturn("3.6.6-alpha");

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }
}