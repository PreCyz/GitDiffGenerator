package pg.gipter.job.upload;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.service.ToolkitService;
import pg.gipter.ui.FXMultiRunner;
import pg.gipter.ui.RunType;
import pg.gipter.ui.UILauncher;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UploadItemJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(UploadItemJob.class);
    public static final String NAME = "Gipter-job";
    public static final String GROUP = "GipterJobGroup";

    private final ConfigurationDao configurationDao;
    private final DataDao dataDao;

    // Instances of Job must have a public no-argument constructor.
    public UploadItemJob() {
        configurationDao = DaoFactory.getConfigurationDao();
        dataDao = DaoFactory.getDataDao();
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        LocalDateTime nextUploadDate = calculateAndSetDates(jobDataMap);

        logger.info("{} initialized and triggered at {}.",
                NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );

        String configNames = jobDataMap.getString(JobProperty.CONFIGS.key());
        LinkedList<String> configurationNames = new LinkedList<>(Arrays.asList(configNames.split(UploadJobCreator.CONFIG_DELIMITER)));
        UILauncher uiLauncher = (UILauncher) jobDataMap.get(UILauncher.class.getName());

        new FXMultiRunner(new LinkedHashSet<>(configurationNames), uiLauncher.nonUIExecutor(), RunType.UPLOAD_ITEM_JOB).start();

        logger.info("{} finished {}.", NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        dataDao.saveNextUpload(nextUploadDate.format(DateTimeFormatter.ISO_DATE_TIME));

        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                configurationDao.loadArgumentArray(configurationNames.getFirst())
        );
        uiLauncher.updateTray(applicationProperties);
        if (applicationProperties.isToolkitCredentialsSet()) {
            new ToolkitService(applicationProperties).lastItemUploadDate()
                    .ifPresent((lastUploadDate) -> uiLauncher.setLastItemSubmissionDate(
                            LocalDateTime.parse(lastUploadDate, DateTimeFormatter.ISO_DATE_TIME)
                    ));
        }
    }

    private LocalDateTime calculateAndSetDates(JobDataMap jobDataMap) throws JobExecutionException {
        JobType jobType = JobType.valueOf(jobDataMap.getString(JobProperty.TYPE.key()));
        LocalDate startDate = LocalDate.now();
        LocalDateTime nextUploadDate = LocalDateTime.now();
        switch (jobType) {
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
                String cronString = jobDataMap.getString(JobProperty.CRON.key());
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
                nextUploadDate = LocalDateTime.ofInstant(nextValidTimeAfter.toInstant(), ZoneId.systemDefault());
                break;
        }

        setDatesOnConfigs(startDate);

        return nextUploadDate;
    }

    private void setDatesOnConfigs(LocalDate startDate) {
        Map<String, RunConfig> runConfigMap = configurationDao.loadRunConfigMap();
        for (Map.Entry<String, RunConfig> entry : runConfigMap.entrySet()) {
            RunConfig runConfig = entry.getValue();
            runConfig.setStartDate(startDate);
            runConfig.setEndDate(LocalDate.now());
            runConfig.setPreferredArgSource(PreferredArgSource.UI);
            configurationDao.saveRunConfig(runConfig);
        }
    }

}
