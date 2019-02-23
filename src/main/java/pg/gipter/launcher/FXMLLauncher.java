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

class FXMLLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(FXMLLauncher.class);
    private ApplicationProperties applicationProperties;
    private Stage primaryStage;

    FXMLLauncher(ApplicationProperties applicationProperties, Stage primaryStage) {
        this.applicationProperties = applicationProperties;
        this.primaryStage = primaryStage;
    }

    private URL url() {
        return getClass()
                .getClassLoader()
                .getResource(String.format("fxml%smain.fxml", File.separator));
    }

    @Override
    public void execute() {
        try {
            logger.info("Launching UI style.");
            FXMLLoader loader = new FXMLLoader(url());
            loader.setController(new MainController(applicationProperties));
            primaryStage.setTitle(String.format("Gipter v%s", applicationProperties.version()));
            primaryStage.setResizable(false);
            Scene scene = new Scene(loader.load());
            //scene.getStylesheets().add(window.css());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
