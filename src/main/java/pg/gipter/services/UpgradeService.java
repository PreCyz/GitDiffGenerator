package pg.gipter.services;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.FlowType;
import pg.gipter.core.ArgName;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.LogLinkAction;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.JarHelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class UpgradeService extends TaskService<Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeService.class);

    private final GithubService githubService;
    private final Executor executor;
    private final RestartService restartService;

    public UpgradeService(SemanticVersioning currentVersion, String githubToken, Executor executor) {
        super();
        this.executor = executor;
        githubService = new GithubService(currentVersion, githubToken);
        restartService = new RestartService();
    }

    void upgradeAndRestartApplication() {
        updateMessage(BundleUtils.getMsg("upgrade.progress.started"));
        initProgress(githubService.getFileSize().orElse(0L));
        increaseProgress();
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
        try {
            Optional<String> homeDirectoryPath = JarHelper.homeDirectoryPath();
            if (homeDirectoryPath.isPresent()) {
                Optional<String> fileName = githubService.downloadLatestDistribution(homeDirectoryPath.get(), this);
                if (fileName.isPresent()) {
                    File sevenZFile = Paths.get(homeDirectoryPath.get(), fileName.get()).toFile();
                    decompress(sevenZFile, Paths.get(homeDirectoryPath.get()).toFile());
                    updateMsg(BundleUtils.getMsg("upgrade.progress.restarting"));
                    final List<String> restartArguments = Stream.of(
                            String.format("%s=%b", ArgName.upgradeFinished.name(), Boolean.TRUE),
                            String.format("%s=%s", ArgName.flowType.name(), FlowType.REGULAR)
                    ).collect(toList());
                    restartService.start(restartArguments);
                    workCompleted();
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

    private void decompress(File sevenZSourceFile, File destination) throws IOException {
        updateMsg(BundleUtils.getMsg("upgrade.progress.decompressing"));
        try (SevenZFile sevenZFile = SevenZFile.builder()
                .setSeekableByteChannel(Files.newByteChannel(sevenZSourceFile.toPath(), EnumSet.of(StandardOpenOption.READ)))
                .setDefaultName(sevenZSourceFile.getName())
                .get()) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File currentFile = new File(destination, entry.getName());
                File parent = currentFile.getParentFile();
                if (!parent.exists()) {
                    final boolean mkdir = parent.mkdirs();
                    logger.info("Directory created [{}] [{}]", mkdir, parent.getAbsolutePath());
                }
                FileOutputStream out = new FileOutputStream(currentFile);
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
                updateTaskProgress(Double.valueOf(5 * Math.pow(10, 5)).longValue());
            }
        } catch (IOException ex) {
            logger.error("What da hell?", ex);
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

    @Override
    protected Void call() {
        upgradeAndRestartApplication();
        return null;
    }
}
