package pg.gipter.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;

import java.io.File;
import java.io.FileNotFoundException;
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
        updateMessage(BundleUtils.getMsg("upgrade.progress.started"));
        initProgress(githubService.getFileSize().orElse(0L));
        increaseProgress();
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
                    logger.error("Did not download the newest version.");
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
            updateMsg(BundleUtils.getMsg("upgrade.fail"));
            workCompleted();
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
    }

    private void decompress(File sevenZSourceFile, File destination) throws IOException {
        updateMsg(BundleUtils.getMsg("upgrade.progress.decompressing"));
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
                updateTaskProgress(Double.valueOf(5 * Math.pow(10, 5)).longValue());
            }
        } catch (IOException ex) {
            logger.error("What da hell ?", ex);
            throw ex;
        } finally {
            updateMsg(BundleUtils.getMsg("upgrade.progress.deleting"));
            forceDelete(sevenZSourceFile);
        }
    }

    private void forceDelete(File sevenZSourceFile) throws IOException {
        try {
            boolean filePresent = sevenZSourceFile.exists();
            if (!sevenZSourceFile.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + sevenZSourceFile);
                }
                throw new IOException("Unable to delete file: " + sevenZSourceFile);
            }
            logger.info("File [{}] deleted.", sevenZSourceFile.getName());
        } catch (IOException ex) {
            logger.error("Could not delete the [{}] file.", sevenZSourceFile.getName(), ex);
            throw ex;
        }
    }

    private void restartApplication() throws IOException {
        updateMsg(BundleUtils.getMsg("upgrade.progress.restarting"));
        final String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Optional<File> jarFile = AlertHelper.getJarFile();

        if (!jarFile.isPresent()) {
            workCompleted();
            logger.error("Error when restarting application. Could not file jar file.");
            return;
        }
        if ("DEV".equals(System.getenv().get("PROGRAM-PROFILE"))) {
            jarFile = Optional.of(Paths.get(jarFile.get().getAbsolutePath().replaceFirst("classes", "Gipter.jar")).toFile());
        }

        if (!jarFile.get().exists() || !jarFile.get().isFile()) {
            logger.error("Error when restarting application. [{}] is not a file.", jarFile.get().getAbsolutePath());
            workCompleted();
            return;
        }

        workCompleted();
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
}
