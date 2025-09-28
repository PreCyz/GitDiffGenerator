package pg.gipter.services.upgrade;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import pg.gipter.services.SemanticVersioning;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.LogLinkAction;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.JarHelper;

import java.nio.file.*;
import java.util.Optional;

class UpgradeExe extends AbstractUpgradeService {

    UpgradeExe(SemanticVersioning currentVersion, String githubToken) {
        super(currentVersion, githubToken);
    }

    @Override
    public void upgradeAndRestartApplication() {
        updateMessage(BundleUtils.getMsg("upgrade.progress.started"));
        initProgress(githubService.getFileSize().orElse(0L));
        increaseProgress();
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
        try {
            Optional<String> homeDirectoryPath = JarHelper.homeDirectoryPath();
            if (homeDirectoryPath.isPresent()) {
                Optional<String> fileName = githubService.downloadLatestDistribution(homeDirectoryPath.get(), this);
                if (fileName.isPresent()) {
                    Files.move(
                            Paths.get(homeDirectoryPath.get(), fileName.get()),
                            Paths.get(homeDirectoryPath.get(), "Gipter.exe"),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    updateMsg(BundleUtils.getMsg("upgrade.progress.backup"));
                    BackupService.backupAppFiles(this);
                    finalizeUpgrade();
                } else {
                    logger.error("Did not download the newest version.");
                    alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                            .withLinkAction(new LogLinkAction())
                            .withAlertType(Alert.AlertType.WARNING);
                }
            } else {
                logger.error("Can not find home directory.");
                alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.WARNING);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            updateMsg(BundleUtils.getMsg("upgrade.fail"));
            alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                    .withMessage(ex.getMessage())
                    .withLinkAction(new LogLinkAction())
                    .withAlertType(Alert.AlertType.WARNING);
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
        workCompleted();
        logger.info("Is restart task done: [{}]", isDone());
    }

}
