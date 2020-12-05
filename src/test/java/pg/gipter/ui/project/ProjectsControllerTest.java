package pg.gipter.ui.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("It works only on Linux because of paths")
class ProjectsControllerTest {

    @Test
    void givenGitFolder_whenSearchForProjects_thenReturnListWithOneElement() {
        Path file = Paths.get("/home/gawa/IdeaProjects/GitDiffGenerator");
        ProjectsController controller = new ProjectsController(null, null);
        List<ProjectDetails> actual = controller.searchForProjects(file);

        assertThat(actual).hasSize(1);
    }

    @Test
    void givenGeneralFolder_whenSearchForProjects_thenReturnList() {
        Path file = Paths.get("/home/gawa/IdeaProjects");
        ProjectsController controller = new ProjectsController(null, null);
        List<ProjectDetails> actual = controller.searchForProjects(file);

        assertThat(actual).hasSize(12);
    }
}