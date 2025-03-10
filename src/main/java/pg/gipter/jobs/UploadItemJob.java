package pg.gipter.jobs;

import javafx.application.Platform;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.FlowType;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.services.*;
import pg.gipter.ui.*;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UploadItemJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(UploadItemJob.class);
    public static final String NAME = "Gipter-job";
    public static final String GROUP = "GipterJobGroup";

    private final ConfigurationDao configurationDao;
    private final DataDao dataDao;
    private LocalDateTime nextUploadDate;
    private LocalDate startDate;

    // Instances of Job must have a public no-argument constructor.
    public UploadItemJob() {
        configurationDao = DaoFactory.getCachedConfiguration();
        dataDao = DaoFactory.getDataDao();
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        if (hasRefreshedCredentials(jobDataMap)) {
            runJob(jobDataMap);
        } else {
            logger.warn("Outdated credentials. Job is terminated.");
        }
    }

    private boolean hasRefreshedCredentials(JobDataMap jobDataMap) {
        if (!CookiesService.hasValidFedAuth()) {
            logger.warn("Cookies are not valid. Trying to refresh cookies and continuing the job.");
            Platform.runLater(() -> {
                FXWebService fxWebService = new FXWebService(jobDataMap);
                fxWebService.initMinimizedSSO(FlowType.JOB);
            });
        }
        LocalDateTime waitStart = LocalDateTime.now();
        while (FXWebService.isRunning() && waitStart.isAfter(LocalDateTime.now().minusMinutes(1))) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Problems with sleeping.", e);
            }
        }
        logger.info("Done waiting for authentication after {} seconds.",
                Duration.between(waitStart, LocalDateTime.now()).toSeconds());
        return CookiesService.hasValidFedAuth();
    }

    public void runJob(Map<String, ?> jobDataMap) throws JobExecutionException {
        final JobParam jobParam = (JobParam) jobDataMap.get(JobParam.class.getSimpleName());
        calculateDates(jobParam);

        logger.info("{} initialized and triggered at {}.",
                NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );

        UILauncher uiLauncher = (UILauncher) jobDataMap.get(UILauncher.class.getName());

        new MultiConfigRunner(
                jobParam.getConfigs(), uiLauncher.nonUIExecutor(), RunType.UPLOAD_ITEM_JOB, startDate
        ).start();

        logger.info("{} finished {}.", NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        dataDao.saveNextUploadDateTime(nextUploadDate);

        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                configurationDao.loadArgumentArray(jobParam.getConfigs().iterator().next())
        );
        uiLauncher.updateTray(applicationProperties);
        Optional<String> userId = new ToolkitService(applicationProperties).getUserId();
        if (applicationProperties.isToolkitCredentialsSet() && userId.isPresent()) {
            new ToolkitService(applicationProperties).lastItemModifiedDate(userId.get())
                    .ifPresent((lastUploadDate) -> uiLauncher.setLastItemSubmissionDate(
                            LocalDateTime.parse(lastUploadDate, DateTimeFormatter.ISO_DATE_TIME)
                    ));
        }
    }

    private void calculateDates(JobParam jobParam) throws JobExecutionException {
        startDate = LocalDate.now();
        nextUploadDate = LocalDateTime.now();
        switch (jobParam.getJobType()) {
            case EVERY_WEEK:
                startDate = startDate.minusDays(7);
                nextUploadDate = nextUploadDate.plusDays(7);
                break;
            case EVERY_MONTH:
                startDate = startDate.minusMonths(startDate.getMonth().length(startDate.isLeapYear()));
                nextUploadDate = nextUploadDate.plusMonths(1);
                break;
            case EVERY_2_WEEKS:
                startDate = startDate.minusDays(14);
                nextUploadDate = nextUploadDate.plusDays(14);
                break;
            case CRON:
                CronExpression cronExpression;
                try {
                    cronExpression = new CronExpression(jobParam.getCronExpression());
                } catch (ParseException e) {
                    throw new JobExecutionException(String.format("Invalid cron expression [%s]", jobParam.getCronExpression()));
                }
                Date now = new Date(System.currentTimeMillis());
                Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(now);
                int daysInMillis = 1000 * 60 * 60 * 24;
                int differenceInDays = (int) (nextValidTimeAfter.getTime() - now.getTime()) / daysInMillis;
                startDate = startDate.minusDays(differenceInDays);
                nextUploadDate = LocalDateTime.ofInstant(nextValidTimeAfter.toInstant(), ZoneId.systemDefault());
                break;
        }
    }

}
