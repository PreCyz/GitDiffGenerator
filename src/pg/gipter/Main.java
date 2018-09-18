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
            
            String gitCommand = gitCommand(appProps.author(), appProps.days());
            System.out.printf("Git command: %s%n", gitCommand);
            
            for (String projectPath : appProps.projectPaths()) {
                System.out.printf("Project path %s%n", projectPath);
                String command = appProps.gitBashPath() + " --cd=\"" + projectPath + "\" --login -i -c \"" + gitCommand + "\"";
                writeItemToFile(fw, command);
            }
            
        } catch (Exception ex) {
            System.err.println(ex);
            System.exit(-1);
        }
        System.exit(0);
    }
    
    private static String gitCommand(String author, int daysInThePast) {
        LocalDate now = LocalDate.now();
        LocalDate minusDays = now.minusDays(daysInThePast);
        return "git log -p --all --author='" + author + "' --since " + minusDays.format(yyyyMMdd) + " --until " + now.format(yyyyMMdd);
    }

    private static void writeItemToFile(FileWriter fw, String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while ((line = br.readLine()) != null) {
            fw.write(String.format("%s%n", line));
            System.out.println(line);
        }
        fw.write(String.format("%n%n%n"));

        br.close();
        isr.close();
        is.close();
    }

}
