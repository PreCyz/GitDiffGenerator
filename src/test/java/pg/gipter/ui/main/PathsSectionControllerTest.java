package pg.gipter.ui.main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.WebViewDetails;
import pg.gipter.ui.alerts.WebViewService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PathsSectionControllerTest {
    @Test
    void unavailableRepositoriesAreReportedTogetherAndCanBeRetried(@TempDir Path drive) throws Exception {
        Path first = drive.resolve("first");
        Path second = drive.resolve("second");
        var paths = new LinkedHashSet<>(List.of(first.toString(), second.toString()));
        var properties = mock(ApplicationProperties.class);
        when(properties.configurationName()).thenReturn("MACD");
        when(properties.projectPaths()).thenReturn(paths);
        var controller = new PathsSectionController(null, properties, null);

        var webViews = mock(WebViewService.class);
        when(webViews.pullFailWebView()).thenReturn(mock(WebViewDetails.class));
        try (var service = mockStatic(WebViewService.class);
             var alerts = mockConstruction(AlertWindowBuilder.class, withSettings().defaultAnswer(RETURNS_SELF))) {
            service.when(WebViewService::getInstance).thenReturn(webViews);
            controller.warnAboutUnavailableRepositories(VersionControlSystem.inspectProjectPaths(paths));
            assertThat(alerts.constructed()).hasSize(1);
            var warning = alerts.constructed().getFirst();
            verify(warning).withMessage(contains(String.join(System.lineSeparator(), paths)));
            verify(warning).buildAndDisplayWindow();
            assertThat(controller.getProjectPaths()).isEqualTo(first + "," + second);

            Files.createDirectories(first.resolve(".git"));
            Files.createDirectories(second.resolve(".git"));
            var recovered = VersionControlSystem.inspectProjectPaths(paths);
            assertThat(recovered.values()).containsExactly(VersionControlSystem.GIT, VersionControlSystem.GIT);
            controller.warnAboutUnavailableRepositories(recovered);
            assertThat(alerts.constructed()).hasSize(1);
            verify(properties, never()).addProjectPath(anyString());
            verify(properties, never()).save();
        }
    }
}
