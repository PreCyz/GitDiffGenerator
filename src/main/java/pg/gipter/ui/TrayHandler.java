package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.ui.job.GipterJob;
import pg.gipter.ui.job.JobKey;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

class TrayHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrayHandler.class);

    private final Stage stage;
    private ApplicationProperties applicationProperties;
    private UILauncher uiLauncher;
    private static TrayIcon trayIcon;
    private static PopupMenu trayPopupMenu;
    private PropertiesHelper propertiesHelper;

    TrayHandler(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        this.uiLauncher = uiLauncher;
        this.stage = uiLauncher.currentWindow();
        this.applicationProperties = applicationProperties;
        this.propertiesHelper = new PropertiesHelper();
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    void createTrayIcon() {
        if (canCreateTrayIcon()) {
            stage.setOnCloseRequest(trayOnCloseEventHandler());

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
        } else {
            stage.setOnCloseRequest(AbstractController.regularOnCloseEventHandler());
        }
    }

    boolean canCreateTrayIcon() {
        return SystemTray.isSupported() && applicationProperties.isUseUI() && applicationProperties.isActiveTray();
    }

    private void addMenuItemsToMenu(PopupMenu popupMenu) {
        propertiesHelper = new PropertiesHelper();
        Optional<Properties> data = propertiesHelper.loadDataProperties();
        if (data.isPresent()) {

            boolean addSeparator = false;

            if (data.get().containsKey(PropertiesHelper.UPLOAD_DATE_TIME_KEY) && data.get().containsKey(PropertiesHelper.UPLOAD_STATUS_KEY)) {
                String uploadInfo = String.format("%s [%s]",
                        data.get().getProperty(PropertiesHelper.UPLOAD_DATE_TIME_KEY),
                        data.get().getProperty(PropertiesHelper.UPLOAD_STATUS_KEY)
                );
                popupMenu.add(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
                addSeparator = true;
            }

            if (data.get().containsKey(JobKey.TYPE.value())) {
                Menu jobMenu = new Menu(
                        String.format("%s %s", GipterJob.NAME, data.get().getProperty(JobKey.TYPE.value()))
                );
                buildMenuItem(data.get(), JobKey.DAY_OF_WEEK).ifPresent(jobMenu::add);
                buildMenuItem(data.get(), JobKey.HOUR_OF_THE_DAY).ifPresent(jobMenu::add);
                buildMenuItem(data.get(), JobKey.DAY_OF_MONTH).ifPresent(jobMenu::add);
                buildMenuItem(data.get(), JobKey.SCHEDULE_START).ifPresent(jobMenu::add);
                buildMenuItem(data.get(), JobKey.CRON).ifPresent(jobMenu::add);
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

        MenuItem uploadItem = new MenuItem(
                BundleUtils.getMsg("tray.item.upload", String.valueOf(applicationProperties.periodInDays()))
        );
        uploadItem.addActionListener(uploadActionListener());
        popupMenu.add(uploadItem);

        MenuItem createJobItem = new MenuItem(BundleUtils.getMsg("tray.item.createJob"));
        createJobItem.addActionListener(createJobActionListener());
        popupMenu.add(createJobItem);
        popupMenu.addSeparator();

        MenuItem closeItem = new MenuItem(BundleUtils.getMsg("tray.item.close"));
        closeItem.addActionListener(closeActionListener());
        popupMenu.add(closeItem);
    }

    private Optional<String> buildMenuItem(Properties data, JobKey key) {
        if (data.containsKey(key.value())) {
            return Optional.of(String.format("%s: %s", key, data.getProperty(key.value())));
        }
        return Optional.empty();
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
        return windowEvent -> hide();
    }

    private ActionListener showActionListener() {
        return e -> Platform.runLater(stage::show);
    }

    private ActionListener closeActionListener() {
        return e -> UILauncher.platformExit();
    }

    private ActionListener uploadActionListener() {
        return e -> Platform.runLater(() -> {
            LocalDate startDate = LocalDate.now().minusDays(applicationProperties.periodInDays());
            LocalDate endDate = LocalDate.now();

            String[] args = {
                    String.format("%s=%s", ArgName.startDate, startDate.format(ApplicationProperties.yyyy_MM_dd)),
                    String.format("%s=%s", ArgName.endDate, endDate.format(ApplicationProperties.yyyy_MM_dd)),
                    String.format("%s=%s", ArgName.preferredArgSource, PreferredArgSource.UI),
            };

            ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(args);
            setApplicationProperties(uiAppProperties);
            new FXRunner(uiAppProperties).start();
        });
    }

    private ActionListener cancelJobActionListener() {
        return e -> uiLauncher.cancelJob();
    }

    void hide() {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
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
