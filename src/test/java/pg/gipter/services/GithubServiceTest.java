package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/** Created by Pawel Gawedzki on 02-Aug-2019. */
class GithubServiceTest {

    private final ApplicationProperties mockAppProps = mock(ApplicationProperties.class);
    private GithubService spyGithubService;

    @Test
    void jarWithBundledJreSelectsJarArchive(@TempDir Path installation) throws IOException {
        Path runtime = Files.createDirectory(installation.resolve("runtime"));
        Files.writeString(runtime.resolve("release"), "IMAGE_TYPE=\"JRE\"");
        Files.createFile(installation.resolve("Gipter.exe"));
        JsonObject release = new Gson().fromJson("""
                {"name":"v5.1","assets":[
                    {"name":"Gipter-v5.1-portable.7z","url":"exe"},
                    {"name":"11+Gipter-v5.1.msi","url":"msi"},
                    {"name":"11+Gipter-v5.1.7z","url":"jar"}]}
                """, JsonObject.class);
        try (var system = mockStatic(SystemUtils.class, CALLS_REAL_METHODS);
             var source = mockStatic(JarHelper.class)) {
            system.when(SystemUtils::javaHome).thenReturn(runtime.toString());
            source.when(JarHelper::getJarPath).thenReturn(Optional.of(installation.resolve("Gipter.jar")));
            var service = new GithubService(new SemanticVersioning(5, 0, 0), "");
            assertThat(service.getDownloadLink(release)).contains("jar");
        }
    }

    @Test
    void givenNewerVersionInGithub_whenIsNewVersion_thenReturnTrue() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("3.6"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6.2"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    void givenNewerVersionInGithubV2_whenIsNewVersion_thenReturnTrue() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("3.5.5"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    @Disabled("This case should not be possible.")
    void givenOlderVersionInGithub_whenIsNewVersion_thenReturnFalse() {
        when(mockAppProps.version()).thenReturn(new SemanticVersioning(3, 6, 1));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNewVersionInGithub_whenIsNewVersion_thenReturnTrue() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("3.6.12"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.7"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    @Disabled
    void givenOldVersionInGithub_whenIsNewVersion_thenReturnFalse() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("3.7"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6.14"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNewVersionInGithub2_whenIsNewVersion_thenReturnTrue() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("1.3"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.7"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isTrue();
    }

    @Test
    void givenTheSameVersion_whenIsNewVersion_thenReturnFalse() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("3.6.2"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6.2"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenVersionWithLetters_whenIsNewVersion_thenReturnFalse() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("3.6.6-alpha"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6.5"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenServerVersionOlderThenCurrentVersion_whenIsNewVersion_thenReturnFalse() {
        when(mockAppProps.version()).thenReturn(SemanticVersioning.getSemanticVersioning("4.0.0"));
        spyGithubService = spy(new GithubService(mockAppProps.version(), mockAppProps.githubToken()));
        doReturn(Optional.of(SemanticVersioning.getSemanticVersioning("3.6.14"))).when(spyGithubService).getLatestVersion();

        boolean actual = spyGithubService.isNewVersion();

        assertThat(actual).isFalse();
    }

    @Test
    void givenLatestDistroDetails_whenGetDownloadLink_thenFileDownloaded() throws FileNotFoundException {
        spyGithubService = spy(new GithubService(SemanticVersioning.getSemanticVersioning("1.0"), mockAppProps.githubToken()));
        String json = String.format(".%ssrc%stest%sjava%sresources%slatestDistributionDetails.json",
                File.separator, File.separator, File.separator, File.separator, File.separator);
        Reader reader = new BufferedReader(new FileReader(json));

        Optional<String> fileName = spyGithubService.getDownloadLink(new Gson().fromJson(reader, JsonObject.class));

        assertThat(fileName.isPresent()).isTrue();
        assertThat(spyGithubService.distributionName).isEqualTo("11+Gipter_v3.6.6.7z");
    }

}
