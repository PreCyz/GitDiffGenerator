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
import pg.gipter.services.*;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.StringUtils;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/** Created by Pawel Gawedzki on 17-Sep-2018 */
public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static ApplicationProperties applicationProperties;

    @Override
    public void start(Stage primaryStage) {
        if (applicationProperties.isNoSSO()) {
            WebViewService.getInstance();
            Launcher launcher = LauncherFactory.getLauncher(applicationProperties, primaryStage);
            launcher.execute();
        } else {
            new FXWebService(primaryStage).initSSO(InitSource.MAIN);
        }
    }

    public static void main(String[] args) {
        logger.info("Gipter is starting ...");
        InternalMain mObj = new InternalMain(args);
        mObj.setup();
        mObj.setLoggerLevel(applicationProperties.loggerLevel());
        logger.info("Java version [{}].", SystemUtils.javaVersion());
        logger.info("Version of application [{}].", applicationProperties.version().getVersion());
        logger.info("Gipter can use [{}] threads.", ConcurrentService.getInstance().availableThreads());
        mObj.runConverters(applicationProperties);
        mObj.setDefaultConfig();

        boolean regularFlow = false;
        if (!applicationProperties.isNoSSO()) {
            regularFlow = isCookieWorking(args);
        }
        if (regularFlow) {
            regularFlow = initProgramSettings(args);
        }
        if (regularFlow) {
            if (Main.applicationProperties.isUseUI()) {
                launch(args);
            } else {
                Launcher launcher = LauncherFactory.getLauncher(applicationProperties);
                launcher.execute();
            }
        } else {
            if (applicationProperties.isUseUI()) {
                launch(args);
            } else {
                logger.warn("Cookies are not available. Upload is not going to be executed.");
                Launcher launcher = LauncherFactory.getLauncher(applicationProperties);
                launcher.execute();
            }
        }
    }

    private static boolean isCookieWorking(String[] args) {
        try {
            return new ToolkitService(ApplicationPropertiesFactory.getInstance(args))
                    .isCookieWorking(CookiesService.getFedAuthString());
        } catch (IllegalStateException ex) {
            logger.error("SSO is not working: {}", ex.getMessage());
            return false;
        }
    }

    private static boolean initProgramSettings(String[] args) {
        try {
            if (args != null && Arrays.asList(args).contains("env=dev")) {
                ProgramSettings.initProgramSettings(Environment.DEV);
            } else {
                ProgramSettings.initProgramSettings(Environment.PROD);
            }
            return true;
        } catch (IOException ex) {
            logger.error("Program settings can't be initialized: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private static class InternalMain {
        private final String[] args;

        InternalMain(String[] args) {
            this.args = args;
        }

        private void setup() {
            applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            Optional<String> javaHome = Stream.of(args).filter(arg -> arg.startsWith("java.home")).findFirst();
            if (javaHome.isPresent()) {
                System.setProperty("java.home", javaHome.get().split("=")[1]);
                logger.info("New JAVA_HOME {}.", SystemUtils.javaHome());
            }
        }

        private void setLoggerLevel(String loggerLevel) {
            if (StringUtils.notEmpty(loggerLevel)) {
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

        private void runConverters(ApplicationProperties applicationProperties) {
            ConverterFactory.getConverters(applicationProperties).forEach(Converter::convert);
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

}
