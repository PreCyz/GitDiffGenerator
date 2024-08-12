package pg.gipter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class RestartService {
    private static final Logger logger = LoggerFactory.getLogger(RestartService.class);
    public static final String PROFILE_ENV_PARAM_NAME = "GIPTER-PROFILE";

    RestartService() {
    }

    public void start(List<String> programArguments) {
        logger.info("Restart with arguments {}.", programArguments);
        try {
            final String javaHome = Paths.get(SystemUtils.javaHome(), "bin", "java").toString();
            logger.info("[{}}] java home is going to be used", javaHome);
            Optional<Path> jarPath = validateAndGetJarPath();

            final List<String> command = Stream.of(
                    javaHome, "-jar",
                    jarPath.get().toAbsolutePath().toString()
            ).collect(toList());
            command.addAll(programArguments);

            executeCommand(command);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private Optional<Path> validateAndGetJarPath() {
        Optional<Path> jarPath = JarHelper.getJarPath();

        if (!jarPath.isPresent()) {
            logger.error("Error when restarting application. Could not find jar file.");
            System.exit(-1);
        }
        if ("DEV".equalsIgnoreCase(getProfile())) {
            final String classesPath = jarPath.get().toAbsolutePath().toString();
            jarPath = Optional.of(classesPath.replaceFirst("classes", "Gipter.jar")).map(Paths::get);
        }

        if (!Files.exists(jarPath.get()) || !Files.isRegularFile(jarPath.get())) {
            logger.error("Error when restarting application. [{}] is not a file.", jarPath.get().toAbsolutePath());
            System.exit(-2);
        }
        logger.info("Path to jar file: [{}]", jarPath.get().toAbsolutePath());
        return jarPath;
    }

    private String getProfile() {
        String profile = "PROD";
        try {
            profile = Stream.of(
                            Optional.ofNullable(System.getenv().get(PROFILE_ENV_PARAM_NAME)).orElse(""),
                            Optional.ofNullable(System.getProperty(PROFILE_ENV_PARAM_NAME)).orElse(""))
                    .collect(toSet())
                    .contains("DEV") ? "DEV" : "PROD";
        } catch (Exception ex) {
            logger.error("Error when getting profile. {}", ex.getMessage());
        }
        return profile;
    }

    private void executeCommand(List<String> command) {
        try {
            logger.info("Restarting the application with the following command: {}", String.join(" ", command));
            Process start = new ProcessBuilder(command).start();
            if (start.isAlive()) {
                logger.info("New application instance is starting...");
            }
        } catch (Exception e) {
            logger.error("Could not restart application gracefully. Shutting it down. {}", e.getMessage());
        }
    }
}
