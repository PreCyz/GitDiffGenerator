package pg.gipter.producer;

import java.io.*;
import java.util.Arrays;
import java.util.List;

class LinuxDiffProducer implements DiffProducer {

    private ApplicationProperties appProps;

    LinuxDiffProducer(String[] args) {
        this.appProps = new ApplicationProperties(args).init();
    }

    private void writeItemToFile(FileWriter fw, List<String> cdCommand, List<String> gitCommand) throws IOException {

        Process process = new ProcessBuilder().command(cdCommand).command(gitCommand).start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;

            while ((line = br.readLine()) != null) {
                fw.write(String.format("%s%n", line));
                System.out.println(line);
            }
            fw.write(String.format("%nEnd-of-diff-for-%s%n%n%n", cdCommand.get(cdCommand.size() -1)));

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            throw new IOException(ex);
        }
    }

    @Override
    public void produceDiff() {

        try (FileWriter fw = new FileWriter(appProps.itemPath())) {

            List<String> gitCommand = GitCommandCreator.gitCommandAsList(appProps.author(), appProps.committerEmail(), appProps.days());
            System.out.printf("Git command: %s%n", gitCommand);

            for (String projectPath : appProps.projectPaths()) {
                System.out.printf("Project path %s%n", projectPath);
                List<String> cdCmd = Arrays.asList("cd", projectPath);
                writeItemToFile(fw, cdCmd, gitCommand);
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }
    }

}
