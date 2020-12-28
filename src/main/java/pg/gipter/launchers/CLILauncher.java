package pg.gipter.launchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;

class CLILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(CLILauncher.class);
    private final ApplicationProperties applicationProperties;

    CLILauncher(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void execute() {
        logger.info("Launching command line style.");

        Runner runner = new Runner(applicationProperties);
        runner.start();

        logger.info("Program is terminated.");
        System.exit(-1);
    }
}
