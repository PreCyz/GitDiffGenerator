package pg.gipter.ui.menu;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ToolkitSettingsWindow extends AbstractWindow {

    public ToolkitSettingsWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "toolkitSettings.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "toolkitSettings.css";
    }

    @Override
    public String windowTitleBundle() {
        return "toolkit.settings.title";
    }
}
