package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Runner;
import pg.gipter.settings.ApplicationProperties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;

public class UICustomization {

    private static final Logger logger = LoggerFactory.getLogger(UICustomization.class);

    private final Stage stage;
    private boolean firstTime;
    private TrayIcon trayIcon;
    private final ApplicationProperties applicationProperties;

    public UICustomization(Stage stage, ApplicationProperties applicationProperties) {
        this.firstTime = false;
        this.stage = stage;
        this.applicationProperties = applicationProperties;
    }

    public void createTrayIcon() {
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            java.awt.Image image = null;
            try {
                //URL url = new URL("http://www.digitalphotoartistry.com/rose1.jpg");
                image = ImageIO.read(Paths.get("img", "chicken-face.jpg").toFile());
            } catch (IOException ex) {
                logger.error("Can not read try icon image.", ex);
            }


            stage.setOnCloseRequest(onCloseEventHandler());

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showActionListener());
            popup.add(showItem);

            MenuItem uploadItem = new MenuItem("Upload item");
            uploadItem.addActionListener(uploadActionListener());
            popup.add(uploadItem);

            MenuItem statusItem = new MenuItem("Actual status");
            popup.add(statusItem);

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(closeActionListener());
            popup.add(closeItem);

            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "Title", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(showActionListener());
            // ...
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                logger.error("Error when creating tray.", e);
            }
        }
    }

    private EventHandler<WindowEvent> onCloseEventHandler() {
        return windowEvent -> hide(stage);
    }

    private ActionListener showActionListener() {
        return e -> Platform.runLater(stage::show);
    }

    private ActionListener closeActionListener() {
        return e -> System.exit(0);
    }

    private ActionListener uploadActionListener() {
        return e -> Platform.runLater(() -> new Runner(applicationProperties).run());
    }

    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
                showProgramIsMinimizedMsg();
            } else {
                System.exit(0);
            }
        });
    }

    private void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("Some message.",
                    "Some other message.",
                    TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }
}
