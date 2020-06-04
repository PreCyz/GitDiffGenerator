package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.jobs.JobHandler;
import pg.gipter.jobs.upload.*;
import pg.gipter.ui.job.JobController;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;

class TrayHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrayHandler.class);

    private ApplicationProperties applicationProperties;
    private final UILauncher uiLauncher;
    private static TrayIcon trayIcon;
    private static PopupMenu trayPopupMenu;
    private final DataDao dataDao;
    private final Executor executor;

    TrayHandler(UILauncher uiLauncher, ApplicationProperties applicationProperties, Executor executor) {
        this.uiLauncher = uiLauncher;
        this.applicationProperties = applicationProperties;
        this.executor = executor;
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
                createTrayImage(),
                BundleUtils.getMsg("main.title", applicationProperties.version().getVersion()),
                trayPopupMenu
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
            ProgramData programData = dataDao.readProgramData();
            if (programData.getJobParam() != null) {
                JobParam jobParam = programData.getJobParam();

                boolean addSeparator = false;

                if (programData.getLastUploadDateTime() != null && programData.getUploadStatus() != null) {
                    String uploadInfo = String.format("%s [%s]",
                            programData.getLastUploadDateTime().format(DateTimeFormatter.ISO_DATE_TIME),
                            programData.getUploadStatus()
                    );
                    popupMenu.add(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
                    addSeparator = true;
                }
                if (jobParam.getNextFireDate() != null) {
                    popupMenu.add(BundleUtils.getMsg(
                            "tray.item.nextUpdate",
                            jobParam.getNextFireDate().format(DateTimeFormatter.ISO_DATE_TIME)
                    ));
                    addSeparator = true;
                }

                if (jobParam.getJobType() != null) {
                    Menu jobMenu = new Menu(
                            String.format("%s %s", UploadItemJob.NAME, jobParam.getJobType())
                    );
                    JobController.buildLabel(jobParam.getDayOfWeek().name(), JobProperty.DAY_OF_WEEK)
                            .ifPresent(jobMenu::add);
                    JobController.buildLabel(String.valueOf(jobParam.getHourOfDay()), JobProperty.HOUR_OF_THE_DAY)
                            .ifPresent(jobMenu::add);
                    JobController.buildLabel(String.valueOf(jobParam.getDayOfMonth()), JobProperty.DAY_OF_MONTH)
                            .ifPresent(jobMenu::add);
                    JobController.buildLabel(
                            jobParam.getScheduleStart().format(DateTimeFormatter.ISO_DATE_TIME), JobProperty.SCHEDULE_START)
                            .ifPresent(jobMenu::add);
                    JobController.buildLabel(jobParam.getCronExpression(), JobProperty.CRON).ifPresent(jobMenu::add);
                    JobController.buildLabel(jobParam.getConfigsStr(), JobProperty.CONFIGS).ifPresent(jobMenu::add);
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
                new FXMultiRunner(applicationProperties.getRunConfigMap().keySet(), executor, RunType.TRAY).start()
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
