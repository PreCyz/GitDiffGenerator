package pg.gipter.launcher;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

class FXMLLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(FXMLLauncher.class);
    private ApplicationProperties applicationProperties;
    private Stage primaryStage;
    private final ResourceBundle resourceBundle;

    FXMLLauncher(ApplicationProperties applicationProperties, Stage primaryStage) {
        this.applicationProperties = applicationProperties;
        this.primaryStage = primaryStage;
        resourceBundle = ResourceBundle.getBundle("bundle.translation", Locale.getDefault());
    }

    private URL url() {
        String platform = System.getProperty("os.name");
        String resource = "";
        if ("Linux".equalsIgnoreCase(platform)) {
            resource = String.format("fxml%smain.fxml", File.separator);
        } else if (platform.startsWith("Windows")) {
            resource = String.format("fxml%s%smain.fxml", File.separator, File.separator);
        }

        return getClass()
                .getClassLoader()
                .getResource(resource);
    }

    @Override
    public void execute() {
        try {
            logger.info("Launching UI style.");
            primaryStage.setTitle(String.format("%sv%s",
                    resourceBundle.getString("main.title"), applicationProperties.version()
            ));
            primaryStage.setResizable(false);
            FXMLLoader loader = new FXMLLoader(url(), resourceBundle);
            loader.setController(new MainController(applicationProperties, primaryStage));
            Scene scene = new Scene(loader.load());
            //scene.getStylesheets().add(window.css());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
