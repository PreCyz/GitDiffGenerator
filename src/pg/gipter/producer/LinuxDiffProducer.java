package pg.gipter.producer;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class LinuxDiffProducer implements DiffProducer {

    private ApplicationProperties appProps;

    public LinuxDiffProducer(String[] args) {
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

            String gitCommand = GitCommandCreator.gitCommand(appProps.author(), appProps.committerEmail(), appProps.days()).replace("'", "");
            System.out.printf("Git command: %s%n", gitCommand);
            final List<String> gitCmd = Arrays.asList(gitCommand.split(" "));
            /*final List<String> gitCmd = Arrays.asList("git", "log", "-p","--all",
                    "--committer=Pawel",
                    "--committer=precpaw@op.pl",
                    "--since", "2018/09/12",
                    "--until", "2018/09/19"
            );*/

            for (String projectPath : appProps.projectPaths()) {
                System.out.printf("Project path %s%n", projectPath);
                List<String> cdCmd = Arrays.asList("cd", projectPath);
                writeItemToFile(fw, cdCmd, gitCmd);
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }
    }

}
