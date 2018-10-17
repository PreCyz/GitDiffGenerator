package pg.gipter.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.DiffCommand;
import pg.gipter.producer.command.DiffCommandFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.*;
import java.util.List;

abstract class AbstractDiffProducer implements DiffProducer {

    protected final ApplicationProperties appProps;
    private final DiffCommand diffCommand;
    protected final Logger logger;

    AbstractDiffProducer(ApplicationProperties applicationProperties) {
        appProps = applicationProperties;
        diffCommand = DiffCommandFactory.getInstance(appProps);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void produceDiff() {
        try (FileWriter fw = new FileWriter(appProps.itemPath())) {

            List<String> cmd = diffCommand.commandAsList();
            logger.info("{} command: {}", appProps.versionControlSystem().name(), String.join(" ", cmd));

            cmd = getFullCommand(cmd);
            logger.info("Platform full command: {}", String.join(" ", cmd));

            for (String projectPath : appProps.projectPaths()) {
                logger.info("Project path: {}", projectPath);
                writeItemToFile(fw, projectPath, cmd);
            }

            logger.info("Diff file generated and saved as: {}.", appProps.itemPath());

        } catch (Exception ex) {
            logger.error("Error when producing diff.", ex);
            throw new RuntimeException();
        }
    }

    private void writeItemToFile(FileWriter fw, String projectPath, List<String> gitCommand) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand);
        processBuilder.directory(new File(projectPath));
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
