package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Launcher;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.job.GipterJob;
import pg.gipter.ui.job.JobKey;
import pg.gipter.util.AlertHelper;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;
import pg.gipter.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Created by Gawa 2017-10-04
 */
public class UILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(UILauncher.class);

    private final Stage primaryStage;
    private ApplicationProperties applicationProperties;
    private Stage jobWindow;
    private TrayHandler trayHandler;
    private Scheduler scheduler;
    private PropertiesHelper propertiesHelper;

    public UILauncher(Stage primaryStage, ApplicationProperties applicationProperties) {
        this.primaryStage = primaryStage;
        this.applicationProperties = applicationProperties;
        propertiesHelper = new PropertiesHelper();
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void initTrayHandler() {
        trayHandler = new TrayHandler(this, applicationProperties);
        if (trayHandler.tryIconExists()) {
            trayHandler.updateTrayLabels();
        } else {
            trayHandler.createTrayIcon();
        }
    }

    @Override
    public void execute() {
        logger.info("Launching UI.");
        buildScene(
                primaryStage,
                WindowFactory.MAIN.createWindow(applicationProperties, this)
        );
        primaryStage.show();
    }

    private void buildScene(Stage stage, AbstractWindow window) {
        try {
            Image icon = readImage(window.windowImgFilePath());
            stage.getIcons().add(icon);
        } catch (IOException ex) {
            logger.warn("Problem with loading window icon: {}.", ex.getMessage());
        }
        try {
            stage.setTitle(BundleUtils.getMsg(
                    window.windowTitleBundle(), applicationProperties.version()
            ));
            stage.setResizable(window.resizable());
            Scene scene = new Scene(window.root());
            if (!StringUtils.nullOrEmpty(window.css())) {
                scene.getStylesheets().add(window.css());
            }
            stage.setScene(scene);
        } catch (IOException ex) {
            logger.error("Building scene error.", ex);
        }
    }

    public Stage currentWindow() {
        return primaryStage;
    }

    private Image readImage(String imgPath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(imgPath)) {
            return new Image(is);
        }
    }

    public void changeLanguage(String language) {
        primaryStage.close();
        BundleUtils.changeBundle(language);
        execute();
    }

    public static void platformExit() {
        Platform.exit();
        System.exit(0);
    }

    void showJobWindow() {
        jobWindow = new Stage();
        jobWindow.initModality(Modality.WINDOW_MODAL);
        buildScene(jobWindow, WindowFactory.JOB.createWindow(applicationProperties, this));
        jobWindow.showAndWait();
    }

    public void hideJobWindow() {
        jobWindow.close();
    }

    public void updateTray() {
        updateTray(applicationProperties);
    }

    public void updateTray(ApplicationProperties applicationProperties) {
        trayHandler.setApplicationProperties(applicationProperties);
        trayHandler.updateTrayLabels();
    }

    public void hideToTray() {
        trayHandler.hide();
    }

    public void removeTray() {
        trayHandler.removeTrayIcon();
    }

    public EventHandler<WindowEvent> trayOnCloseEventHandler() {
        return trayHandler.trayOnCloseEventHandler();
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    void cancelJob() {
        try {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            String errorMessage = String.format("Can not shutdown the job scheduler [%s]. Reason: %s",
                    scheduler.getClass().getName(), e.getMessage()
            );
            logger.error(errorMessage);
            AlertHelper.displayWindow(errorMessage, Alert.AlertType.ERROR);
        } finally {
            Optional<Properties> data = propertiesHelper.loadDataProperties();
            if (data.isPresent()) {
                data.ifPresent(properties -> Stream.of(JobKey.values()).forEach(jobKey -> properties.remove(jobKey.value())));
                propertiesHelper.saveDataProperties(data.get());
            }

            logger.info("{} canceled.", GipterJob.NAME);
            updateTray();
        }
    }
}
