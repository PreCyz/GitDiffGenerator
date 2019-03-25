package pg.gipter.ui.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("It works only on Linux because of paths")
class ProjectsControllerTest {

    @Test
    void givenGitFolder_whenSearchForProjects_thenReturnListWithOneElement() {
        File file = new File("/home/gawa/IdeaProjects/GitDiffGenerator");
        ProjectsController controller = new ProjectsController(null, null);
        List<ProjectDetails> actual = controller.searchForProjects(file);

        assertThat(actual).hasSize(1);
    }

    @Test
    void givenGeneralFolder_whenSearchForProjects_thenReturnList() {
        File file = new File("/home/gawa/IdeaProjects");
        ProjectsController controller = new ProjectsController(null, null);
        List<ProjectDetails> actual = controller.searchForProjects(file);

        assertThat(actual).hasSize(12);
    }
}