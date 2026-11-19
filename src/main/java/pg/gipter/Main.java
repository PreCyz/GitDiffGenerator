package pg.gipter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.converters.Converter;
import pg.gipter.converters.ConverterFactory;
import pg.gipter.core.*;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.launchers.Launcher;
import pg.gipter.launchers.LauncherFactory;
import pg.gipter.services.*;
import pg.gipter.services.restart.AbstractRestartService;
import pg.gipter.services.upgrade.BackupService;
import pg.gipter.ui.WizardLauncher;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.StringUtils;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/** Created by Pawel Gawedzki on 17-Sep-2018 */
public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static ApplicationProperties applicationProperties;
    private static FlowType flowType;
    private static String[] startupArguments;

    @Override
    public void start(Stage primaryStage) {
        if (flowType == FlowType.CONFIGURE) {
            // Keep JavaFX alive between closing the wizard and showing the next window.
            Platform.setImplicitExit(false);
            if (!new WizardLauncher(primaryStage, applicationProperties).showAndSave()) {
                Platform.exit();
                return;
            }
            Thread.ofVirtual().start(() -> {
                try {
                    flowType = prepareConfiguredStartup(applicationProperties, startupArguments);
                } catch (RuntimeException ex) {
                    logger.error("Could not start with the saved configuration.", ex);
                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                        Platform.exit();
                    });
                    return;
                }
                Platform.runLater(() -> {
                    Platform.setImplicitExit(true);
                    startConfigured(primaryStage);
                });
            });
        } else {
            startConfigured(primaryStage);
        }
    }

    private void startConfigured(Stage primaryStage) {
        if (FlowType.INIT == flowType) {
            new FXWebService(primaryStage).onAuthenticated(() -> Thread.ofVirtual().start(() -> {
                boolean initialized = initProgramSettings(startupArguments);
                flowType = initialized ? FlowType.REGULAR : FlowType.NO_UPLOAD;
                Platform.runLater(() -> {
                    WebViewService.getInstance();
                    LauncherFactory.getLauncher(applicationProperties, primaryStage).execute();
                });
            })).initSSO(flowType);
        } else {
            WebViewService.getInstance();
            Launcher launcher = LauncherFactory.getLauncher(applicationProperties, primaryStage);
            launcher.execute();
        }
    }

    public static void main(String[] args) {
        startupArguments = args.clone();
        logger.info("Gipter is starting ...");
        restoreBackup();
        InternalMain mObj = new InternalMain(args);
        mObj.setup();
        mObj.setLoggerLevel(applicationProperties.loggerLevel());
        logger.info("Java version [{}].", SystemUtils.javaVersion());
        logger.info("Version of application [{}].", applicationProperties.version().getVersion());
        logger.info("Gipter can use [{}] threads.", Runtime.getRuntime().availableProcessors());
        mObj.runConverters(applicationProperties);
        mObj.setDefaultConfig();

        flowType = startupFlow(applicationProperties, args);
        logger.info("Calculated flow type is [{}].", flowType);
        if (applicationProperties.isUseUI() || flowType == FlowType.INIT) {
            launch(args);
        } else {
            LauncherFactory.getLauncher(applicationProperties).execute();
        }
    }

    static FlowType startupFlow(ApplicationProperties properties, String[] args) {
        if (properties.isUseUI() && properties.getRunConfigMap().isEmpty()) {
            return FlowType.CONFIGURE;
        }
        return prepareConfiguredStartup(properties, args);
    }

    private static FlowType prepareConfiguredStartup(ApplicationProperties properties, String[] args) {
        CookiesService.configure(properties);
        FlowType requested = Stream.of(args)
                .filter(arg -> arg.startsWith(ArgName.flowType.name() + "="))
                .map(arg -> FlowType.valueOf(arg.substring(arg.indexOf('=') + 1)))
                .findFirst().orElse(FlowType.REGULAR);
        if (!properties.isNoSSO() && (requested == FlowType.INIT
                || !CookiesService.isCookiesFileExist() || !isCookieWorking(properties))) {
            logger.info("Toolkit session requires WebView2 sign-in.");
            return FlowType.INIT;
        }
        boolean initialized = initProgramSettings(args);
        return initialized && !properties.isNoSSO() ? FlowType.REGULAR : FlowType.NO_UPLOAD;
    }

    private static void restoreBackup() {
        if (SystemUtils.isMsi()) {
            if (Files.exists(BackupService.gipterTmp())) {
                BackupService.restoreBackup();
            }
            Path gifJson = Path.of(".", DaoConstants.GIFS_JSON);
            if (!Files.exists(gifJson)) {
                Path source = Path.of("app", DaoConstants.GIFS_JSON);
                try {
                    Files.copy(
                            source,
                            gifJson,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                    logger.info("Gifs definition was moved from the [{}].", source);
                } catch (IOException e) {
                    logger.warn("Failed to move gif files from [{}] to [{}]: {}", gifJson, source, e.getMessage());
                }
            }
        }
    }

    private static boolean isCookieWorking(ApplicationProperties properties) {
        try {
            return new ToolkitService(properties).isCookieWorking();
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

    private record InternalMain(String[] args) {

        private void setup() {
            applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            Optional<String> javaHome = Stream.of(args).filter(arg -> arg.startsWith("java.home")).findFirst();
            if (javaHome.isPresent()) {
                System.setProperty("java.home", javaHome.get().split("=")[1]);
                logger.info("New JAVA_HOME {}.", SystemUtils.javaHome());
            }
            if (Stream.of(args).anyMatch("env=dev"::equalsIgnoreCase)) {
                System.setProperty(AbstractRestartService.PROFILE_ENV_PARAM_NAME, "DEV");
                logger.warn("DEV profile is on.");
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
