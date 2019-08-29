package pg.gipter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesConverter;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.launcher.Launcher;
import pg.gipter.launcher.LauncherFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.utils.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Created by Pawel Gawedzki on 17-Sep-2018
 */
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
        setLoggerLevel(applicationProperties.loggerLevel());
        logger.info("Version of application '{}'.", applicationProperties.version());
        logger.info("Gipter can use '{}' threads.", Runtime.getRuntime().availableProcessors());
        convertPropertiesToNewFormat();
        Launcher launcher = LauncherFactory.getLauncher(applicationProperties, primaryStage);
        launcher.execute();
    }

    private void setLoggerLevel(String loggerLevel) {
        if (!StringUtils.nullOrEmpty(loggerLevel)) {
            Set<String> loggers = Stream.of("pg.gipter", "org.springframework", "org.mongodb", "org.quartz").collect(toSet());
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            for (String loggerName : loggers) {
                ch.qos.logback.classic.Logger logger = loggerContext.getLogger(loggerName);
                logger.setLevel(Level.toLevel(loggerLevel));
                Main.logger.info("Level of the logger [{}] is set to [{}]", loggerName, Level.toLevel(loggerLevel));
            }
        }
    }

    private void convertPropertiesToNewFormat() {
        PropertiesConverter converter = DaoFactory.getPropertiesConverter();
        boolean isConverted = converter.convertPropertiesToNewFormat();
        if (isConverted) {
            PropertiesDao propertiesDao = DaoFactory.getPropertiesDao();
            LinkedList<String> configs = new LinkedList<>(propertiesDao.loadAllApplicationProperties().keySet());
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
