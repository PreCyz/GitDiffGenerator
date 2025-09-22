package pg.gipter.services.restart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractRestartService implements RestartService {
    protected final Logger logger;
    public static final String PROFILE_ENV_PARAM_NAME = "GIPTER-PROFILE";

    AbstractRestartService() {
        logger = LoggerFactory.getLogger(getClass());
    }

    protected void executeCommand(List<String> command) {
        try {
            logger.info("Restarting the application with the following command: {}", String.join(" ", command));
            Process start = new ProcessBuilder(command).start();
            if (start.isAlive()) {
                logger.info("New application instance is starting...");
            }
        } catch (Exception e) {
            logger.error("Could not restart application gracefully. Shutting it down. {}", e.getMessage());
        }
    }
}
