package pg.gipter.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.DiffCommand;
import pg.gipter.producer.command.DiffCommandFactory;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.settings.ApplicationProperties;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class AbstractDiffProducer implements DiffProducer {

    protected final ApplicationProperties appProps;
    protected final Logger logger;

    AbstractDiffProducer(ApplicationProperties applicationProperties) {
        appProps = applicationProperties;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void produceDiff() {
        try (FileWriter fw = new FileWriter(Paths.get(appProps.itemPath()).toFile())) {

            Set<VersionControlSystem> vcsSet = new HashSet<>();

            for (String projectPath : appProps.projectPaths()) {
                logger.info("Project path: {}", projectPath);
                VersionControlSystem vcs = VersionControlSystem.valueFrom(Paths.get(projectPath).toFile());
                logger.info("Discovered '{}' version control system.", vcs);

                final DiffCommand diffCommand = DiffCommandFactory.getInstance(vcs, appProps);
                List<String> cmd = diffCommand.commandAsList();
                logger.info("{} command: {}", vcs.name(), String.join(" ", cmd));
                cmd = getFullCommand(cmd);
                logger.info("Platform full command: {}", String.join(" ", cmd));

                writeItemToFile(fw, projectPath, cmd);
                vcsSet.add(vcs);
            }

            appProps.setVcs(vcsSet);

            logger.info("Diff file generated and saved as: {}.", appProps.itemPath());

        } catch (Exception ex) {
            logger.error("Error when producing diff.", ex);
            throw new RuntimeException();
        }
    }

    private void writeItemToFile(FileWriter fw, String projectPath, List<String> gitCommand) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand);
        processBuilder.directory(Paths.get(projectPath).toFile());
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                fw.write(String.format("%s%n", line));
                System.out.println(line);
            }
            fw.write(String.format("%nEnd-of-diff-for-%s%n%n%n", projectPath));

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        }
    }

    protected abstract List<String> getFullCommand(List<String> diffCmd);

}
