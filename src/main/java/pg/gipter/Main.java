package pg.gipter;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Launcher;
import pg.gipter.launcher.LauncherFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;

/**Created by Pawel Gawedzki on 17-Sep-2018*/
public class Main extends Application {

    private static ApplicationProperties applicationProperties;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Gipter started.");
        applicationProperties = ApplicationPropertiesFactory.getInstance(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Version of application '{}'.", applicationProperties.version());
        Launcher launcher = LauncherFactory.getLauncher(applicationProperties, primaryStage);
        launcher.execute();
    }
}
