package pg.gipter.ui.upgrade;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

public class UpgradeWindow extends AbstractWindow {

    public UpgradeWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "upgrade.fxml";
    }

    @Override
    protected ImageFile windowImgFileName() {
        return ImageFile.CHICKEN_FACE_PNG;
    }

    @Override
    protected String cssFileName() {
        return "upgrade.css";
    }

    @Override
    public String windowTitleBundle() {
        return "upgrade.title";
    }

}
