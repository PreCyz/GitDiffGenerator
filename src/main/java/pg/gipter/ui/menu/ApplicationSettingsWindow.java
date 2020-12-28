package pg.gipter.ui.menu;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ApplicationSettingsWindow extends AbstractWindow {

    public ApplicationSettingsWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "applicationSettings.fxml";
    }

    @Override
    protected ImageFile windowImgFileName() {
        return ImageFile.CHICKEN_FACE_PNG;
    }

    @Override
    protected String cssFileName() {
        return "applicationSettings.css";
    }

    @Override
    public String windowTitleBundle() {
        return "application.settings.title";
    }
}
