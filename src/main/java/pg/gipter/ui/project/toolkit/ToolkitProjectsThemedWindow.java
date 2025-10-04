package pg.gipter.ui.project.toolkit;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

/**Created by Gawa 2019-03-02*/
public class ToolkitProjectsThemedWindow extends AbstractWindow {

    public ToolkitProjectsThemedWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "theme/toolkitProjects.fxml";
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
        return "toolkit.projects.title";
    }
}
