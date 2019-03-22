package pg.gipter.ui;

import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
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

    public static void setImageOnAlertWindow(Alert alert) {
        URL imgUrl = AbstractController.class.getClassLoader().getResource("img/chicken-face.jpg");
        if (imgUrl != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.toString()));
        }
    }

    protected static EventHandler<WindowEvent> regularOnCloseEventHandler() {
        return t -> UILauncher.platformExit();
    }

}
