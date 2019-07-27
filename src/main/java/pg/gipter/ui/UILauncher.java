package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Launcher;
import pg.gipter.service.GithubService;
import pg.gipter.service.StartupService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.ui.job.*;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.PropertiesHelper;
import pg.gipter.utils.StringUtils;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/** Created by Gawa 2017-10-04 */
public class UILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(UILauncher.class);

    private final Stage mainWindow;
    private ApplicationProperties applicationProperties;
    private Stage jobWindow;
    private Stage projectsWindow;
    private Stage toolkitProjectsWindow;
    private TrayHandler trayHandler;
    private PropertiesHelper propertiesHelper;
    private boolean silentMode;
    private boolean upgradeChecked = false;
    private Executor executor;
    private Executor uiThread;

    public UILauncher(Stage mainWindow, ApplicationProperties applicationProperties) {
        this.mainWindow = mainWindow;
        this.applicationProperties = applicationProperties;
        propertiesHelper = new PropertiesHelper();
        silentMode = applicationProperties.isSilentMode();
        this.executor = Executors.newFixedThreadPool(3);
        uiThread = Platform::runLater;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    boolean isSilentMode() {
        return silentMode;
    }

    void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public void initTrayHandler() {
        trayHandler = new TrayHandler(this, applicationProperties);
        if (trayHandler.tryIconExists()) {
            logger.info("Updating tray icon. Silent mode [{}].", silentMode);
            trayHandler.updateTrayLabels();
        } else {
            logger.info("Initializing tray icon. Silent mode [{}].", silentMode);
            trayHandler.createTrayIcon();
            scheduleJobIfExists();
            JobCreator.scheduleCheckUpgradeJob();
        }
    }

    @Override
    public void execute() {
        if (!isTraySupported() && silentMode) {
            logger.info("Tray icon is not supported. Can't launch in silent mode. Program is terminated");
            Platform.exit();
        }
        checkUpgrades();
        setStartOnStartup();
        logger.info("Launching UI. Silent mode: [{}].", silentMode);
        initTray();
        if (!silentMode) {
            buildAndShowMainWindow();
        }
    }

    private void checkUpgrades() {
        if (!upgradeChecked) {
            GithubService service = new GithubService(applicationProperties);
            executor.execute(service::checkUpgrades);
            upgradeChecked = true;
        }
    }

    private void setStartOnStartup() {
        logger.info("Checking if Gipter can be started on system startup.");
        if (!isTraySupported()) {
            logger.info("Tray not supported. Can not set start on startup.");
            return;
        }
        if (applicationProperties.isEnableOnStartup()) {
            new StartupService().startOnStartup();
        }
    }

    public void buildAndShowMainWindow() {
        buildScene(
                mainWindow,
                WindowFactory.MAIN.createWindow(applicationProperties, this)
        );
        showMainWindow();
    }

    void showMainWindow() {
        Platform.runLater(mainWindow::show);
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
            stage.sizeToScene();
        } catch (IOException ex) {
            logger.error("Building scene error.", ex);
        }
    }

    public Stage currentWindow() {
        return mainWindow;
    }

    private Image readImage(String imgPath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(imgPath)) {
            return new Image(is);
        }
    }

    public void changeLanguage(String language) {
        mainWindow.close();
        BundleUtils.changeBundle(language);
        execute();
    }

    public static void platformExit() {
        Platform.exit();
        System.exit(0);
    }

    public void showJobWindow() {
        Platform.runLater(() -> {
            Map<String, Properties> propertiesMap = propertiesHelper.loadAllApplicationProperties();
            if (propertiesMap.containsKey(ArgName.configurationName.defaultValue())) {
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.job.window.canNotOpen"))
                        .withWindowType(WindowType.OVERRIDE_WINDOW)
                        .withAlertType(Alert.AlertType.WARNING)
                        .withImage()
                        .buildAndDisplayWindow()
                );
            } else {
                jobWindow = new Stage();
                jobWindow.initModality(Modality.WINDOW_MODAL);
                buildScene(jobWindow, WindowFactory.JOB.createWindow(applicationProperties, this));
                jobWindow.showAndWait();
            }
        });
    }

    public void hideJobWindow() {
        jobWindow.close();
        jobWindow = null;
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

    public void removeTray() {
        trayHandler.removeTrayIcon();
    }

    public EventHandler<WindowEvent> trayOnCloseEventHandler() {
        return trayHandler.trayOnCloseEventHandler();
    }

    public void cancelJob() {
        try {
            if (JobCreator.isSchedulerInitiated()) {
                JobCreator.cancelUploadJob();
            }
        } catch (SchedulerException e) {
            String errorMessage = BundleUtils.getMsg("job.cancel.errMsg", JobCreator.schedulerClassName(), e.getMessage());
            logger.error(errorMessage);
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(errorMessage)
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.ERROR)
                    .withImage()
                    .buildAndDisplayWindow()
            );
        } finally {
            Optional<Properties> data = propertiesHelper.loadDataProperties();
            if (data.isPresent()) {
                data.ifPresent(properties -> Stream.of(JobProperty.values()).forEach(jobKey -> properties.remove(jobKey.value())));
                propertiesHelper.saveDataProperties(data.get());
            }

            logger.info("{} canceled.", UploadItemJob.NAME);
            updateTray();
        }
    }

    private void scheduleJobIfExists() {
        Optional<Properties> data = propertiesHelper.loadDataProperties();
        if (data.isPresent() && !JobCreator.isSchedulerInitiated() && data.get().containsKey(JobProperty.TYPE.value())) {
            try {
                JobType jobType = JobType.valueOf(data.get().getProperty(JobProperty.TYPE.value()));

                LocalDate scheduleStart = null;
                if (data.get().containsKey(JobProperty.SCHEDULE_START.value())) {
                    scheduleStart = LocalDate.parse(
                            data.get().getProperty(JobProperty.SCHEDULE_START.value()),
                            ApplicationProperties.yyyy_MM_dd
                    );
                }
                int dayOfMonth = 0;
                if (data.get().containsKey(JobProperty.DAY_OF_MONTH.value())) {
                    dayOfMonth = Integer.valueOf(data.get().getProperty(JobProperty.DAY_OF_MONTH.value()));
                }
                int hourOfDay = 0;
                int minuteOfHour = 0;
                if (data.get().containsKey(JobProperty.HOUR_OF_THE_DAY.value())) {
                    String hourOfDayString = data.get().getProperty(JobProperty.HOUR_OF_THE_DAY.value());
                    hourOfDay = Integer.valueOf(hourOfDayString.substring(0, hourOfDayString.lastIndexOf(":")));
                    minuteOfHour = Integer.valueOf(hourOfDayString.substring(hourOfDayString.lastIndexOf(":") + 1));
                }
                DayOfWeek dayOfWeek = null;
                if (data.get().containsKey(JobProperty.DAY_OF_WEEK.value())) {
                    dayOfWeek = DayOfWeek.valueOf(data.get().getProperty(JobProperty.DAY_OF_WEEK.value()));
                }
                String cronExpression = data.get().getProperty(JobProperty.CRON.value());
                String configs = data.get().getProperty(JobProperty.CONFIGS.value());

                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), this);

                new JobCreatorBuilder()
                        .withData(data.get())
                        .withJobType(jobType)
                        .withStartDateTime(scheduleStart)
                        .withDayOfMonth(dayOfMonth)
                        .withHourOfDay(hourOfDay)
                        .withMinuteOfHour(minuteOfHour)
                        .withDayOfWeek(dayOfWeek)
                        .withCronExpression(cronExpression)
                        .withConfigs(configs)
                        .createJobCreator()
                        .scheduleUploadJob(additionalJobParams);
            } catch (ParseException | SchedulerException e) {
                logger.warn("Can not restart the scheduler.", e);
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.job.errorMsg", e.getMessage()))
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage()
                        .buildAndDisplayWindow());
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
        } else {
            setMainOnCloseRequest(AbstractController.regularOnCloseEventHandler());
        }
    }

    void setMainOnCloseRequest(EventHandler<WindowEvent> onCloseEventHandler) {
        mainWindow.setOnCloseRequest(onCloseEventHandler);
    }

    void hideMainWindow() {
        mainWindow.hide();
    }

    public void showProjectsWindow() {
        Platform.runLater(() -> {
            projectsWindow = new Stage();
            projectsWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(projectsWindow, WindowFactory.PROJECTS.createWindow(applicationProperties, this));
            projectsWindow.setOnCloseRequest(event -> {
                hideProjectsWindow();
                execute();
            });
            projectsWindow.showAndWait();
        });
    }

    public void hideProjectsWindow() {
        projectsWindow.hide();
    }

    public String getConfigurationName() {
        return applicationProperties.configurationName();
    }

    public void showApplicationSettingsWindow() {
        createSettingsWindow(WindowFactory.APPLICATION_MENU);
    }

    private void createSettingsWindow(WindowFactory windowFactory) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        buildScene(stage, windowFactory.createWindow(applicationProperties, this));
        stage.setOnCloseRequest(event -> stage.close());
        stage.showAndWait();
    }

    public void showToolkitSettingsWindow() {
        createSettingsWindow(WindowFactory.TOOLKIT_MENU);
    }

    public void showToolkitProjectsWindow() {
        Platform.runLater(() -> {
            toolkitProjectsWindow = new Stage();
            toolkitProjectsWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(toolkitProjectsWindow, WindowFactory.TOOLKIT_PROJECTS.createWindow(applicationProperties, this));
            toolkitProjectsWindow.setOnCloseRequest(event -> {
                hideToolkitProjectsWindow();
                execute();
            });
            toolkitProjectsWindow.showAndWait();
        });
    }

    public void hideToolkitProjectsWindow() {
        toolkitProjectsWindow.hide();
    }

}
