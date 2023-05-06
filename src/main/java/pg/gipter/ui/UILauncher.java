package pg.gipter.ui;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.*;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.*;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.jobs.*;
import pg.gipter.launchers.Launcher;
import pg.gipter.services.*;
import pg.gipter.ui.alerts.*;
import pg.gipter.ui.alerts.controls.ControlFactory;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

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
    private Stage sharePointConfigWindow;
    private TrayHandler trayHandler;
    private final ConfigurationDao configurationDao;
    private final DataDao dataDao;
    private boolean silentMode;
    private boolean upgradeChecked = false;
    private LocalDateTime lastItemSubmissionDate;
    private final Executor executor;
    private final JobService jobService;
    private Properties wizardProperties;

    public UILauncher(Stage mainWindow, ApplicationProperties applicationProperties) {
        this.mainWindow = mainWindow;
        this.applicationProperties = applicationProperties;
        configurationDao = DaoFactory.getCachedConfiguration();
        dataDao = DaoFactory.getDataDao();
        silentMode = applicationProperties.isSilentMode();
        this.executor = ConcurrentService.getInstance().executor();
        jobService = new JobService();
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

    public void addPropertyToWizard(String key, Collection<SharePointConfig> value) {
        wizardProperties.put(key, new LinkedHashSet<>(value));
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
        logger.info("Upgrade to version {} finished [{}].", applicationProperties.version().getVersion(), applicationProperties.isUpgradeFinished());
        new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("popup.no.upgrade.message"))
                .withAlertType(Alert.AlertType.INFORMATION)
                .withWebViewDetails(WebViewService.getInstance().pullSuccessWebView())
                .buildAndDisplayWindow();
    }

    private void checkUpgrades() {
        if (!upgradeChecked) {
            executor.execute(() -> {
                GithubService service = new GithubService(applicationProperties.version());
                if (service.isNewVersion()) {
                    logger.info("New version available: {}.", service.getServerVersion());
                    Platform.runLater(() -> new AlertWindowBuilder()
                            .withHeaderText(BundleUtils.getMsg("popup.upgrade.message", service.getServerVersion()))
                            .withLinkAction(new BrowserLinkAction(
                                    GithubService.GITHUB_URL + "/releases/latest",
                                    BundleUtils.getMsg("upgrade.readReleaseNotes")
                            ))
                            .withAlertType(Alert.AlertType.INFORMATION)
                            .withCustomControl(ControlFactory.createUpgradeButton(this))
                            .withWebViewDetails(WebViewService.getInstance().pullSuccessWebView())
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
        scheduleUpgradeJob();
        jobService.executeUploadJobIfMissed(executor);
        if (applicationProperties.isCheckLastItemEnabled()) {
            scheduleCheckLastItemJob();
        }
    }

    private void scheduleUpgradeJob() {
        try {
            jobService.scheduleJob(JobCreatorFactory.upgradeJobCreator());
        } catch (SchedulerException e) {
            logger.error("Can not schedule the upgrade job.");
        }
    }

    private void scheduleCheckLastItemJob() {
        try {
            jobService.scheduleJob(JobCreatorFactory.lastItemJobCreator(applicationProperties));
        } catch (SchedulerException e) {
            logger.error("Can not schedule the last item job.");
        }
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
                    window.windowTitleBundle(), applicationProperties.version().getVersion()
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
            new AlertWindowBuilder()
                    .withAlertType(Alert.AlertType.ERROR)
                    .withHeaderText(String.format("I can't show you the %s!", window.getClass().getSimpleName()))
                    .withLinkAction(new LogLinkAction())
                    .withMessage(ex.getCause().getMessage())
                    .withImageFile(ImageFile.ERROR_CHICKEN_PNG)
                    .buildAndDisplayWindow();
            System.exit(-1);
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

    public static void platformExit() {
        Platform.exit();
        System.exit(0);
    }

    public void showJobWindow() {
        Platform.runLater(() -> {
            Map<String, RunConfig> runConfigMap = configurationDao.loadRunConfigMap();
            if (runConfigMap.containsKey(ArgName.configurationName.defaultValue())) {
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.job.window.canNotOpen"))
                        .withAlertType(Alert.AlertType.WARNING)
                        .withImageFile(ImageFile.OVERRIDE_PNG);
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
            if (jobService.isSchedulerInitiated()) {
                final Optional<JobParam> jobParamOpt = dataDao.loadJobParam();
                JobParam jobParam = jobParamOpt.get();
                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), this);

                UploadItemJobBuilder builder = new UploadItemJobBuilder()
                        .withJobType(jobParam.getJobType())
                        .withStartDate(jobParam.getScheduleStart())
                        .withDayOfMonth(jobParam.getDayOfMonth())
                        .withHourOfDay(jobParam.getHourOfDay())
                        .withMinuteOfHour(jobParam.getMinuteOfHour())
                        .withDayOfWeek(jobParam.getDayOfWeek())
                        .withCronExpression(jobParam.getCronExpression())
                        .withConfigs(String.join(",", jobParam.getConfigs()))
                        .withAdditionalParams(additionalJobParams);
                jobService.deleteJob(builder.createJobCreator());
            }
        } catch (SchedulerException e) {
            String errorMessage = BundleUtils.getMsg("job.cancel.errMsg", jobService.schedulerClassName(), e.getMessage());
            logger.error(errorMessage);
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withMessage(errorMessage)
                    .withLinkAction(new LogLinkAction())
                    .withAlertType(Alert.AlertType.ERROR)
                    .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        } finally {
            dataDao.removeJobParam();

            logger.info("{} canceled.", UploadItemJob.NAME);
            updateTray();
        }
    }

    private void scheduleUploadJob() {
        final Optional<JobParam> jobParamOpt = dataDao.loadJobParam();
        if (!jobService.isSchedulerInitiated() && jobParamOpt.isPresent() && jobParamOpt.get().getJobType() != null) {
            JobParam jobParam = jobParamOpt.get();
            logger.info("Setting up the job.");
            try {
                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), this);

                UploadItemJobBuilder builder = new UploadItemJobBuilder()
                        .withJobType(jobParam.getJobType())
                        .withStartDate(jobParam.getScheduleStart())
                        .withDayOfMonth(jobParam.getDayOfMonth())
                        .withHourOfDay(jobParam.getHourOfDay())
                        .withMinuteOfHour(jobParam.getMinuteOfHour())
                        .withDayOfWeek(jobParam.getDayOfWeek())
                        .withCronExpression(jobParam.getCronExpression())
                        .withConfigs(String.join(",", jobParam.getConfigs()))
                        .withAdditionalParams(additionalJobParams);
                jobService.scheduleJob(builder.createJobCreator());
                logger.info("Job set up successfully.");

            } catch (SchedulerException e) {
                logger.warn("Can not restart the scheduler.", e);
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withMessage(BundleUtils.getMsg("popup.job.errorMsg", e.getMessage()))
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.ERROR)
                        .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
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

    private void showProjectsWindow(Properties wizardProperties) {
        this.wizardProperties = wizardProperties;
        Platform.runLater(() -> {
            projectsWindow = new Stage();
            projectsWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(projectsWindow, WindowFactory.PROJECTS.createWindow(applicationProperties, this));
            projectsWindow.setOnCloseRequest(event -> {
                hideProjectsWindow();
                if (hasNoWizardProperties()) {
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
        mainWindow.close();

        applicationSettingsWindow = new Stage();
        applicationSettingsWindow.initModality(Modality.APPLICATION_MODAL);
        AbstractWindow window = WindowFactory.APPLICATION_MENU.createWindow(applicationProperties, this);
        buildScene(applicationSettingsWindow, window);
        applicationSettingsWindow.setOnCloseRequest(event -> {
            window.getController().executeBeforeClose();
            closeWindow(applicationSettingsWindow);
        });
        applicationSettingsWindow.showAndWait();
    }

    private void closeWindow(Stage stage) {
        if (StringUtils.nullOrEmpty(applicationProperties.configurationName())) {
            applicationProperties = ApplicationPropertiesFactory.getInstance(
                    configurationDao.loadToolkitConfig().toArgumentArray()
            );
        } else {
            applicationProperties = ApplicationPropertiesFactory.getInstance(
                    configurationDao.loadArgumentArray(applicationProperties.configurationName())
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

    private void showToolkitProjectsWindow(Properties wizardProperties) {
        this.wizardProperties = wizardProperties;
        Platform.runLater(() -> {
            toolkitProjectsWindow = new Stage();
            toolkitProjectsWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(toolkitProjectsWindow, WindowFactory.TOOLKIT_PROJECTS.createWindow(applicationProperties, this));
            toolkitProjectsWindow.setOnCloseRequest(event -> {
                hideToolkitProjectsWindow();
                if (hasNoWizardProperties()) {
                    execute();
                }
            });
            toolkitProjectsWindow.showAndWait();
        });
    }

    public void hideToolkitProjectsWindow() {
        toolkitProjectsWindow.hide();
    }

    public JobService getJobService() {
        return jobService;
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

    private boolean hasNoWizardProperties() {
        return !hasWizardProperties();
    }

    public boolean hasWizardProperties() {
        return wizardProperties != null && !wizardProperties.isEmpty();
    }

    private void showSharePointProjectWindow(Properties wizardProperties) {
        this.wizardProperties = wizardProperties;
        Platform.runLater(() -> {
            sharePointConfigWindow = new Stage();
            sharePointConfigWindow.initModality(Modality.APPLICATION_MODAL);
            buildScene(sharePointConfigWindow, WindowFactory.SHARE_POINT_PROJECTS.createWindow(applicationProperties, this));
            sharePointConfigWindow.setOnCloseRequest(event -> {
                hideSharePointConfigWindow();
                if (hasNoWizardProperties()) {
                    execute();
                }
            });
            sharePointConfigWindow.showAndWait();
        });
    }

    private void hideSharePointConfigWindow() {
        sharePointConfigWindow.close();
        sharePointConfigWindow = null;
    }

    public void showProject(ItemType itemType) {
        showProject(itemType, new Properties());
    }

    public void showProject(ItemType itemType, Properties properties) {
        switch (itemType) {
            case TOOLKIT_DOCS:
                showToolkitProjectsWindow(properties);
                break;
            case SHARE_POINT_DOCS:
                showSharePointProjectWindow(properties);
                break;
            default:
                showProjectsWindow(properties);
        }
    }

    public void changeApplicationSettingsWindowTitle() {
        final AbstractWindow window = WindowFactory.APPLICATION_MENU.createWindow(applicationProperties, this);
        applicationSettingsWindow.setTitle(BundleUtils.getMsg(
                window.windowTitleBundle(), applicationProperties.version().getVersion()
        ));
    }

    public void refreshApplicationSettingsWindow() {
        applicationSettingsWindow.close();
        showApplicationSettingsWindow();
    }
}
