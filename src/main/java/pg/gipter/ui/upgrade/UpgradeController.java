package pg.gipter.ui.upgrade;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.services.UpgradeService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.ResourceBundle;

public class UpgradeController  extends AbstractController {

    @FXML
    private ProgressBar upgradeProgressBar;
    @FXML
    private Label upgradeLabel;

    private final Task<Void> upgradeService;

    public UpgradeController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.upgradeService = new UpgradeService(applicationProperties.version());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        resetIndicatorProperties(upgradeService);
        upgradeLabel.setAlignment(Pos.CENTER);
        upgrade();
    }

    private void upgrade() {
        uiLauncher.executeOutsideUIThread(() -> {
            upgradeService.run();
            Platform.runLater(() -> {
                uiLauncher.hideUpgradeWindow();
                uiLauncher.execute();
            });
        });
    }

    private void resetIndicatorProperties(Task<?> task) {
        upgradeProgressBar.setProgress(0);
        upgradeProgressBar.progressProperty().unbind();
        upgradeProgressBar.progressProperty().bind(task.progressProperty());
        upgradeLabel.textProperty().unbind();
        upgradeLabel.textProperty().bind(task.messageProperty());
    }
}
