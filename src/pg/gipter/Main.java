package pg.gipter;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class Main {

    private static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static void main(String[] args) {
        ApplicationProperties appProps = new ApplicationProperties(args).init();

        try (FileWriter fw = new FileWriter(appProps.itemPath());) {
            
            String gitCommand = gitCommand(appProps.author(), appProps.committerEmail(), appProps.days());
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
        System.exit(0);
    }
    
    private static String gitCommand(String author, String committerEmail, int daysInThePast) {
        LocalDate now = LocalDate.now();
        LocalDate minusDays = now.minusDays(daysInThePast);
        StringBuilder builder = new StringBuilder("git log -p --all");
        if (notEmpty(author)) {
            builder.append(" --author='").append(author).append("'");
        }
        if (notEmpty(committerEmail)) {
            builder.append(" --committer='").append(committerEmail).append("'");
        }
        builder.append(" --since ").append(minusDays.format(yyyyMMdd));
        builder.append(" --until ").append(now.format(yyyyMMdd));
        return builder.toString();
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void writeItemToFile(FileWriter fw, String command, String projectPath) throws IOException {
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
