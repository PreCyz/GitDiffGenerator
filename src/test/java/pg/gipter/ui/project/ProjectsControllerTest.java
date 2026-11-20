package pg.gipter.ui.project;

import javafx.stage.DirectoryChooser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.ui.UILauncher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectsControllerTest {

    @Test
    void cancellingAddProjectReturnsWithoutUpdatingTheTable() {
        var controller = new ProjectsController(null, mock(UILauncher.class));
        try (var choosers = mockConstruction(DirectoryChooser.class)) {
            controller.addButtonActionEventHandler(mock(ResourceBundle.class)).handle(null);
            verify(choosers.constructed().getFirst()).showDialog(null);
        }
    }

    @Test
    void editorPreservesUnavailableProjectsAlongsideAvailableOnes(@TempDir Path drive) throws IOException {
        Path offline = drive.resolve("offline");
        Path available = drive.resolve("available");
        Files.createDirectories(available.resolve(".git"));
        var properties = mock(ApplicationProperties.class);
        when(properties.itemType()).thenReturn(ItemType.SIMPLE);
        when(properties.projectPaths()).thenReturn(new LinkedHashSet<>(List.of(
                "", ArgName.projectPath.defaultValue(), offline.toString(), available.toString())));
        var controller = new ProjectsController(properties, null);

        List<ProjectDetails> rows = controller.configuredProjects();
        assertThat(rows).extracting(ProjectDetails::getPath).containsExactly(offline.toString(), available.toString());
        assertThat(rows.getFirst().getVcsType()).isNotEqualTo("GIT");
        assertThat(rows.getLast().getVcsType()).isEqualTo("GIT");
    }

    @Test
    void emptyAndLegacyPlaceholderPathsShowOnlyTheDefaultRow() {
        var properties = mock(ApplicationProperties.class);
        when(properties.itemType()).thenReturn(ItemType.SIMPLE);
        when(properties.projectPaths()).thenReturn(java.util.Set.of("", ArgName.projectPath.defaultValue()));
        assertThat(new ProjectsController(properties, null).configuredProjects()).containsExactly(ProjectDetails.DEFAULT);
    }

    @Disabled("It works only on Linux because of paths")
    @Test
    void givenGitFolder_whenSearchForProjects_thenReturnListWithOneElement() {
        Path file = Paths.get("/home/gawa/IdeaProjects/GitDiffGenerator");
        ProjectsController controller = new ProjectsController(null, null);
        List<ProjectDetails> actual = controller.searchForProjects(file);

        assertThat(actual).hasSize(1);
    }

    @Disabled("It works only on Linux because of paths")
    @Test
    void givenGeneralFolder_whenSearchForProjects_thenReturnList() {
        Path file = Paths.get("/home/gawa/IdeaProjects");
        ProjectsController controller = new ProjectsController(null, null);
        List<ProjectDetails> actual = controller.searchForProjects(file);

        assertThat(actual).hasSize(12);
    }
}
