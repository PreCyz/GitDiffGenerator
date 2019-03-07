package pg.gipter.ui.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Runner;
import pg.gipter.settings.ApplicationProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GipterJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(GipterJob.class);
    static final String NAME = "GipterJob";
    static final String GROUP = "GipterJobGroup";

    private final ApplicationProperties applicationProperties;

    public GipterJob(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Gipter job initialized and triggered at {}.", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        Runner run = new Runner(applicationProperties);
        run.run();
        logger.info("Gipter job finished {}.");
    }
}
