package pg.gipter.ui.job;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.ui.FXRunner;
import pg.gipter.ui.UILauncher;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
        LocalDate startDate = LocalDate.now();

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        JobType jobType = JobType.valueOf(jobDataMap.getString(JobKey.TYPE.value()));
        switch (jobType) {
            case EVERY_WEEK:
                startDate = startDate.minusDays(7);
                break;
            case EVERY_MONTH:
                startDate = startDate.minusMonths(startDate.getMonth().length(startDate.isLeapYear()));
                break;
            case EVERY_2_WEEKS:
                startDate = startDate.minusDays(14);
                break;
            case CRON:
                CronExpression cronExpression;
                String cronString = jobDataMap.getString(JobKey.CRON.value());
                try {
                    cronExpression = new CronExpression(cronString);
                } catch (ParseException e) {
                    throw new JobExecutionException(String.format("Invalid cron expression [%s]", cronString));
                }
                Date now = new Date(System.currentTimeMillis());
                Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(now);
                int daysInMillis = 1000 * 60 * 60 * 24;
                int differenceInDays = (int) (nextValidTimeAfter.getTime() - now.getTime()) / daysInMillis;
                startDate = startDate.minusDays(differenceInDays);
                break;
        }

        String[] args = {
                String.format("%s=%s", ArgName.startDate, startDate.format(ApplicationProperties.yyyy_MM_dd)),
                String.format("%s=%s", ArgName.endDate, LocalDate.now().format(ApplicationProperties.yyyy_MM_dd)),
                String.format("%s=%s", ArgName.preferredArgSource, PreferredArgSource.UI),
        };

        ApplicationProperties uiAppProps = ApplicationPropertiesFactory.getInstance(args);
        logger.info("{} initialized and triggered at {}.",
                NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        new FXRunner(uiAppProps).start();
        logger.info("{} finished {}.", NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        UILauncher uiLauncher = (UILauncher) jobDataMap.get(UILauncher.class.getName());
        uiLauncher.updateTray(uiAppProps);
    }
}
