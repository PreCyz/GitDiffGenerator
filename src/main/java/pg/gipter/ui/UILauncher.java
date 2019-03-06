package pg.gipter.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.Main;
import pg.gipter.launcher.Launcher;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**Created by Gawa 2017-10-04*/
public class UILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(UILauncher.class);

    private final Stage primaryStage;
    private final ApplicationProperties applicationProperties;

    public UILauncher(Stage primaryStage, ApplicationProperties applicationProperties) {
        this.primaryStage = primaryStage;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void execute() {
        logger.info("Launching UI.");
        BundleUtils.loadBundle();
        buildScene(
                primaryStage,
                WindowFactory.MAIN.createWindow(applicationProperties, this)
        );
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
            stage.setTitle(BundleUtils.getMsg(
                    window.windowTitleBundle(), applicationProperties.version()
            ));
            stage.setResizable(window.resizable());
	        Scene scene = new Scene(window.root());
	        if (!StringUtils.nullOrEmpty(window.css())) {
	            scene.getStylesheets().add(window.css());
            }
	        stage.setScene(scene);
        } catch (IOException ex) {
            logger.error("Building scene error.", ex);
        }
    }

    public Stage currentWindow() {
        return primaryStage;
    }

    private Image readImage(String imgPath) throws IOException {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(imgPath)) {
            return new Image(is);
        }
    }

    public void changeLanguage(String language) {
        primaryStage.close();
        BundleUtils.changeBundle(language);
        execute();
    }

    public static void platformExit() {
        Platform.exit();
        System.exit(0);
    }
}
