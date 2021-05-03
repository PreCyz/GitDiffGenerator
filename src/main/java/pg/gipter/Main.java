package pg.gipter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.converters.Converter;
import pg.gipter.converters.ConverterFactory;
import pg.gipter.core.*;
import pg.gipter.launchers.Launcher;
import pg.gipter.launchers.LauncherFactory;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Created by Pawel Gawedzki on 17-Sep-2018
 */
public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private String[] args;
    private static ApplicationProperties applicationProperties;

    public static void main(String[] args) {
        logger.info("Gipter started.");
        Main mObj = new Main(args);
        mObj.setLoggerLevel(applicationProperties.loggerLevel());
        logger.info("Version of application '{}'.", applicationProperties.version().getVersion());
        logger.info("Gipter can use '{}' threads.", Runtime.getRuntime().availableProcessors());
        mObj.runConverters();
        mObj.setDefaultConfig();
        if (Main.applicationProperties.isUseUI()) {
            WebViewService.getInstance();
            launch(args);
        } else {
            Launcher launcher = LauncherFactory.getLauncher(applicationProperties);
            launcher.execute();
        }
    }

    public Main() {
    }

    Main(String[] args) {
        this.args = args;
        applicationProperties = ApplicationPropertiesFactory.getInstance(args);
        Optional<String> javaHome = Stream.of(args).filter(arg -> arg.startsWith("java.home")).findFirst();
        if (javaHome.isPresent()) {
            System.setProperty("java.home", javaHome.get().split("=")[1]);
            logger.info("New JAVA_HOME {}.", System.getProperty("java.home"));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Launcher launcher = LauncherFactory.getLauncher(applicationProperties, primaryStage);
        launcher.execute();
    }

    private void setLoggerLevel(String loggerLevel) {
        if (!StringUtils.nullOrEmpty(loggerLevel)) {
            Set<String> loggers = Stream.of(
                    "pg.gipter",
                    "org.springframework",
                    "org.mongodb",
                    "org.quartz"
            ).collect(toSet());

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            for (String loggerName : loggers) {
                ch.qos.logback.classic.Logger logger = loggerContext.getLogger(loggerName);
                logger.setLevel(Level.toLevel(loggerLevel));
                Main.logger.info("Level of the logger [{}] is set to [{}]", loggerName, Level.toLevel(loggerLevel));
            }
        }
    }

    private void runConverters() {
        ConverterFactory.getConverters().forEach(Converter::convert);
    }

    private void setDefaultConfig() {
        LinkedList<String> configs = new LinkedList<>(applicationProperties.getRunConfigMap().keySet());
        if (!configs.isEmpty()) {
            String defaultConfigName = configs.getFirst();
            String[] arguments = Arrays.copyOf(args, args.length + 1);
            arguments[arguments.length - 1] = ArgName.configurationName.name() + "=" + defaultConfigName;
            applicationProperties = ApplicationPropertiesFactory.getInstance(arguments);
            logger.info("Default configuration set as [{}].", defaultConfigName);
        }
    }
}
