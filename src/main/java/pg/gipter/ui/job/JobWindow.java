package pg.gipter.ui.job;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

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
    protected String windowImgFileName() {
        return "chicken-face.jpg";
    }

    @Override
    protected String cssFileName() {
        return "job.css";
    }

    @Override
    public String windowTitleBundle() {
        return "job.main.title";
    }
}
