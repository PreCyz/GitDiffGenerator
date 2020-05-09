package pg.gipter.core.producer.vcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

abstract class AbstractVersionProducer implements VCSVersionProducer {

    private final String projectPath;
    private final Logger logger;

    protected AbstractVersionProducer(String projectPath) {
        this.projectPath = projectPath;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String getVersion() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(getCommand());
        processBuilder.directory(Paths.get(projectPath).toFile());
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("version")) {
                    return line;
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        }
        return "";
    }

    abstract List<String> getCommand();
}
