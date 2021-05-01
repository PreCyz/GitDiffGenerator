package pg.gipter.services.vcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class GitService implements VcsService {

    private static final Logger logger = LoggerFactory.getLogger(GitService.class);

    private final List<String> userNameCommand = Stream.of("git", "config", "--get", "user.name").collect(toList());
    private final List<String> userEmailCommand = Stream.of("git", "config", "--get", "user.email").collect(toList());
    private final List<String> versionCommand = Stream.of("git", "--version").collect(toList());

    private String projectPath;

    @Override
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public Optional<String> getUserName() {
        Optional<String> result = Optional.empty();

        ProcessBuilder processBuilder = new ProcessBuilder(userNameCommand);
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
            ProcessBuilder processBuilder = new ProcessBuilder(userEmailCommand);
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
    public boolean isGitAvailableInCommandLine() {
        boolean result = true;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(versionCommand);
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
