package pg.gipter.job;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.job.upgrade.UpgradeJobCreator;
import pg.gipter.job.upload.UploadItemJobBuilder;
import pg.gipter.job.upload.UploadJobCreator;

import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

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
            logger.info("Delete upgrade trigger.");
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void scheduleUploadJob(UploadItemJobBuilder jobCreatorBuilder, Map<String, Object> additionalJobParameters) throws ParseException, SchedulerException {
        uploadJobCreator = jobCreatorBuilder.createJobCreator();
        uploadJobCreator.createTrigger();
        uploadJobCreator.setNextFireDate();
        uploadJobCreator.addAdditionalParameters(additionalJobParameters);
        if (!isSchedulerInitiated()) {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } else if (scheduler.checkExists(uploadJobCreator.getJobKey())) {
            scheduler.deleteJob(uploadJobCreator.getJobKey());
        }
        scheduler.scheduleJob(uploadJobCreator.getJobDetail(), uploadJobCreator.getTrigger());
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
}
