package pg.gipter.services.vcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.producers.command.VersionControlSystem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

abstract class AbstractVcsService implements VcsService {

    protected final Logger logger;
    protected String projectPath;

    protected AbstractVcsService() {
        logger = LoggerFactory.getLogger(getClass());
    }

    protected abstract List<String> getAvailabilityCommand();
    protected abstract List<String> getUserNameCommand();
    protected abstract List<String> getUserEmailCommand();

    protected VersionControlSystem getVcs() {
        return VersionControlSystem.NA;
    }

    @Override
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public Optional<String> getUserName() {
        Optional<String> result = Optional.empty();

        ProcessBuilder processBuilder = new ProcessBuilder(getUserNameCommand());
        processBuilder.directory(Paths.get(projectPath).toFile());
        try {
            Process process = processBuilder.start();

            try (InputStream is = process.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {

                String line;
                if ((line = br.readLine()) != null) {
                    result = Optional.of(String.format("%s", line));
                    logger.debug("Git config contains following user.name [{}].", line);
                }
            }
        } catch (Exception ex) {
            logger.error("Unable to retrieve user name from git config. {}", ex.getMessage());
        }

        return result;
    }

    @Override
    public Optional<String> getUserEmail() {
        Optional<String> result = Optional.empty();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(getUserEmailCommand());
            processBuilder.directory(Paths.get(projectPath).toFile());
            Process process = processBuilder.start();

            try (InputStream is = process.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {

                String line;
                if ((line = br.readLine()) != null) {
                    result = Optional.of(String.format("%s", line));
                    logger.debug("Git config contains following user.name [{}].", line);
                }
            }
        } catch (Exception ex) {
            logger.error("Unable to retrieve user email from git config. {}", ex.getMessage());
        }

        return result;
    }

    @Override
    public boolean isVcsAvailableInCommandLine() {
        boolean result = true;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(getAvailabilityCommand());
            processBuilder.directory(Paths.get(projectPath).toFile());
            Process process = processBuilder.start();

            try (InputStream is = process.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {

                String line;
                if ((line = br.readLine()) != null) {
                    logger.debug("Git available from command line - {}.", line);
                }
            }
        } catch (Exception ex) {
            result = false;
            logger.error("Unable to retrieve user email from git config. {}", ex.getMessage());
        }
        return result;
    }
}
