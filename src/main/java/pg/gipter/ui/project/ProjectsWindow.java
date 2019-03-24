package pg.gipter.ui.project;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/**Created by Gawa 2019-03-02*/
public class ProjectsWindow extends AbstractWindow {

    public ProjectsWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "projects.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.jpg";
    }

    @Override
    protected String cssFileName() {
        return "projects.css";
    }

    @Override
    public String windowTitleBundle() {
        return "projects.title";
    }
}
