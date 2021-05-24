package pg.gipter.launchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class CLILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(CLILauncher.class);
    private final ApplicationProperties applicationProperties;
    private final Executor executor;

    CLILauncher(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void execute() {
        logger.info("Launching command line style.");

        Runner runner = new Runner(applicationProperties, executor);
        runner.start();

        logger.info("Program is terminated.");
        System.exit(-1);
    }
}
