package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Runner;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

public class TrayHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrayHandler.class);

    private final Stage stage;
    private ApplicationProperties applicationProperties;
    private UILauncher uiLauncher;
    private TrayIcon trayIcon;

    public TrayHandler(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        this.uiLauncher = uiLauncher;
        this.stage = uiLauncher.currentWindow();
        this.applicationProperties = applicationProperties;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void createTrayIcon() {
        if (canCreateTrayIcon()) {
            stage.setOnCloseRequest(trayOnCloseEventHandler());

            Platform.setImplicitExit(false);
            SystemTray tray = SystemTray.getSystemTray();

            BundleUtils.loadBundle();
            PopupMenu popup = new PopupMenu();

            PropertiesHelper propertiesHelper = new PropertiesHelper();
            Optional<Properties> data = propertiesHelper.loadDataProperties();

            if (data.isPresent()) {
                String uploadInfo = String.format("%s [%s]",
                        data.get().getProperty(PropertiesHelper.UPLOAD_DATE_TIME_KEY),
                        data.get().getProperty(PropertiesHelper.UPLOAD_STATUS_KEY)
                );
                MenuItem statusItem = new MenuItem(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
                popup.add(statusItem);
                popup.addSeparator();
            }

            MenuItem showItem = new MenuItem(BundleUtils.getMsg("tray.item.show"));
            showItem.addActionListener(showActionListener());
            popup.add(showItem);

            MenuItem uploadItem = new MenuItem(BundleUtils.getMsg("tray.item.upload"));
            uploadItem.addActionListener(uploadActionListener());
            popup.add(uploadItem);

            MenuItem createJobItem = new MenuItem(BundleUtils.getMsg("tray.item.createJob"));
            createJobItem.addActionListener(createJobActionListener());
            popup.add(createJobItem);
            popup.addSeparator();

            MenuItem closeItem = new MenuItem(BundleUtils.getMsg("tray.item.close"));
            closeItem.addActionListener(closeActionListener());
            popup.add(closeItem);

            trayIcon = new TrayIcon(createTrayImage(), BundleUtils.getMsg("main.title", applicationProperties.version()), popup);
            trayIcon.addActionListener(showActionListener());

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

    private Image createTrayImage() {
        String path = "img/chicken-tray.gif";
        URL imageURL = getClass().getClassLoader().getResource(path);
        if (imageURL == null) {
            logger.error("Resource not found: {}", path);
            return null;
        } else {
            String description = "Tray icon";
            return (new ImageIcon(imageURL, description)).getImage();
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
        return e -> Platform.runLater(() -> new Runner(applicationProperties).run());
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

}
