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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class TrayCreator {

    private static final Logger logger = LoggerFactory.getLogger(TrayCreator.class);

    private final Stage stage;
    private ApplicationProperties applicationProperties;
    private UILauncher uiLauncher;

    public TrayCreator(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        this.uiLauncher = uiLauncher;
        this.stage = uiLauncher.currentWindow();
        this.applicationProperties = applicationProperties;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void createTrayIcon() {
        if (SystemTray.isSupported() && applicationProperties.isUseUI() && applicationProperties.isActiveTray()) {
            stage.setOnCloseRequest(trayOnCloseEventHandler());

            Platform.setImplicitExit(false);
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            Image image = null;
            try (InputStream imgIs = getClass().getClassLoader().getResourceAsStream(Paths.get("img", "chicken-face.jpg").toString())){
                image = ImageIO.read(imgIs);
            } catch (IOException ex) {
                logger.error("Can not read try icon image.", ex);
            }

            BundleUtils.loadBundle();

            PopupMenu popup = new PopupMenu();

            MenuItem statusItem = new MenuItem(BundleUtils.getMsg("tray.item.lastUpdate"));
            popup.add(statusItem);
            popup.addSeparator();

            MenuItem showItem = new MenuItem(BundleUtils.getMsg("tray.item.show"));
            showItem.addActionListener(showActionListener());
            popup.add(showItem);

            MenuItem uploadItem = new MenuItem(BundleUtils.getMsg("tray.item.upload"));
            uploadItem.addActionListener(uploadActionListener());
            popup.add(uploadItem);
            popup.addSeparator();

            MenuItem closeItem = new MenuItem(BundleUtils.getMsg("tray.item.close"));
            closeItem.addActionListener(closeActionListener());
            popup.add(closeItem);

            TrayIcon trayIcon = new TrayIcon(image, BundleUtils.getMsg("main.title", applicationProperties.version()), popup);
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
}
