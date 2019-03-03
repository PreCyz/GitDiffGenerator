package pg.gipter.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.Main;
import pg.gipter.launcher.Launcher;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

/**Created by Gawa 2017-10-04*/
public class UILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(UILauncher.class);

    private final Stage primaryStage;
    private ResourceBundle bundle;
    private Window window;
    private final ApplicationProperties applicationProperties;

    public UILauncher(Stage primaryStage, ApplicationProperties applicationProperties) {
        this.primaryStage = primaryStage;
        this.applicationProperties = applicationProperties;
        this.primaryStage.setOnCloseRequest(onCloseEventHandler());
        this.bundle = ResourceBundle.getBundle("bundle.translation", Locale.getDefault());
    }

    private EventHandler<WindowEvent> onCloseEventHandler() {
        return t -> {
            Platform.exit();
            System.exit(0);
        };
    }

    @Override
    public void execute() {
        buildScene(primaryStage, WindowFactory.MAIN.createWindow(applicationProperties, this, bundle));
        primaryStage.show();
    }

    private void buildScene(Stage stage, AbstractWindow window) {
        try {
            Image icon = readImage(window.windowImgFilePath());
            stage.getIcons().add(icon);
        } catch (IOException ex) {
            logger.warn("Problem with loading window icon: {}.", ex.getMessage());
        }
        try {
            stage.setTitle(String.format("%s v%s",
                    bundle.getString(window.windowTitleBundle()),
                    applicationProperties.version()
            ));
            stage.setResizable(window.resizable());
	        Scene scene = new Scene(window.root());
	        if (!StringUtils.nullOrEmpty(window.css())) {
	            scene.getStylesheets().add(window.css());
            }
	        stage.setScene(scene);
            this.window = stage;
        } catch (IOException ex) {
            logger.error("Building scene error.", ex);
        }
    }

    Window currentWindow() {
        return window;
    }

    private Image readImage(String imgPath) throws IOException {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(imgPath)) {
            return new Image(is);
        }
    }

    void changeLanguage(String language) {
        primaryStage.close();
        if (Locale.ENGLISH.getLanguage().equals(language)) {
            this.bundle = ResourceBundle.getBundle("bundle.translation", Locale.ENGLISH);
        } else if ("pl".equals(language)) {
            this.bundle = ResourceBundle.getBundle("bundle.translation_pl");
        }
        execute();
    }
}
