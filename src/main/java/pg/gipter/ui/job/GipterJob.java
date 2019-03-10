package pg.gipter.ui.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.FXRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GipterJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(GipterJob.class);
    static final String NAME = "GipterJob";
    static final String GROUP = "GipterJobGroup";
    static final String APP_PROPS_KEY = "applicationProperties";

    public GipterJob() {
        // Instances of Job must have a public no-argument constructor.
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ApplicationProperties applicationProperties =
                (ApplicationProperties) jobExecutionContext.getMergedJobDataMap().get(APP_PROPS_KEY);
        logger.info("Gipter job initialized and triggered at {}.",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        FXRunner runner = new FXRunner(applicationProperties);
        runner.run();
        logger.info("Gipter job finished {}.");
    }
}
