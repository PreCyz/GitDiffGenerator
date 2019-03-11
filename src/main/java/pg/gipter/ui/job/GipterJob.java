package pg.gipter.ui.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.ui.FXRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GipterJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(GipterJob.class);
    public static final String NAME = "Gipter-job";
    static final String GROUP = "GipterJobGroup";
    static final String APP_PROPS_KEY = "applicationProperties";

    public GipterJob() {
        // Instances of Job must have a public no-argument constructor.
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ApplicationProperties applicationProperties =
                (ApplicationProperties) jobExecutionContext.getMergedJobDataMap().get(APP_PROPS_KEY);

        LocalDate startDate = LocalDate.now().minusDays(applicationProperties.periodInDays());
        LocalDate endDate = LocalDate.now();

        String[] args = {
                String.format("%s=%s", ArgName.startDate, startDate.format(ApplicationProperties.yyyy_MM_dd)),
                String.format("%s=%s", ArgName.endDate, endDate.format(ApplicationProperties.yyyy_MM_dd)),
                String.format("%s=%s", ArgName.preferredArgSource, PreferredArgSource.UI),
        };

        ApplicationProperties uiAppProps = ApplicationPropertiesFactory.getInstance(args);
        logger.info("{} initialized and triggered at {}.",
                NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        new FXRunner(uiAppProps).call();
        logger.info("{} finished {}.", NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
