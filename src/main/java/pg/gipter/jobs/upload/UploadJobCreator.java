package pg.gipter.jobs.upload;

import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.time.*;
import java.util.*;

import static org.quartz.TriggerBuilder.newTrigger;

/** Created by Pawel Gawedzki on 12-Mar-2019. */
public class UploadJobCreator {

    public static final String CONFIG_DELIMITER = ",";
    public static final TriggerKey CRON_TRIGGER_KEY = new TriggerKey("cronTrigger", "cronTriggerGroup");
    public static final TriggerKey EVERY_WEEK_TRIGGER_KEY = new TriggerKey("everyWeekTrigger", "everyWeekTriggerGroup");
    public static final TriggerKey EVERY_2_WEEKS_CRON_TRIGGER_KEY = new TriggerKey("every2WeeksTrigger", "every2WeeksTriggerGroup");
    public static final TriggerKey EVERY_MONTH_TRIGGER_KEY = new TriggerKey("everyMonthTrigger", "everyMonthTriggerGroup");

    private final JobParam jobParam;

    private Trigger trigger;
    private JobDetail jobDetail;

    UploadJobCreator(JobParam jobParam) {
        this.jobParam = jobParam;
    }

    private Trigger createTriggerEveryMonth() {
        //0 0 12 1 * ? 	Every month on the 1st, at noon
        return newTrigger()
                .withIdentity(EVERY_MONTH_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(
                        jobParam.getDayOfMonth(), jobParam.getHourOfDay(), jobParam.getMinuteOfHour()
                ))
                .build();
    }

    private Trigger createTriggerEvery2Weeks() throws ParseException {
        //0 0 12 */14 * ? 	Every 14 days at noon
        int second = 0;
        Date startDate = DateBuilder.dateOf(
                jobParam.getHourOfDay(),
                jobParam.getMinuteOfHour(),
                second,
                jobParam.getScheduleStart().getDayOfMonth(),
                jobParam.getScheduleStart().getMonthValue(),
                jobParam.getScheduleStart().getYear()
        );

        String cronExpr = "0" + " " +
                jobParam.getMinuteOfHour() + " " +
                jobParam.getHourOfDay() + " " +
                "*/14" + " " +
                "* " +
                "?";
        CronExpression expression = new CronExpression(cronExpr);
        return TriggerBuilder.newTrigger()
                .withIdentity(EVERY_2_WEEKS_CRON_TRIGGER_KEY)
                .startAt(startDate)
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    private Trigger createTriggerEveryWeek() {
        return newTrigger()
                .withIdentity(EVERY_WEEK_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(
                        convertToDateBuilder(jobParam.getDayOfWeek()), jobParam.getHourOfDay(), jobParam.getMinuteOfHour())
                )
                .build();
    }

    private int convertToDateBuilder(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return DateBuilder.MONDAY;
            case TUESDAY:
                return DateBuilder.TUESDAY;
            case WEDNESDAY:
                return DateBuilder.WEDNESDAY;
            case THURSDAY:
                return DateBuilder.THURSDAY;
            case FRIDAY:
                return DateBuilder.FRIDAY;
            case SATURDAY:
                return DateBuilder.SATURDAY;
            default:
                return DateBuilder.SUNDAY;
        }
    }

    private Trigger createCronTrigger() throws ParseException {
        CronExpression expression = new CronExpression(jobParam.getCronExpression());
        return newTrigger()
                .withIdentity(CRON_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    private JobDetail createJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JobParam.class.getSimpleName(), jobParam);
        return JobBuilder.newJob(UploadItemJob.class)
                .withIdentity(UploadItemJob.NAME, UploadItemJob.GROUP)
                .setJobData(jobDataMap)
                .build();
    }

    public void createTrigger() throws ParseException {
        if (trigger != null) {
            return;
        }

        switch (jobParam.getJobType()) {
            case CRON:
                trigger = createCronTrigger();
                break;
            case EVERY_MONTH:
                trigger = createTriggerEveryMonth();
                break;
            case EVERY_2_WEEKS:
                trigger = createTriggerEvery2Weeks();
                break;
            case EVERY_WEEK:
                trigger = createTriggerEveryWeek();
        }
        jobDetail = createJobDetail();
    }

    public JobParam getJobParam() {
        return jobParam;
    }

    private Optional<LocalDateTime> getNextFireTime(Trigger trigger) {
        try {
            CronExpression cronExpression = new CronExpression(((CronTriggerImpl) trigger).getCronExpression());
            Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(trigger.getStartTime());
            return Optional.of(LocalDateTime.ofInstant(nextValidTimeAfter.toInstant(), ZoneId.systemDefault()));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public void addAdditionalParameters(Map<String, Object> additionalJobParameters) {
        if (additionalJobParameters != null && !additionalJobParameters.isEmpty()) {
            jobDetail.getJobDataMap().putAll(additionalJobParameters);
        }
    }

    public void setNextFireDate() {
        getNextFireTime(trigger).ifPresent(jobParam::setNextFireDate);
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public JobKey getJobKey() {
        return new JobKey(UploadItemJob.NAME, UploadItemJob.GROUP);
    }
}
