package pg.gipter.services.vcs;

import java.io.*;
import java.nio.file.Paths;

public class GitService implements VcsService {





    @Override
    public String getUserName() {
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

        return null;
    }

    @Override
    public String getUserEmail() {
        return null;
    }
}
