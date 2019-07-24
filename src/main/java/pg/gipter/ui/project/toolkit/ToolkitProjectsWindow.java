package pg.gipter.ui.project.toolkit;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/**Created by Gawa 2019-03-02*/
public class ToolkitProjectsWindow extends AbstractWindow {

    public ToolkitProjectsWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "toolkitProjects.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "toolkitProjects.css";
    }

    @Override
    public String windowTitleBundle() {
        return "toolkit.projects.title";
    }
}
