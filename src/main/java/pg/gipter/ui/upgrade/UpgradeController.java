package pg.gipter.ui.upgrade;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import mslinks.ShellLink;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.services.upgrade.UpgradeServiceFactory;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.SystemUtils;

import java.net.URL;
import java.nio.file.*;
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
        this.upgradeService = UpgradeServiceFactory.getUpgradeServiceInstance(applicationProperties.version(), applicationProperties.githubToken());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        resetIndicatorProperties(upgradeService);
        upgradeLabel.setAlignment(Pos.CENTER);
        upgrade();
    }

    private void upgrade() {
        createShortcut(Paths.get(".").normalize().toAbsolutePath());
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

    /** This method ?from some reason? must be called here, otherwise,
     * shortcut creation does not work in UpgradeService!
     * */
    private void createShortcut(Path destinationDir) {
        Path jar = Paths.get(destinationDir.toString(), "Gipter.jar").normalize().toAbsolutePath();
        if (Files.exists(jar)) {
            try {
                Path shortcutLnkPath = Paths.get(destinationDir.toString(), "Gipter.lnk");
                String target = Paths.get(SystemUtils.javaHome(), "bin", "javaw.exe").toString();

                logger.info("Creating shortcut [{}] for [{}] with Java runtime [{}].",
                        shortcutLnkPath,
                        jar,
                        target
                );

                ShellLink shellLink = ShellLink.createLink(target)
                        .setWorkingDir(destinationDir.toString())
                        .setCMDArgs(String.format(" -jar \"%s\"", jar));

                Path iconPath = Paths.get(destinationDir.toString(), "gipter.ico").toAbsolutePath();
                if (Files.exists(iconPath)) {
                    shellLink = shellLink.setIconLocation(iconPath.toString());
                    shellLink.getHeader().setIconIndex(0);
                }

                shellLink.saveTo(shortcutLnkPath.toString());
                logger.info("Shortcut created. [{}]", shortcutLnkPath);
            } catch (Exception e) {
                logger.error("Shortcut [Gipter.lnk] was not created. {}", e.getMessage());
            }
        }
    }
}
