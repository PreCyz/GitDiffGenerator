package pg.gipter.ui.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Runner;
import pg.gipter.settings.ApplicationProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimerTask;

public class TimerJob extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(TimerJob.class);

    private String name;
    private ApplicationProperties applicationProperties;

    public TimerJob(String name, ApplicationProperties applicationProperties) {
        this.name = name;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void run() {
        logger.info("Job {} {} has executed successfully. Some details {}",
                Thread.currentThread().getName(), name,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );

        Runner runner = new Runner(applicationProperties);
        runner.run();
    }
}
