package pg.gipter.ui.project;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

/**Created by Gawa 2019-03-02*/
public class ProjectsThemedWindow extends AbstractWindow {

    public ProjectsThemedWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "theme/projects.fxml";
    }

    @Override
    protected ImageFile windowImgFileName() {
        return ImageFile.OFFICER_PNG;
    }

    @Override
    protected String cssFileName() {
        return "";
    }

    @Override
    public String windowTitleBundle() {
        return "projects.title";
    }
}
