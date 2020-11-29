package pg.gipter.jobs;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;

import java.text.ParseException;

import static org.quartz.TriggerBuilder.newTrigger;

class LastItemJobCreator implements JobCreator {

    private static final Logger logger = LoggerFactory.getLogger(LastItemJobCreator.class);

    final static TriggerKey TRIGGER_KEY = new TriggerKey("checkLastItemTrigger", "checkLastItemTriggerGroup");
    private Trigger trigger;
    private final ApplicationProperties applicationProperties;

    public LastItemJobCreator(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public JobDetail create() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ApplicationProperties.class.getSimpleName(), applicationProperties);
        return JobBuilder.newJob(LastItemJob.class)
                .withIdentity(LastItemJob.NAME, LastItemJob.GROUP)
                .usingJobData(jobDataMap)
                .build();
    }

    @Override
    public void createTrigger() throws ParseException {
        CronExpression expression = new CronExpression(applicationProperties.getCheckLastItemJobCronExpression());
        trigger = newTrigger()
                .withIdentity(TRIGGER_KEY.getName(), TRIGGER_KEY.getGroup())
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
        return new JobKey(LastItemJob.NAME, LastItemJob.GROUP);
    }

    @Override
    public TriggerKey getTriggerKey() {
        return TRIGGER_KEY;
    }

    @Override
    public void schedule(Scheduler scheduler) {
        try {
            createTrigger();

            scheduler.scheduleJob(create(), getTrigger());
            logger.info("New check last item job scheduled with the following frequency [{}].",
                    applicationProperties.getCheckLastItemJobCronExpression());
        } catch (ParseException | SchedulerException ex) {
            logger.error("Can not schedule upgrade job.", ex);
        }
    }

    @Override
    public JobParam getJobParam() {
        throw new IllegalArgumentException("This job should not have JobParam.");
    }
}
