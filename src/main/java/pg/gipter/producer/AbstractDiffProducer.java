package pg.gipter.producer;

import pg.gipter.producer.command.DiffCommand;
import pg.gipter.producer.command.DiffCommandFactory;

import java.io.*;
import java.util.List;

abstract class AbstractDiffProducer implements DiffProducer {
    private final ApplicationProperties appProps;
    private final DiffCommand diffCommand;

    AbstractDiffProducer(String[] programParameters) {
        appProps = new ApplicationProperties(programParameters);
        diffCommand = DiffCommandFactory.getInstance(appProps.versionControlSystem(), appProps);
    }

    @Override
    public void produceDiff() {
        try (FileWriter fw = new FileWriter(appProps.itemPath())) {

            List<String> cmd = diffCommand.commandAsList();
            System.out.printf("%s command: %s%n", appProps.versionControlSystem().name(), String.join(" ", cmd));

            cmd = getFullCommand(cmd);
            System.out.printf("Platform full command: %s%n", String.join(" ", cmd));

            for (String projectPath : appProps.projectPaths()) {
                System.out.printf("Project path: %s%n%n", projectPath);
                writeItemToFile(fw, projectPath, cmd);
            }

            System.out.printf("Diff file generated and saved as: %s.%n", appProps.itemPath());

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
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
            System.err.println(ex.getMessage());
            throw new IOException(ex);
        }
    }

    protected abstract List<String> getFullCommand(List<String> diffCmd);

}
