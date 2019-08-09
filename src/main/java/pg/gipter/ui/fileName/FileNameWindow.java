package pg.gipter.ui.fileName;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/**Created by Gawa 2019-03-02*/
public class FileNameWindow extends AbstractWindow {

    public FileNameWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "fileNameSettings.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "fileNameSettings.css";
    }

    @Override
    public String windowTitleBundle() {
        return "fileNameSettings.title";
    }
}
