package pg.gipter.services.restart;

import pg.gipter.utils.SystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class RestartExe extends AbstractRestartService {

    RestartExe() {
        super();
    }

    @Override
    public void start(List<String> programArguments) {
        try {
            logger.info("Restart with arguments {}.", programArguments);
            final String systemJavaHome = SystemUtils.javaHome();
            logger.info("System JAVA_HOME: [{}]", systemJavaHome);

            Path exe = Path.of(".", "Gipter.exe");
            logger.info("Exe file detected: [{}]. File exists: [{}]", exe.toAbsolutePath().normalize(), Files.exists(exe));
            List<String> command = Stream.of(exe.toAbsolutePath().normalize().toString()).collect(toList());

            command.addAll(programArguments);
            executeCommand(command);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
