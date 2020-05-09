package pg.gipter.ui.job;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.alerts.ImageFile;

/**Created by Gawa 2019-03-02*/
public class JobWindow extends AbstractWindow {

    public JobWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "job.fxml";
    }

    @Override
    protected ImageFile windowImgFileName() {
        return ImageFile.CHICKEN_FACE_PNG;
    }

    @Override
    protected String cssFileName() {
        return "job.css";
    }

    @Override
    public String windowTitleBundle() {
        return "job.title";
    }
}
