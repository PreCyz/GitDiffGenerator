package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Runner;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

public class TrayHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrayHandler.class);

    private final Stage stage;
    private ApplicationProperties applicationProperties;
    private UILauncher uiLauncher;
    private static TrayIcon trayIcon;
    private static PopupMenu trayPopupMenu;
    private PropertiesHelper propertiesHelper;

    public TrayHandler(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        this.uiLauncher = uiLauncher;
        this.stage = uiLauncher.currentWindow();
        this.applicationProperties = applicationProperties;
        this.propertiesHelper = new PropertiesHelper();
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void createTrayIcon() {
        if (canCreateTrayIcon()) {
            stage.setOnCloseRequest(trayOnCloseEventHandler());

            Platform.setImplicitExit(false);
            SystemTray tray = SystemTray.getSystemTray();

            trayPopupMenu = new PopupMenu();
            addMenuItemsToMenu(trayPopupMenu);

            trayIcon = new TrayIcon(createTrayImage(), BundleUtils.getMsg("main.title", applicationProperties.version()), trayPopupMenu);
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

    private boolean canCreateTrayIcon() {
        return SystemTray.isSupported() && applicationProperties.isUseUI() && applicationProperties.isActiveTray();
    }

    private void addMenuItemsToMenu(PopupMenu popupMenu) {
        propertiesHelper = new PropertiesHelper();
        Optional<Properties> data = propertiesHelper.loadDataProperties();
        if (data.isPresent()) {
            String uploadInfo = String.format("%s [%s]",
                    data.get().getProperty(PropertiesHelper.UPLOAD_DATE_TIME_KEY),
                    data.get().getProperty(PropertiesHelper.UPLOAD_STATUS_KEY)
            );
            popupMenu.add(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
            popupMenu.addSeparator();
        }

        MenuItem showItem = new MenuItem(BundleUtils.getMsg("tray.item.show"));

        showItem.addActionListener(showActionListener());
        popupMenu.add(showItem);

        MenuItem uploadItem = new MenuItem(BundleUtils.getMsg("tray.item.upload", String.valueOf(applicationProperties.periodInDays())));
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

    public EventHandler<WindowEvent> trayOnCloseEventHandler() {
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

            ApplicationProperties appProperties = ApplicationPropertiesFactory.getInstance(args);
            setApplicationProperties(appProperties);
            new Runner(appProperties).run();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
            } else {
                UILauncher.platformExit();
            }
        });
    }

    public void removeTrayIcon() {
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
            }
        }
    }

    public void updateTrayLabels() {
        trayPopupMenu.removeAll();
        addMenuItemsToMenu(trayPopupMenu);
    }

    public boolean tryIconExists() {
        return trayIcon != null;
    }
}
