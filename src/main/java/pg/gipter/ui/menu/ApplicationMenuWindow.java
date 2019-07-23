package pg.gipter.ui.menu;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ApplicationMenuWindow extends AbstractWindow {

    public ApplicationMenuWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "applicationMenu.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "applicationMenu.css";
    }

    @Override
    public String windowTitleBundle() {
        return "application.menu.title";
    }
}
