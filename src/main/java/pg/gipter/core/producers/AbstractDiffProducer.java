package pg.gipter.core.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.dao.command.CustomCommandDao;
import pg.gipter.core.producers.command.*;
import pg.gipter.core.producers.vcs.VCSVersionProducer;
import pg.gipter.core.producers.vcs.VCSVersionProducerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

abstract class AbstractDiffProducer implements DiffProducer {

    protected final ApplicationProperties applicationProperties;
    protected final Logger logger;
    private boolean noDiff;

    AbstractDiffProducer(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void produceDiff() {
        noDiff = true;
        try (FileWriter fw = new FileWriter(Paths.get(applicationProperties.itemPath()).toFile())) {

            Set<VersionControlSystem> vcsSet = new HashSet<>();

            for (String projectPath : applicationProperties.projectPaths()) {
                logger.info("Project path: {}", projectPath);
                VersionControlSystem vcs = VersionControlSystem.valueFrom(Paths.get(projectPath).toFile());
                VCSVersionProducer VCSVersionProducer = VCSVersionProducerFactory.getInstance(vcs, projectPath);
                logger.info("Discovered '{}' version control system.", VCSVersionProducer.getVersion());

                final DiffCommand diffCommand = DiffCommandFactory.getInstance(vcs, applicationProperties);
                if (applicationProperties.isFetchAll()) {
                    updateRepositories(projectPath, diffCommand);
                }

                List<String> cmd = calculateCommand(diffCommand, vcs);

                writeItemToFile(fw, projectPath, cmd);
                vcsSet.add(vcs);
            }
            applicationProperties.setVcs(vcsSet);
            if (noDiff) {
                String errMsg = String.format("For given repositories within time period [from %s to %s] I couldn't produce any diff.",
                        applicationProperties.startDate().format(ApplicationProperties.yyyy_MM_dd),
                        applicationProperties.endDate().format(ApplicationProperties.yyyy_MM_dd)
                );
                logger.warn(errMsg);
                throw new IllegalArgumentException(errMsg);
            }
            logger.info("Diff file generated and saved as: {}.", applicationProperties.itemPath());
        } catch (Exception ex) {
            logger.error("Error when producing diff.", ex);
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    protected List<String> calculateCommand(DiffCommand diffCommand, VersionControlSystem vcs) {
        List<String> cmd;
        final Optional<CustomCommand> customCommand = CustomCommandDao.readCustomCommand();
        if (customCommand.isPresent() && customCommand.get().containsCommand(vcs)) {
            logger.info("Custom command is used.");
            cmd = customCommand.get().fullCommand(applicationProperties);
            logger.info("{} command: {}", vcs.name(), String.join(" ", cmd));
        } else {
            cmd = diffCommand.commandAsList();
            logger.info("{} command: {}", vcs.name(), String.join(" ", cmd));
            cmd = getFullCommand(cmd);
        }

        logger.info("Platform full command: {}", String.join(" ", cmd));
        return cmd;
    }

    private void updateRepositories(String projectPath, DiffCommand diffCommand) throws IOException {
        logger.info("Updating the repository [{}] with command [{}].", projectPath, String.join(" ", diffCommand.updateRepositoriesCommand()));
        ProcessBuilder processBuilder = new ProcessBuilder(diffCommand.updateRepositoriesCommand());
        processBuilder.directory(Paths.get(projectPath).toFile());
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(String.format("%s%n", line));
            }
            logger.debug(builder.toString());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        }
    }

    private void writeItemToFile(FileWriter fw, String projectPath, List<String> gitCommand) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand);
        processBuilder.directory(Paths.get(projectPath).toFile());
        processBuilder.environment().put("LANG", "pl_PL.UTF-8");
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                fw.write(String.format("%s%n", line));
                noDiff = false;
            }

            if (noDiff) {
                fw.write(String.format("For repository [%s] within period [from %s to %s] diff is unavailable!%n",
                        projectPath,
                        applicationProperties.startDate().format(ApplicationProperties.yyyy_MM_dd),
                        applicationProperties.endDate().format(ApplicationProperties.yyyy_MM_dd)
                ));
            } else {
                fw.write(String.format("%nEnd-of-diff-for-%s%n%n%n", projectPath));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        }
    }

    protected abstract List<String> getFullCommand(List<String> diffCmd);

}
