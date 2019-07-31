package pg.gipter;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Launcher;
import pg.gipter.launcher.LauncherFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.utils.PropertiesHelper;

import java.util.Arrays;
import java.util.LinkedList;

/**Created by Pawel Gawedzki on 17-Sep-2018*/
public class Main extends Application {

    private static ApplicationProperties applicationProperties;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static String[] args;

    public static void main(String[] args) {
        logger.info("Gipter started.");
        Main.args = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        applicationProperties = ApplicationPropertiesFactory.getInstance(args);
        logger.info("Version of application '{}'.", applicationProperties.version());
        logger.info("Gipter can use '{}' threads.", Runtime.getRuntime().availableProcessors());
        convertPropertiesToNewFormat();
        Launcher launcher = LauncherFactory.getLauncher(applicationProperties, primaryStage);
        launcher.execute();
    }

    private void convertPropertiesToNewFormat() {
        PropertiesHelper propertiesHelper = new PropertiesHelper();
        boolean isConverted = propertiesHelper.convertPropertiesToNewFormat();
        if (isConverted) {
            LinkedList<String> configs = new LinkedList<>(propertiesHelper.loadAllApplicationProperties().keySet());
            if (!configs.isEmpty()) {
                logger.info("Old configuration converted to new format. [{}] run configs created.", String.join(",", configs));
                String defaultConfigName = configs.getFirst();
                String[] arguments = Arrays.copyOf(args, args.length + 1);
                arguments[arguments.length - 1] = defaultConfigName;
                applicationProperties = ApplicationPropertiesFactory.getInstance(arguments);
                logger.info("Configuration '{}' is set as default one.", defaultConfigName);
            }
        }
    }
}
