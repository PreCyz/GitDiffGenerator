package pg.gipter.ui.project.sharepoint;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

public class SharePointProjectWindow extends AbstractWindow {

    public SharePointProjectWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "sharepointProject.fxml";
    }

    @Override
    protected ImageFile windowImgFileName() {
        return ImageFile.CHICKEN_FACE_PNG;
    }

    @Override
    protected String cssFileName() {
        return "sharepointProject.css";
    }

    @Override
    public String windowTitleBundle() {
        return "sharepoint.title";
    }
}
