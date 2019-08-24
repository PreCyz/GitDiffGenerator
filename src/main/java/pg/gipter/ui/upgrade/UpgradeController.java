package pg.gipter.ui.upgrade;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import pg.gipter.service.UpgradeService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.ResourceBundle;

public class UpgradeController  extends AbstractController {

    @FXML
    private ProgressBar upgradeProgressBar;
    @FXML
    private Label upgradeLabel;

    private Task<Void> upgradeService;

    public UpgradeController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.upgradeService = new UpgradeService(applicationProperties.version());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        resetIndicatorProperties(upgradeService);
        uiLauncher.executeOutsideUIThread(() -> {
            upgradeService.run();
            Platform.runLater(() -> {
                uiLauncher.hideUpgradeWindow();
                uiLauncher.execute();
            });
        });
    }

    private void resetIndicatorProperties(Task<?> task) {
        upgradeProgressBar.progressProperty().unbind();
        upgradeProgressBar.progressProperty().bind(task.progressProperty());
        upgradeLabel.textProperty().unbind();
        upgradeLabel.textProperty().bind(task.messageProperty());
    }
}
