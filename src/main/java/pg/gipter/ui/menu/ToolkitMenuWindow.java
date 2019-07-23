package pg.gipter.ui.menu;

import pg.gipter.ui.AbstractController;
import pg.gipter.ui.AbstractWindow;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ToolkitMenuWindow extends AbstractWindow {

    public ToolkitMenuWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "toolkitMenu.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.png";
    }

    @Override
    protected String cssFileName() {
        return "toolkitMenu.css";
    }

    @Override
    public String windowTitleBundle() {
        return "toolkit.menu.title";
    }
}
