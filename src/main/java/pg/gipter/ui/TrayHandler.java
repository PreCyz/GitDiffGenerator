package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.configuration.CacheManager;
import pg.gipter.configuration.ConfigurationDao;
import pg.gipter.dao.DaoConstants;
import pg.gipter.dao.DaoFactory;
import pg.gipter.data.DataDao;
import pg.gipter.job.JobHandler;
import pg.gipter.job.upload.JobProperty;
import pg.gipter.job.upload.UploadItemJob;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.job.JobController;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;

class TrayHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrayHandler.class);

    private ApplicationProperties applicationProperties;
    private UILauncher uiLauncher;
    private static TrayIcon trayIcon;
    private static PopupMenu trayPopupMenu;
    private ConfigurationDao propertiesDao;
    private DataDao dataDao;
    private Executor executor;

    TrayHandler(UILauncher uiLauncher, ApplicationProperties applicationProperties, Executor executor) {
        this.uiLauncher = uiLauncher;
        this.applicationProperties = applicationProperties;
        this.executor = executor;
        this.propertiesDao = DaoFactory.getConfigurationDao();
        this.dataDao = DaoFactory.getDataDao();
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    void createTrayIcon() {
        uiLauncher.setMainOnCloseRequest(trayOnCloseEventHandler());

        Platform.setImplicitExit(false);
        SystemTray tray = SystemTray.getSystemTray();

        trayPopupMenu = new PopupMenu();
        addMenuItemsToMenu(trayPopupMenu);

        trayIcon = new TrayIcon(
                createTrayImage(), BundleUtils.getMsg("main.title", applicationProperties.version()), trayPopupMenu
        );
        trayIcon.addActionListener(showActionListener());
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            logger.error("Error when creating tray.", e);
        }
    }

    boolean canCreateTrayIcon() {
        return SystemTray.isSupported() && applicationProperties.isUseUI() && applicationProperties.isActiveTray();
    }

    private void addMenuItemsToMenu(PopupMenu popupMenu) {
        executor.execute(() -> {
            JobHandler jobHandler = uiLauncher.getJobHandler();
            Optional<Properties> data = dataDao.loadDataProperties();
            if (data.isPresent()) {

                boolean addSeparator = false;

                if (data.get().containsKey(DaoConstants.UPLOAD_DATE_TIME_KEY) && data.get().containsKey(DaoConstants.UPLOAD_STATUS_KEY)) {
                    String uploadInfo = String.format("%s [%s]",
                            data.get().getProperty(DaoConstants.UPLOAD_DATE_TIME_KEY),
                            data.get().getProperty(DaoConstants.UPLOAD_STATUS_KEY)
                    );
                    popupMenu.add(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
                    addSeparator = true;
                }
                if (data.get().containsKey(JobProperty.NEXT_FIRE_DATE.key()) &&
                        !StringUtils.nullOrEmpty(data.get().getProperty(JobProperty.NEXT_FIRE_DATE.key(), ""))) {
                    popupMenu.add(BundleUtils.getMsg(
                            "tray.item.nextUpdate",
                            data.get().getProperty(JobProperty.NEXT_FIRE_DATE.key())
                    ));
                    addSeparator = true;
                }

                if (data.get().containsKey(JobProperty.TYPE.key())) {
                    Menu jobMenu = new Menu(
                            String.format("%s %s", UploadItemJob.NAME, data.get().getProperty(JobProperty.TYPE.key()))
                    );
                    JobController.buildLabel(data.get(), JobProperty.DAY_OF_WEEK).ifPresent(jobMenu::add);
                    JobController.buildLabel(data.get(), JobProperty.HOUR_OF_THE_DAY).ifPresent(jobMenu::add);
                    JobController.buildLabel(data.get(), JobProperty.DAY_OF_MONTH).ifPresent(jobMenu::add);
                    JobController.buildLabel(data.get(), JobProperty.SCHEDULE_START).ifPresent(jobMenu::add);
                    JobController.buildLabel(data.get(), JobProperty.CRON).ifPresent(jobMenu::add);
                    JobController.buildLabel(data.get(), JobProperty.CONFIGS).ifPresent(jobMenu::add);
                    jobMenu.addSeparator();

                    MenuItem cancelJobItem = new MenuItem(BundleUtils.getMsg("job.cancel"));
                    cancelJobItem.addActionListener(cancelJobActionListener());
                    jobMenu.add(cancelJobItem);

                    popupMenu.add(jobMenu);
                    addSeparator = true;
                }
                if (addSeparator) {
                    popupMenu.addSeparator();
                }
            }

            MenuItem showItem = new MenuItem(BundleUtils.getMsg("tray.item.show"));
            showItem.addActionListener(showActionListener());
            popupMenu.add(showItem);

            MenuItem uploadItem = new MenuItem(BundleUtils.getMsg("tray.item.upload"));
            uploadItem.addActionListener(uploadActionListener());
            popupMenu.add(uploadItem);

            MenuItem createJobItem = new MenuItem(BundleUtils.getMsg("tray.item.createJob"));
            createJobItem.addActionListener(createJobActionListener());
            popupMenu.add(createJobItem);

            MenuItem upgradeItem = new MenuItem(BundleUtils.getMsg(jobHandler.isUpgradeJobExists() ? "tray.item.upgradeJobDisable" : "tray.item.upgradeJobEnable"));
            upgradeItem.addActionListener(upgradeJobActionListener());
            popupMenu.add(upgradeItem);
            popupMenu.addSeparator();

            MenuItem closeItem = new MenuItem(BundleUtils.getMsg("tray.item.close"));
            closeItem.addActionListener(closeActionListener());
            popupMenu.add(closeItem);
        });
    }

    private ActionListener upgradeJobActionListener() {
        return e -> {
            JobHandler jobHandler = uiLauncher.getJobHandler();
            if (jobHandler.isUpgradeJobExists()) {
                jobHandler.cancelUpgradeJob();
            } else {
                jobHandler.scheduleUpgradeJob();
            }
            updateTrayLabels();
        };
    }

    private Image createTrayImage() {
        String path = "img/chicken-tray.gif";
        URL imageURL = getClass().getClassLoader().getResource(path);
        if (imageURL == null) {
            logger.error("Resource not found: {}", path);
            return null;
        } else {
            String description = "Tray icon";
            return new ImageIcon(imageURL, description).getImage();
        }
    }

    private ActionListener createJobActionListener() {
        return e -> Platform.runLater(() -> uiLauncher.showJobWindow());
    }

    EventHandler<WindowEvent> trayOnCloseEventHandler() {
        return windowEvent -> {
            hide();
            CacheManager.clearAllCache();
        };
    }

    private ActionListener showActionListener() {
        return e -> Platform.runLater(() -> {
            if (uiLauncher.isSilentMode()) {
                uiLauncher.setSilentMode(false);
            }
            if (!StringUtils.nullOrEmpty(applicationProperties.configurationName())) {
                applicationProperties = CacheManager.getApplicationProperties(applicationProperties.configurationName());
                uiLauncher.setApplicationProperties(applicationProperties);
            }
            uiLauncher.buildAndShowMainWindow();
        });
    }

    private ActionListener closeActionListener() {
        return e -> UILauncher.platformExit();
    }

    private ActionListener uploadActionListener() {
        return e -> executor.execute(() ->
                new FXMultiRunner(propertiesDao.loadAllConfigs().keySet(), executor, RunType.TRAY).start()
        );
    }

    private ActionListener cancelJobActionListener() {
        return e -> uiLauncher.cancelJob();
    }

    private void hide() {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                uiLauncher.hideMainWindow();
            } else {
                UILauncher.platformExit();
            }
        });
    }

    void removeTrayIcon() {
        if (canCreateTrayIcon()) {
            SystemTray tray = SystemTray.getSystemTray();
            boolean exist = false;
            for (TrayIcon icon : tray.getTrayIcons()) {
                if (icon.getImage().equals(trayIcon.getImage()) && icon.getToolTip().equals(trayIcon.getToolTip())) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                tray.remove(trayIcon);
                trayIcon = null;
            }
        }
    }

    void updateTrayLabels() {
        if (canCreateTrayIcon()) {
            trayPopupMenu.removeAll();
            addMenuItemsToMenu(trayPopupMenu);
        }
    }

    boolean tryIconExists() {
        return trayIcon != null;
    }
}
