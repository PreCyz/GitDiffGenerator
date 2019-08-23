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

public class UpgradeService {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeService.class);

    private GithubService githubService;

    public UpgradeService() {
        githubService = new GithubService(null);
    }

    public void upgradeAndRestartApplication() {
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
        try {
            Optional<String> homeDirectoryPath = AlertHelper.homeDirectoryPath();
            if (homeDirectoryPath.isPresent()) {
                Optional<String> fileName = githubService.downloadLatestDistribution(homeDirectoryPath.get());
                if (fileName.isPresent()) {
                    File sevenZFile = Paths.get(homeDirectoryPath.get(), fileName.get()).toFile();
                    decompress(sevenZFile, Paths.get(homeDirectoryPath.get()).toFile());
                    FileUtils.forceDelete(sevenZFile);
                    restartApplication();
                } else {
                    alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                            .withLink(AlertHelper.logsFolder())
                            .withWindowType(WindowType.LOG_WINDOW)
                            .withAlertType(Alert.AlertType.WARNING);
                }
            } else {
                alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.WARNING);
            }
        } catch (Exception ex) {
            alertWindowBuilder.withHeaderText(BundleUtils.getMsg("upgrade.fail"))
                    .withMessage(ex.getMessage())
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.WARNING);
        } finally {
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
    }

    private void decompress(File sevenZSourceFile, File destination) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(sevenZSourceFile)){
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null){
                if (entry.isDirectory()){
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
        }
    }

    private void restartApplication() throws IOException {
        final String javaBin = Paths.get(System.getProperty("java.home"),"bin", "java").toString();
        Optional<File> jarFile = AlertHelper.getJarFile();

        /* is it a jar file? */
        if (!jarFile.isPresent()) {
            logger.error("Error when restarting application. Could not file jar file.");
            return;
        }
        if (!jarFile.get().getName().endsWith(".jar")) {
            jarFile = Optional.of(Paths.get(jarFile.get().getAbsolutePath().replaceFirst("classes", "Gipter.jar")).toFile());
        }

        if (!jarFile.get().exists() || !jarFile.get().isFile()) {
            logger.error("Error when restarting application. [{}] is not a file.", jarFile.get().getAbsolutePath());
        }

        /* Build command: java -jar application.jar */
        final LinkedList<String> command = new LinkedList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(jarFile.get().getPath());
        command.add(ArgName.upgradeFinished.name() + "=" + Boolean.TRUE);

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }
}
