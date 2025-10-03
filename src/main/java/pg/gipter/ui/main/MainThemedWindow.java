package pg.gipter.ui.main;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

/**Created by Gawa 2019-03-02*/
public class MainThemedWindow extends AbstractWindow {

    public MainThemedWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "theme/main.fxml";
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
        return "main.title";
    }
}
