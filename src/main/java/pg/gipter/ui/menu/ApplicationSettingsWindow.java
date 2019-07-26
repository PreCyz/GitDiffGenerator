package pg.gipter.ui.menu;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

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
    protected String windowImgFileName() {
        return "chicken-face.png";
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
