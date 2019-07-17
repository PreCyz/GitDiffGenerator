package pg.gipter.ui.configuration;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

public class ConfigurationWindow extends AbstractWindow {

    public ConfigurationWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "new-configuration.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "new-configuration.css";
    }

    @Override
    public String windowTitleBundle() {
        return "new.configuration.title";
    }
}
