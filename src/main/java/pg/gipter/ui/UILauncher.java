package pg.gipter.ui;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
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
import pg.gipter.configuration.ConfigurationDao;
import pg.gipter.dao.DaoFactory;
import pg.gipter.data.DataDao;
import pg.gipter.job.JobHandler;
import pg.gipter.job.upload.JobProperty;
import pg.gipter.job.upload.JobType;
import pg.gipter.job.upload.UploadItemJob;
import pg.gipter.job.upload.UploadItemJobBuilder;
import pg.gipter.launcher.Launcher;
import pg.gipter.service.GithubService;
import pg.gipter.service.StartupService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.PropertiesUtils;
import pg.gipter.utils.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Stage applicationSettingsWindow;
    private Stage upgradeWindow;
    private Stage toolkitSettingsWindow;
    private TrayHandler trayHandler;
    private ConfigurationDao propertiesDao;
    private DataDao dataDao;
    private boolean silentMode;
    private boolean upgradeChecked = false;
    private LocalDateTime lastItemSubmissionDate;
    private Executor executor;
    private JobHandler jobHandler;
    private Properties wizardProperties;

    public UILauncher(Stage mainWindow, ApplicationProperties applicationProperties) {
        this.mainWindow = mainWindow;
        this.applicationProperties = applicationProperties;
        propertiesDao = DaoFactory.getConfigurationDao();
        dataDao = DaoFactory.getDataDao();
        silentMode = applicationProperties.isSilentMode();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        jobHandler = new JobHandler();
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

    public LocalDateTime getLastItemSubmissionDate() {
        return lastItemSubmissionDate;
    }

    public void setLastItemSubmissionDate(LocalDateTime lastItemSubmissionDate) {
        this.lastItemSubmissionDate = lastItemSubmissionDate;
    }

    public void addPropertyToWizard(String key, String value) {
        wizardProperties.put(key, value);
        StringProperty projectLabelProperty = (StringProperty) wizardProperties.get(WizardLauncher.projectLabelPropertyName);
        projectLabelProperty.setValue(StringUtils.trimTo50(value));
        wizardProperties.put(WizardLauncher.projectLabelPropertyName, projectLabelProperty);
    }

    public void executeOutsideUIThread(Runnable runnable) {
        executor.execute(runnable);
    }

    public Executor nonUIExecutor() {
        return executor;
    }

    public void initTrayHandler() {
        trayHandler = new TrayHandler(this, applicationProperties, executor);
        if (trayHandler.tryIconExists()) {
            logger.info("Updating tray icon. Silent mode [{}].", silentMode);
            trayHandler.updateTrayLabels();
        } else {
            logger.info("Initializing tray icon. Silent mode [{}].", silentMode);
            trayHandler.createTrayIcon();
        }
    }

    @Override
    public void execute() {
        if (!isTraySupported() && silentMode) {
            logger.info("Tray icon is not supported. Can't launch in silent mode. Program is terminated");
            Platform.exit();
        }
        if (applicationProperties.isUpgradeFinished()) {
            displayUpgradeInfo();
        } else {
            checkUpgrades();
        }
        setStartOnStartup();
        scheduleJobs();
        logger.info("Launching UI. Silent mode: [{}].", silentMode);
        initTray();
        if (!silentMode) {
            buildAndShowMainWindow();
        }
    }

    private void displayUpgradeInfo() {
        logger.info("Upgrade to version {} finished [{}].", applicationProperties.version(), applicationProperties.isUpgradeFinished());
        new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("popup.no.upgrade.message"))
                .withWindowType(WindowType.CONFIRMATION_WINDOW)
                .withAlertType(Alert.AlertType.INFORMATION)
                .withImage(ImageFile.MINION_AAAA_GIF)
                .buildAndDisplayWindow();
    }

    private void checkUpgrades() {
        if (!upgradeChecked) {
            executor.execute(() -> {
                GithubService service = new GithubService(applicationProperties.version());
                if (service.isNewVersion()) {
                    logger.info("New version available: {}.", service.getStrippedVersion());
                    Platform.runLater(() -> new AlertWindowBuilder()
                            .withHeaderText(BundleUtils.getMsg("popup.upgrade.message", service.getStrippedVersion()))
                            .withLink(GithubService.GITHUB_URL + "/releases/latest")
                            .withWindowType(WindowType.BROWSER_WINDOW)
                            .withAlertType(Alert.AlertType.INFORMATION)
                            .withImage(ImageFile.MINION_AAAA_2_GIF)
                            .buildAndDisplayWindow()
                    );
                }
            });
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

    private void scheduleJobs() {
        scheduleUploadJob();
        jobHandler.scheduleUpgradeJob();
        jobHandler.executeUploadJobIfMissed(executor);
    }

    public void buildAndShowMainWindow() {
        buildScene(
                mainWindow,
                WindowFactory.MAIN.createWindow(applicationProperties, this)
        );
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
        applicationSettingsWindow.close();
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
            Map<String, Properties> propertiesMap = propertiesDao.loadAllConfigs();
            if (propertiesMap.containsKey(ArgName.configurationName.defaultValue())) {
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.job.window.canNotOpen"))
                        .withWindowType(WindowType.OVERRIDE_WINDOW)
                        .withAlertType(Alert.AlertType.WARNING)
                        .withImage(ImageFile.OVERRIDE_PNG);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
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
            if (jobHandler.isSchedulerInitiated()) {
                jobHandler.cancelUploadJob();
            }
        } catch (SchedulerException e) {
            String errorMessage = BundleUtils.getMsg("job.cancel.errMsg", jobHandler.schedulerClassName(), e.getMessage());
            logger.error(errorMessage);
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(errorMessage)
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.ERROR)
                    .withImage(ImageFile.ERROR_CHICKEN_PNG);
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        } finally {
            Optional<Properties> data = dataDao.loadDataProperties();
            if (data.isPresent()) {
                data.ifPresent(properties -> Stream.of(JobProperty.values()).forEach(jobKey -> properties.remove(jobKey.key())));
                dataDao.saveDataProperties(data.get());
            }

            logger.info("{} canceled.", UploadItemJob.NAME);
            updateTray();
        }
    }

    private void scheduleUploadJob() {
        Optional<Properties> data = dataDao.loadDataProperties();
        if (data.isPresent() && !jobHandler.isSchedulerInitiated() && data.get().containsKey(JobProperty.TYPE.key())) {
            logger.info("Setting up the job.");
            try {
                JobType jobType = JobType.valueOf(data.get().getProperty(JobProperty.TYPE.key()));

                LocalDate scheduleStart = null;
                if (data.get().containsKey(JobProperty.SCHEDULE_START.key())) {
                    scheduleStart = LocalDate.parse(
                            data.get().getProperty(JobProperty.SCHEDULE_START.key()),
                            ApplicationProperties.yyyy_MM_dd
                    );
                }
                int dayOfMonth = 0;
                if (data.get().containsKey(JobProperty.DAY_OF_MONTH.key())) {
                    dayOfMonth = Integer.parseInt(data.get().getProperty(JobProperty.DAY_OF_MONTH.key()));
                }
                int hourOfDay = 0;
                int minuteOfHour = 0;
                if (data.get().containsKey(JobProperty.HOUR_OF_THE_DAY.key())) {
                    String hourOfDayString = data.get().getProperty(JobProperty.HOUR_OF_THE_DAY.key());
                    hourOfDay = Integer.parseInt(hourOfDayString.substring(0, hourOfDayString.lastIndexOf(":")));
                    minuteOfHour = Integer.parseInt(hourOfDayString.substring(hourOfDayString.lastIndexOf(":") + 1));
                }
                DayOfWeek dayOfWeek = null;
                if (data.get().containsKey(JobProperty.DAY_OF_WEEK.key())) {
                    dayOfWeek = DayOfWeek.valueOf(data.get().getProperty(JobProperty.DAY_OF_WEEK.key()));
                }
                String cronExpression = data.get().getProperty(JobProperty.CRON.key());
                String configs = data.get().getProperty(JobProperty.CONFIGS.key());

                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), this);

                UploadItemJobBuilder builder = new UploadItemJobBuilder()
                        .withData(data.get())
                        .withJobType(jobType)
                        .withStartDateTime(scheduleStart)
                        .withDayOfMonth(dayOfMonth)
                        .withHourOfDay(hourOfDay)
                        .withMinuteOfHour(minuteOfHour)
                        .withDayOfWeek(dayOfWeek)
                        .withCronExpression(cronExpression)
                        .withConfigs(configs);
                jobHandler.scheduleUploadJob(builder, additionalJobParams);
                logger.info("Job set up successfully.");

            } catch (ParseException | SchedulerException e) {
                logger.warn("Can not restart the scheduler.", e);
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.job.errorMsg", e.getMessage()))
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage(ImageFile.ERROR_CHICKEN_PNG);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
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

    public void hideMainWindow() {
        mainWindow.hide();
    }

    public void showProjectsWindow(Properties wizardProperties) {
        this.wizardProperties = wizardProperties;
        Platform.runLater(() -> {
            projectsWindow = new Stage();
            projectsWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(projectsWindow, WindowFactory.PROJECTS.createWindow(applicationProperties, this));
            projectsWindow.setOnCloseRequest(event -> {
                hideProjectsWindow();
                if (isInvokeExecute()) {
                    execute();
                }
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
        applicationSettingsWindow = new Stage();
        applicationSettingsWindow.initModality(Modality.APPLICATION_MODAL);

        Optional<Properties> appConfigProperties = propertiesDao.loadAppSettings();
        if (appConfigProperties.isPresent()) {
            String[] args = PropertiesUtils.propertiesToArray(appConfigProperties.get());
            applicationProperties = ApplicationPropertiesFactory.getInstance(args);
        }

        buildScene(applicationSettingsWindow, WindowFactory.APPLICATION_MENU.createWindow(applicationProperties, this));
        applicationSettingsWindow.setOnCloseRequest(event -> closeWindow(applicationSettingsWindow));
        applicationSettingsWindow.showAndWait();
    }

    private void closeWindow(Stage stage) {
        if (StringUtils.nullOrEmpty(applicationProperties.configurationName())) {
            applicationProperties = ApplicationPropertiesFactory.getInstance(
                    PropertiesUtils.propertiesToArray(propertiesDao.loadToolkitCredentials())
            );
        } else {
            applicationProperties = ApplicationPropertiesFactory.getInstance(
                    propertiesDao.loadArgumentArray(applicationProperties.configurationName())
            );
        }
        stage.close();
        execute();
    }

    public void closeApplicationWindow() {
        closeWindow(applicationSettingsWindow);
    }

    public void closeToolkitWindow() {
        closeWindow(toolkitSettingsWindow);
    }

    public void showToolkitSettingsWindow() {
        toolkitSettingsWindow = new Stage();
        toolkitSettingsWindow.initModality(Modality.APPLICATION_MODAL);
        buildScene(toolkitSettingsWindow, WindowFactory.TOOLKIT_MENU.createWindow(applicationProperties, this));
        toolkitSettingsWindow.setOnCloseRequest(event -> closeWindow(toolkitSettingsWindow));
        toolkitSettingsWindow.showAndWait();
    }

    public void showToolkitProjectsWindow(Properties wizardProperties) {
        this.wizardProperties = wizardProperties;
        Platform.runLater(() -> {
            toolkitProjectsWindow = new Stage();
            toolkitProjectsWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(toolkitProjectsWindow, WindowFactory.TOOLKIT_PROJECTS.createWindow(applicationProperties, this));
            toolkitProjectsWindow.setOnCloseRequest(event -> {
                hideToolkitProjectsWindow();
                if (isInvokeExecute()) {
                    execute();
                }
            });
            toolkitProjectsWindow.showAndWait();
        });
    }

    public void hideToolkitProjectsWindow() {
        toolkitProjectsWindow.hide();
    }

    public JobHandler getJobHandler() {
        return jobHandler;
    }

    public void showUpgradeWindow() {
        upgradeWindow = new Stage();
        upgradeWindow.initModality(Modality.APPLICATION_MODAL);
        buildScene(upgradeWindow, WindowFactory.UPGRADE.createWindow(applicationProperties, this));
        upgradeWindow.setOnCloseRequest(event -> {
            upgradeWindow.close();
            System.exit(0);
        });
        upgradeWindow.showAndWait();
    }

    public void hideUpgradeWindow() {
        upgradeWindow.close();
    }

    public boolean isInvokeExecute() {
        return wizardProperties == null || wizardProperties.isEmpty();
    }
}
