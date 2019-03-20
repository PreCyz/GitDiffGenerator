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
import pg.gipter.ui.job.JobCreator;
import pg.gipter.ui.job.JobKey;
import pg.gipter.ui.job.JobType;
import pg.gipter.util.AlertHelper;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;
import pg.gipter.util.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
    private boolean silentMode;

    public UILauncher(Stage primaryStage, ApplicationProperties applicationProperties) {
        this.primaryStage = primaryStage;
        this.applicationProperties = applicationProperties;
        propertiesHelper = new PropertiesHelper();
        silentMode = applicationProperties.isSilentMode();
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    boolean isSilentMode() {
        return silentMode;
    }

    void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public void initTrayHandler() {
        trayHandler = new TrayHandler(this, applicationProperties);
        if (trayHandler.tryIconExists()) {
            logger.info("Updating tray icon.", silentMode);
            trayHandler.updateTrayLabels();
        } else {
            logger.info("Initializing tray icon.", silentMode);
            trayHandler.createTrayIcon();
            scheduleJobIfExists();
        }
    }

    @Override
    public void execute() {
        if (!isTraySupported() && silentMode) {
            logger.info("Tray icon is not supported. Can't launch in silent mode. Program is terminated");
            Platform.exit();
        }
        logger.info("Launching UI in silent mode: [{}].", silentMode);
        initTray();
        if (!silentMode) {
            buildAndShowMainWindow();
        }
    }

    void buildAndShowMainWindow() {
        buildScene(
                primaryStage,
                WindowFactory.MAIN.createWindow(applicationProperties, this)
        );
        showMainWindow();
    }

    void showMainWindow() {
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

    public void showJobWindow() {
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
        if (trayHandler != null && trayHandler.canCreateTrayIcon()) {
            trayHandler.setApplicationProperties(applicationProperties);
            trayHandler.updateTrayLabels();
        }
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

    public void cancelJob() {
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

    private void scheduleJobIfExists() {
        Optional<Properties> data = propertiesHelper.loadDataProperties();
        if (data.isPresent() && scheduler == null && data.get().containsKey(JobKey.TYPE.value())) {
            try {
                JobType jobType = JobType.valueOf(data.get().getProperty(JobKey.TYPE.value()));

                LocalDate scheduleStart = null;
                if (data.get().containsKey(JobKey.SCHEDULE_START.value())) {
                    scheduleStart = LocalDate.parse(
                            data.get().getProperty(JobKey.SCHEDULE_START.value()),
                            ApplicationProperties.yyyy_MM_dd
                    );
                }
                int dayOfMonth = 0;
                if (data.get().containsKey(JobKey.DAY_OF_MONTH.value())) {
                    dayOfMonth = Integer.valueOf(data.get().getProperty(JobKey.DAY_OF_MONTH.value()));
                }
                int hourOfDay = 0;
                int minuteOfHour = 0;
                if (data.get().containsKey(JobKey.HOUR_OF_THE_DAY.value())) {
                    String hourOfDayString = data.get().getProperty(JobKey.HOUR_OF_THE_DAY.value());
                    hourOfDay = Integer.valueOf(hourOfDayString.substring(0, hourOfDayString.lastIndexOf(":")));
                    minuteOfHour = Integer.valueOf(hourOfDayString.substring(hourOfDayString.lastIndexOf(":") + 1));
                }
                DayOfWeek dayOfWeek = null;
                if (data.get().containsKey(JobKey.DAY_OF_WEEK.value())) {
                    dayOfWeek = DayOfWeek.valueOf(data.get().getProperty(JobKey.DAY_OF_WEEK.value()));
                }
                String cronExpression = data.get().getProperty(JobKey.CRON.value());

                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), this);

                JobCreator jobCreator = new JobCreator(data.get(), jobType, scheduleStart, dayOfMonth,
                        hourOfDay, minuteOfHour, dayOfWeek, cronExpression, scheduler);

                scheduler = jobCreator.scheduleJob(additionalJobParams);
            } catch (ParseException | SchedulerException e) {
                logger.warn("Can not restart the scheduler.", e);
            }
        }
    }

    public boolean isTrayActivated() {
        return isTraySupported() && applicationProperties.isActiveTray();
    }

    public boolean isTraySupported() {
        return (trayHandler != null || SystemTray.isSupported()) && applicationProperties.isUseUI();
    }

    private void initTray() {
        if (isTrayActivated()) {
            initTrayHandler();
        }
    }

    void setMainOnCloseRequest(EventHandler<WindowEvent> trayOnCloseEventHandler) {
        primaryStage.setOnCloseRequest(trayOnCloseEventHandler);
    }

    void hideMainWindow() {
        primaryStage.hide();
    }
}
