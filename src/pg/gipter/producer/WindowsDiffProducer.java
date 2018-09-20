package pg.gipter.producer;

import java.io.*;

class WindowsDiffProducer implements DiffProducer {

    private ApplicationProperties appProps;

    WindowsDiffProducer(String[] args) {
        this.appProps = new ApplicationProperties(args).init();
    }

    @Override
    public void produceDiff() {

        try (FileWriter fw = new FileWriter(appProps.itemPath())) {

            String gitCommand = GitCommandCreator.gitCommandAsString(
                    appProps.author(), appProps.committerEmail(), appProps.startDate(), appProps.endDate()
            );
            System.out.printf("Git command: %s%n", gitCommand);

            for (String projectPath : appProps.projectPaths()) {
                System.out.printf("Project path %s%n", projectPath);
                String command = appProps.gitBashPath() + " --cd=\"" + projectPath + "\" --login -i -c \"" + gitCommand + "\"";
                writeItemToFile(fw, command, projectPath);
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }

    }

    private void writeItemToFile(FileWriter fw, String command, String projectPath) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

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
}
