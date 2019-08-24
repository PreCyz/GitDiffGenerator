package pg.gipter.ui.upgrade;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

public class UpgradeWindow extends AbstractWindow {

    public UpgradeWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "upgrade.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
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
