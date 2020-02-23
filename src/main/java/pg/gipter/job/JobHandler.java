package pg.gipter.job;

import javafx.scene.control.Alert;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.job.upgrade.UpgradeJobCreator;
import pg.gipter.job.upload.JobProperty;
import pg.gipter.job.upload.JobType;
import pg.gipter.job.upload.UploadItemJobBuilder;
import pg.gipter.job.upload.UploadJobCreator;
import pg.gipter.ui.FXMultiRunner;
import pg.gipter.ui.RunType;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;

public class JobHandler {

    private static final Logger logger = LoggerFactory.getLogger(JobHandler.class);

    private Scheduler scheduler;
    private final UpgradeJobCreator upgradeJob;
    private UploadJobCreator uploadJobCreator;

    public JobHandler() {
        upgradeJob = new UpgradeJobCreator();
    }

    public void scheduleUpgradeJob() {
        try {
            upgradeJob.createTrigger();

            if (!isSchedulerInitiated()) {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.scheduleJob(upgradeJob.create(), upgradeJob.getTrigger());
                scheduler.start();
                logger.info("New upgrade job scheduled and started.");
            } else if (!isUpgradeJobExists()) {
                scheduler.scheduleJob(upgradeJob.create(), upgradeJob.getTrigger());
                logger.info("New upgrade job scheduled with following frequency [{}].", UpgradeJobCreator.UPGRADE_CRON_EXPRESSION);
            }
        } catch (ParseException | SchedulerException ex) {
            logger.error("Can not schedule upgrade job.", ex);
        }
    }

    public void cancelUpgradeJob() {
        try {
            scheduler.deleteJob(upgradeJob.getJobKey());
            logger.info("Upgrade job deleted.");
        } catch (SchedulerException e) {
            logger.error("Weird :( can not stop the upgrade job.", e);
        }
    }

    public boolean isUpgradeJobExists() {
        try {
            return isSchedulerInitiated() && scheduler.checkExists(upgradeJob.getTriggerKey());
        } catch (SchedulerException e) {
            return false;
        }
    }

    public boolean isSchedulerInitiated() {
        return scheduler != null;
    }

    public void scheduleUploadJob(UploadItemJobBuilder jobCreatorBuilder, Map<String, Object> additionalJobParameters)
            throws ParseException, SchedulerException {
        uploadJobCreator = jobCreatorBuilder.createJobCreator();
        uploadJobCreator.createTrigger();
        uploadJobCreator.setNextFireDate();
        uploadJobCreator.addAdditionalParameters(additionalJobParameters);
        if (!isSchedulerInitiated()) {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } else if (scheduler.checkExists(uploadJobCreator.getJobKey())) {
            cancelUploadJob();
        }
        if (scheduler.getJobDetail(uploadJobCreator.getJobDetail().getKey()) == null) {
            scheduler.scheduleJob(uploadJobCreator.getJobDetail(), uploadJobCreator.getTrigger());
            logger.info("New upload items job scheduled and started.");
        } else {
            logger.info("Job with key [{}] already exists. No need to schedule it again.", uploadJobCreator.getJobDetail().getKey());
        }
    }

    public void cancelUploadJob() throws SchedulerException {
        scheduler.deleteJob(uploadJobCreator.getJobKey());
        logger.info("Upload job canceled.");
    }

    public String schedulerClassName() {
        return scheduler.getClass().getName();
    }

    public Properties getDataProperties() {
        return uploadJobCreator.getDataProperties();
    }

    public void executeUploadJobIfMissed(Executor executor) {
        try {
            DataDao dataDao = DaoFactory.getDataDao();
            Optional<Properties> data = dataDao.loadDataProperties();
            if (data.isPresent() && isMissedJobExecution(data.get())) {
                String savedNextFireDate = data.get().getProperty(JobProperty.NEXT_FIRE_DATE.key());
                logger.warn("Missed a job execution at [{}].", savedNextFireDate);
                JobType jobType = JobType.valueOf(data.get().getProperty(JobProperty.TYPE.key()));
                LocalDate startDate = null;
                Date nextFireDate = new Date();
                switch (jobType) {
                    case CRON:
                        nextFireDate = scheduler.getTrigger(UploadJobCreator.CRON_TRIGGER_KEY).getNextFireTime();
                        logger.warn("Calculation startDate from cron expression is not supported. Overdue job will not be executed.");
                        break;
                    case EVERY_MONTH:
                        nextFireDate = scheduler.getTrigger(UploadJobCreator.EVERY_MONTH_TRIGGER_KEY).getNextFireTime();
                        startDate = LocalDate.parse(savedNextFireDate, DateTimeFormatter.ISO_DATE_TIME).minusMonths(1);
                        break;
                    case EVERY_2_WEEKS:
                        nextFireDate = scheduler.getTrigger(UploadJobCreator.EVERY_2_WEEKS_CRON_TRIGGER_KEY).getNextFireTime();
                        startDate = LocalDate.parse(savedNextFireDate, DateTimeFormatter.ISO_DATE_TIME).minusWeeks(2);
                        break;
                    case EVERY_WEEK:
                        nextFireDate = scheduler.getTrigger(UploadJobCreator.EVERY_WEEK_TRIGGER_KEY).getNextFireTime();
                        startDate = LocalDate.parse(savedNextFireDate, DateTimeFormatter.ISO_DATE_TIME).minusWeeks(1);
                        break;
                }
                if (startDate != null) {
                    boolean shouldExecute = new AlertWindowBuilder()
                            .withAlertType(Alert.AlertType.WARNING)
                            .withWindowType(WindowType.CONFIRMATION_WINDOW)
                            .withImage(ImageFile.OVERRIDE_PNG)
                            .withTitle(BundleUtils.getMsg("job.missingExecution",
                                    LocalDateTime.parse(savedNextFireDate, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")),
                                    startDate.format(DateTimeFormatter.ISO_DATE)
                            ))
                            .withOkButtonText(BundleUtils.getMsg("job.missingExecution.triggerNow"))
                            .withCancelButtonText(BundleUtils.getMsg("job.missingExecution.dontTrigger"))
                            .buildAndDisplayOverrideWindow();

                    if (shouldExecute) {
                        String configs = data.get().getProperty(JobProperty.CONFIGS.key());
                        if (!StringUtils.nullOrEmpty(configs)) {
                            logger.info("Fixing missed job execution for following configs [{}].", configs);
                            String[] configArray = configs.split(",");
                            List<ApplicationProperties> applicationPropertiesCollection = new ArrayList<>(configArray.length);
                            for (String configName : configArray) {
                                Optional<RunConfig> runConfig = DaoFactory.getCachedConfiguration().loadRunConfig(configName);
                                if (runConfig.isPresent()) {
                                    runConfig.get().setStartDate(startDate);
                                    applicationPropertiesCollection.add(
                                            ApplicationPropertiesFactory.getInstance(runConfig.get().toArgumentArray())
                                    );
                                }
                            }
                            new FXMultiRunner(applicationPropertiesCollection, executor, RunType.FIXING_JOB_EXECUTION).start();
                        } else {
                            logger.warn("From some reason the job is defined but without any specific configurations. I do not know how this happened and can do nothing with it.");
                        }
                    }
                    String nextFireStr = LocalDateTime.ofInstant(nextFireDate.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME);
                    dataDao.saveNextUpload(nextFireStr);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set up the missed job.", ex);
        }
    }

    private boolean isMissedJobExecution(Properties data) {
        boolean result = false;
        Object nextFire = data.get(JobProperty.NEXT_FIRE_DATE.key());
        if (Objects.nonNull(nextFire)) {
            LocalDateTime nextFireDate = LocalDateTime.parse(String.valueOf(nextFire), DateTimeFormatter.ISO_DATE_TIME);
            result = nextFireDate.isBefore(LocalDateTime.now());
        }
        return result;
    }
}
