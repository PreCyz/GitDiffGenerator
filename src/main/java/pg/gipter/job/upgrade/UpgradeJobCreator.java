package pg.gipter.job.upgrade;

import org.quartz.*;

import java.text.ParseException;

import static org.quartz.TriggerBuilder.newTrigger;

public class UpgradeJobCreator {

    final static TriggerKey UPGRADE_TRIGGER_KEY = new TriggerKey("checkUpgradesTrigger", "checkUpgradesTriggerGroup");
    public static final String UPGRADE_CRON_EXPRESSION = "0 0 12 */3 * ?";
    private Trigger trigger;

    public JobDetail create() {
        return JobBuilder.newJob(UpgradeJob.class)
                .withIdentity(UpgradeJob.NAME, UpgradeJob.GROUP)
                .build();
    }

    public void createTrigger() throws ParseException {
        CronExpression expression = new CronExpression(UPGRADE_CRON_EXPRESSION);
        trigger = newTrigger()
                .withIdentity(UPGRADE_TRIGGER_KEY.getName(), UPGRADE_TRIGGER_KEY.getGroup())
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public JobKey getJobKey() {
        return new JobKey(UpgradeJob.NAME, UpgradeJob.GROUP);
    }

    public TriggerKey getTriggerKey() {
        return UPGRADE_TRIGGER_KEY;
    }
}
