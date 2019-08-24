package pg.gipter.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpgradeService extends TaskService<Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeService.class);

    private GithubService githubService;

    public UpgradeService(String currentVersion) {
        super();
        githubService = new GithubService(currentVersion);
    }

    void upgradeAndRestartApplication() {
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
        try {
            Optional<String> homeDirectoryPath = AlertHelper.homeDirectoryPath();
            if (homeDirectoryPath.isPresent()) {
                Optional<String> fileName = githubService.downloadLatestDistribution(homeDirectoryPath.get(), this);
                if (fileName.isPresent()) {
                    File sevenZFile = Paths.get(homeDirectoryPath.get(), fileName.get()).toFile();
                    decompress(sevenZFile, Paths.get(homeDirectoryPath.get()).toFile());
                    restartApplication();
                } else {
                    logger.error("Did not downloaded the new version.");
                    alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                            .withLink(AlertHelper.logsFolder())
                            .withWindowType(WindowType.LOG_WINDOW)
                            .withAlertType(Alert.AlertType.WARNING);
                }
            } else {
                logger.error("Can not find home directory.");
                alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.WARNING);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                    .withMessage(ex.getMessage())
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.WARNING);
        } finally {
            taskUpdateMessage(BundleUtils.getMsg("upgrade.fail"));
            workCompleted();
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
    }

    private void decompress(File sevenZSourceFile, File destination) throws IOException {
        Platform.runLater(() -> {
            taskUpdateMessage("Decompressing files ...");
            taskUpdateProgress((long) getWorkDone() + 1);
        });
        try (SevenZFile sevenZFile = new SevenZFile(sevenZSourceFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File currentFile = new File(destination, entry.getName());
                File parent = currentFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(currentFile);
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
            }
        } finally {
            taskUpdateMessage("Deleting downloaded file ...");
            FileUtils.forceDelete(sevenZSourceFile);
            logger.info("File [{}] deleted.", sevenZSourceFile.getName());
        }
    }

    private void restartApplication() throws IOException {
        taskUpdateMessage("Restarting application.");
        workCompleted();
        final String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Optional<File> jarFile = AlertHelper.getJarFile();

        if (!jarFile.isPresent()) {
            logger.error("Error when restarting application. Could not file jar file.");
            return;
        }
        if ("DEV".equals(System.getenv().get("PROGRAM-PROFILE"))) {
            jarFile = Optional.of(Paths.get(jarFile.get().getAbsolutePath().replaceFirst("classes", "Gipter.jar")).toFile());
        }

        if (!jarFile.get().exists() || !jarFile.get().isFile()) {
            logger.error("Error when restarting application. [{}] is not a file.", jarFile.get().getAbsolutePath());
        }

        final LinkedList<String> command = Stream.of(
                javaBin, "-jar", jarFile.get().getPath(), ArgName.upgradeFinished.name() + "=" + Boolean.TRUE
        ).collect(Collectors.toCollection(LinkedList::new));

        new ProcessBuilder(command).start();

        System.exit(0);
    }

    @Override
    protected Void call() {
        upgradeAndRestartApplication();
        return null;
    }

    @Override
    public void run() {
        super.run();
    }
}
