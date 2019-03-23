package pg.gipter.ui;

import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**Created by Gawa 2017-10-04*/
public abstract class AbstractController implements Initializable {

    protected final UILauncher uiLauncher;
    protected URL location;

    protected AbstractController(UILauncher uiLauncher) {
        this.uiLauncher = uiLauncher;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.location = location;
    }


    protected static EventHandler<WindowEvent> regularOnCloseEventHandler() {
        return t -> UILauncher.platformExit();
    }

}
