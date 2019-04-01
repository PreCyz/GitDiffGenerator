package pg.gipter.ui.main;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/**Created by Gawa 2019-03-02*/
public class MainWindow extends AbstractWindow {

    public MainWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "main.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "main.css";
    }

    @Override
    public String windowTitleBundle() {
        return "main.title";
    }
}
