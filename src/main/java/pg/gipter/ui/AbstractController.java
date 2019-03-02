package pg.gipter.ui;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**Created by Gawa 2017-10-04*/
public abstract class AbstractController implements Initializable {

    protected final UILauncher uiLauncher;
    protected URL location;
    protected ResourceBundle bundle;

    protected AbstractController(UILauncher uiLauncher) {
        this.uiLauncher = uiLauncher;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.location = location;
        bundle = resources;
    }
}
