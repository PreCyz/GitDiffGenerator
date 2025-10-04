package pg.gipter.ui.menu;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ToolkitSettingsThemedWindow extends AbstractWindow {

    public ToolkitSettingsThemedWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "theme/toolkitSettings.fxml";
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
        return "toolkit.settings.title";
    }
}
