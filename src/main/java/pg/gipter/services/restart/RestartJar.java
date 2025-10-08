package pg.gipter.services.restart;

import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class RestartJar extends AbstractRestartService implements RestartService {

    RestartJar() {
        super();
    }

    @Override
    public void start(List<String> programArguments) {
        try {
            logger.info("Restart with arguments {}.", programArguments);
            String javaLocation = SystemUtils.javaHome();
            Optional<String> homeDirectoryPath = JarHelper.homeDirectoryPath();
            if (homeDirectoryPath.isPresent() && Files.exists(Paths.get(homeDirectoryPath.get(), "runtime"))) {
                javaLocation = Paths.get(homeDirectoryPath.get(), "runtime").normalize().toAbsolutePath().toString();
            }

            logger.info("Java used to execute application: [{}]", javaLocation);

            Optional<Path> jarPath = validateAndGetJarPath();

            final String javaW = Paths.get(javaLocation, "bin", "javaw.exe").toString();

            List<String> command = Stream.of(
                    javaW,
                    "-jar",
                    jarPath.orElseThrow(() -> new IllegalArgumentException("Could not find path to jar."))
                            .normalize().toAbsolutePath().toString()
            ).collect(toList());

            command.addAll(programArguments);
            executeCommand(command);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private Optional<Path> validateAndGetJarPath() {
        Optional<Path> jarPath = JarHelper.getJarPath();

        if (jarPath.isEmpty()) {
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
            profile = Set.of(
                            Optional.ofNullable(System.getenv().get(PROFILE_ENV_PARAM_NAME)).orElse(""),
                            Optional.ofNullable(System.getProperty(PROFILE_ENV_PARAM_NAME)).orElse(""))
                    .contains("DEV") ? "DEV" : "PROD";
        } catch (Exception ex) {
            logger.error("Error when getting profile. {}", ex.getMessage());
        }
        return profile;
    }
}
