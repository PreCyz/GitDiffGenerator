package pg.gipter.launcher;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import pg.gipter.settings.ApplicationProperties;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Button runButton;

    private ApplicationProperties applicationProperties;
    private Runnable runner;

    MainController(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.runner = new Runner(applicationProperties);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (runButton != null) {
            runButton.setOnAction(event -> runner.run());
        }
    }

}
