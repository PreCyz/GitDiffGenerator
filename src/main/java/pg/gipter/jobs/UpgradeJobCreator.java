package pg.gipter.jobs;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

import static org.quartz.TriggerBuilder.newTrigger;

class UpgradeJobCreator implements JobCreator {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeJobCreator.class);

    final static TriggerKey UPGRADE_TRIGGER_KEY = new TriggerKey("checkUpgradesTrigger", "checkUpgradesTriggerGroup");
    public static final String UPGRADE_CRON_EXPRESSION = "0 0 12 */3 * ?"; //Every 3 days at noon
    private Trigger trigger;

    @Override
    public JobDetail create() {
        return JobBuilder.newJob(UpgradeJob.class)
                .withIdentity(UpgradeJob.NAME, UpgradeJob.GROUP)
                .build();
    }

    @Override
    public void createTrigger() throws ParseException {
        CronExpression expression = new CronExpression(UPGRADE_CRON_EXPRESSION);
        trigger = newTrigger()
                .withIdentity(UPGRADE_TRIGGER_KEY.getName(), UPGRADE_TRIGGER_KEY.getGroup())
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    @Override
    public Trigger getTrigger() {
        return trigger;
    }

    @Override
    public JobKey getJobKey() {
        return new JobKey(UpgradeJob.NAME, UpgradeJob.GROUP);
    }

    @Override
    public TriggerKey getTriggerKey() {
        return UPGRADE_TRIGGER_KEY;
    }

    @Override
    public void schedule(Scheduler scheduler) {
        try {

            if (scheduler.checkExists(getJobKey())) {
                logger.info("Job with key [{}] already exists. No need to schedule it again.", getJobKey());
            } else {
                createTrigger();
                scheduler.scheduleJob(create(), getTrigger());
                logger.info("New upgrade job scheduled and started.");
            }

        } catch (ParseException | SchedulerException ex) {
            logger.error("Can not schedule upgrade job.", ex);
        }
    }

    @Override
    public JobParam getJobParam() {
        throw new IllegalArgumentException("This job should not have JobParam.");
    }
}
