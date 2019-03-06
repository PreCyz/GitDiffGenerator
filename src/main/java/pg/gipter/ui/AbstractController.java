package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.nio.file.Paths;
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

    public static void setImageOnAlertWindow(Alert alert) {
        URL imgUrl = AbstractController.class.getClassLoader().getResource(Paths.get("img", "chicken-face.jpg").toString());
        if (imgUrl != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.toString()));
        }
    }

    static EventHandler<WindowEvent> regularOnCloseEventHandler() {
        return t -> platformExit();
    }

    static void platformExit() {
        Platform.exit();
        System.exit(0);
    }
}
