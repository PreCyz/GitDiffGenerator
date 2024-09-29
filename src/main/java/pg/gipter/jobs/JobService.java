package pg.gipter.jobs;

import javafx.scene.control.Alert;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.FlowType;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.model.RunConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.services.FXWebService;
import pg.gipter.ui.MultiConfigRunner;
import pg.gipter.ui.RunType;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;

public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    public static final String CONFIG_DELIMITER = ",";

    private Scheduler scheduler;

    public void scheduleJob(JobCreator jobCreator) throws SchedulerException {
        if (!isSchedulerInitiated()) {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            logger.info("Default scheduler created.");
        }
        jobCreator.schedule(scheduler);
        scheduler.start();
    }

    public void deleteJob(JobCreator jobCreator) throws SchedulerException {
        scheduler.deleteJob(jobCreator.getJobKey());
        logger.info("{} job deleted.", jobCreator.getJobKey().getName());
    }

    public boolean isSchedulerInitiated() {
        return scheduler != null;
    }

    public String schedulerClassName() {
        return scheduler.getClass().getName();
    }

    public JobParam getJobParam(JobCreator jobCreator) {
        return jobCreator.getJobParam();
    }

    public void executeUploadJobIfMissed(Executor executor) {
        try {
            DataDao dataDao = DaoFactory.getDataDao();
            final Optional<JobParam> jobParamOpt = dataDao.loadJobParam();
            if (jobParamOpt.isPresent() && isMissedJobExecution(jobParamOpt.get())) {
                final JobParam jobParam = jobParamOpt.get();
                String savedNextFireDate = jobParam.getNextFireDate().format(DateTimeFormatter.ISO_DATE_TIME);
                logger.warn("Missed a job execution at [{}].", savedNextFireDate);
                LocalDate startDate = null;
                Date nextFireDate = new Date();
                switch (jobParam.getJobType()) {
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
                            .withHeaderText(BundleUtils.getMsg("job.missingExecution",
                                    LocalDateTime.parse(savedNextFireDate, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")),
                                    startDate.format(DateTimeFormatter.ISO_DATE)
                            ))
                            .withImageFile(ImageFile.OVERRIDE_PNG)
                            .withOkButtonText(BundleUtils.getMsg("job.missingExecution.triggerNow"))
                            .withCancelButtonText(BundleUtils.getMsg("job.missingExecution.dontTrigger"))
                            .buildAndDisplayOverrideWindow();

                    if (shouldExecute) {
                        if (!jobParam.getConfigs().isEmpty()) {
                            final LocalDate start = LocalDate.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth());
                            if (!CookiesService.hasValidFedAuth()) {
                                FXWebService fxWebService = new FXWebService().initMinimizedSSO(FlowType.MISSED_JOB);
                                executor.execute(() -> delayExecuteMissedJob(fxWebService, executor, jobParam, start));
                            } else {
                                executor.execute(() -> executeMissedJob(executor, jobParam, start));
                            }
                        } else {
                            logger.warn("From some reason the job is defined but without any specific configurations. I do not know how this happened and can do nothing with it.");
                        }
                    }
                    dataDao.saveNextUploadDateTime(LocalDateTime.ofInstant(nextFireDate.toInstant(), ZoneId.systemDefault()));
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set up the missed job.", ex);
        }
    }

    private boolean isMissedJobExecution(JobParam jobParam) {
        boolean result = false;
        if (jobParam.getNextFireDate() != null) {
            result = jobParam.getNextFireDate().isBefore(LocalDateTime.now());
        }
        return result;
    }

    public boolean isJobExist(JobCreator jobCreator) {
        try {
            return scheduler.checkExists(jobCreator.getTriggerKey());
        } catch (SchedulerException e) {
            return false;
        }
    }

    private void executeMissedJob(Executor executor, JobParam jobParam, LocalDate startDate) {
        logger.info("Fixing missed job execution for following configs [{}].", jobParam.getConfigs());
        List<ApplicationProperties> applicationPropertiesCollection = new ArrayList<>(jobParam.getConfigs().size());
        for (String configName : jobParam.getConfigs()) {
            Optional<RunConfig> runConfig = DaoFactory.getCachedConfiguration().loadRunConfig(configName);
            if (runConfig.isPresent()) {
                runConfig.get().setStartDate(startDate);
                applicationPropertiesCollection.add(
                        ApplicationPropertiesFactory.getInstance(runConfig.get().toArgumentArray())
                );
            }
        }
        new MultiConfigRunner(applicationPropertiesCollection, executor, RunType.FIXING_JOB_EXECUTION).start();
    }

    private void delayExecuteMissedJob(FXWebService fxWebService, Executor executor, JobParam jobParam, LocalDate startDate) {
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
        if (!CookiesService.hasValidFedAuth()) {
            logger.warn("Authentication process is not successful. Fix is terminated.");
            return;
        }
        executeMissedJob(executor, jobParam, startDate);
    }
}
