package pg.gipter.ui.menu.fileName;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/**Created by Gawa 2019-03-02*/
public class NameSettingsWindow extends AbstractWindow {

    public NameSettingsWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "nameSettings.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "nameSettings.css";
    }

    @Override
    public String windowTitleBundle() {
        return "nameSettings.title";
    }
}
