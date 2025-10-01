package pg.gipter.services.upgrade;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import pg.gipter.services.SemanticVersioning;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.LogLinkAction;
import pg.gipter.utils.BundleUtils;

import java.nio.file.*;
import java.util.LinkedList;
import java.util.Optional;

class UpgradeMsi extends AbstractUpgradeService {

    UpgradeMsi(SemanticVersioning currentVersion, String githubToken) {
        super(currentVersion, githubToken);
    }

    @Override
    public void upgradeAndRestartApplication() {
        updateMessage(BundleUtils.getMsg("upgrade.progress.started"));
        initProgress(githubService.getFileSize().orElse(0L));
        increaseProgress();
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
        try {
            Path downloadLocation = BackupService.gipterTmp();
            if (!Files.exists(downloadLocation)) {
                Files.createDirectory(downloadLocation);
            }
            if (Files.exists(downloadLocation)) {
                String downloadFolder = downloadLocation.toAbsolutePath().normalize().toString();
                Optional<String> fileName = githubService.downloadLatestDistribution(
                        downloadFolder,
                        this
                );
                if (fileName.isPresent()) {
                    updateMsg(BundleUtils.getMsg("upgrade.progress.backup"));
                    BackupService.backupAppFiles(this);
                    updateMsg(BundleUtils.getMsg("upgrade.progress.restarting"));
                    workCompleted();
                    Path installer = Paths.get(downloadFolder, fileName.get());
                    executeInstaller(installer);
                    System.exit(0);
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

    private void executeInstaller(Path installer) {
        try {
            LinkedList<String> fullCommand = new LinkedList<>();
            fullCommand.add("powershell.exe");
            fullCommand.add("-NoProfile");
            fullCommand.add("-NoLogo");
            fullCommand.add("-NonInteractive");
            fullCommand.add("Start-Process");
            fullCommand.add(installer.toAbsolutePath().toString());
            logger.info("Starting installer from: {}", installer.toAbsolutePath());
            Process start = new ProcessBuilder(fullCommand).start();
            if (start.isAlive()) {
                logger.info("Installer is starting...");
            }
        } catch (Exception e) {
            logger.error("Could not start installer located in [{}]. {}", installer.toAbsolutePath(), e.getMessage());
        }
    }

}
