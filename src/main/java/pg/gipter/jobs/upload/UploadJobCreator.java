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

    private static JobParam jobParam;
    private final JobType jobType;
    private final LocalDate startDateTime;
    private final int dayOfMonth;
    private final int hourOfDay;
    private final int minuteOfHour;
    private final DayOfWeek dayOfWeek;
    private final String cronExpression;
    private final String configs;

    private Trigger trigger;
    private JobDetail jobDetail;

    UploadJobCreator(JobParam jobParam, JobType jobType, LocalDate startDateTime, int dayOfMonth, int hourOfDay, int minuteOfHour,
                     DayOfWeek dayOfWeek, String cronExpression, String configs) {
        UploadJobCreator.jobParam = jobParam;
        this.jobType = jobType;
        this.startDateTime = startDateTime;
        this.dayOfMonth = dayOfMonth;
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minuteOfHour;
        this.dayOfWeek = dayOfWeek;
        this.cronExpression = cronExpression;
        this.configs = configs;
    }

    private void clearProperties() {
        jobParam = new JobParam();
    }

    private Trigger createTriggerEveryMonth() {
        //0 0 12 1 * ? 	Every month on the 1st, at noon
        clearProperties();
        jobParam.setJobType(JobType.EVERY_MONTH);
        jobParam.setDayOfMonth(dayOfMonth);
        jobParam.setScheduleStart(startDateTime);
        jobParam.setHourOfDay(hourOfDay);
        jobParam.setMinuteOfHour(minuteOfHour);
        jobParam.setConfigsStr(configs);

        return newTrigger()
                .withIdentity(EVERY_MONTH_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(dayOfMonth, hourOfDay, minuteOfHour))
                .build();
    }

    private Trigger createTriggerEvery2Weeks() throws ParseException {
        //0 0 12 */14 * ? 	Every 14 days at noon
        clearProperties();
        jobParam.setJobType(JobType.EVERY_2_WEEKS);
        jobParam.setHourOfDay(hourOfDay);
        jobParam.setMinuteOfHour(minuteOfHour);
        jobParam.setScheduleStart(startDateTime);
        jobParam.setConfigsStr(configs);

        int second = 0;
        Date startDate = DateBuilder.dateOf(
                hourOfDay,
                minuteOfHour,
                second,
                startDateTime.getDayOfMonth(),
                startDateTime.getMonthValue(),
                startDateTime.getYear()
        );

        String cronExpr = "0" + " " +
                minuteOfHour + " " +
                hourOfDay + " " +
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
        String hourOfThDay = String.format("%s:%s", hourOfDay, minuteOfHour);

        clearProperties();
        jobParam.setJobType(JobType.EVERY_WEEK);
        jobParam.setDayOfWeek(dayOfWeek);
        jobParam.setHourOfDay(hourOfDay);
        jobParam.setScheduleStart(LocalDate.now());
        jobParam.setConfigsStr(configs);

        //0 0 12 ? * FRI - Every Friday at noon
        /*String cronExpr = "0" + " " +
                minuteOfHour + " " +
                hourOfDay + " " +
                "? " +
                "* " +
                dayOfWeek.name().substring(0, 3);
        CronExpression expression = new CronExpression(cronExpr);*/
        return newTrigger()
                .withIdentity(EVERY_WEEK_TRIGGER_KEY)
                .startNow()
                //.withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(
                        convertToDateBuilder(dayOfWeek), hourOfDay, minuteOfHour)
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
        clearProperties();
        jobParam.setJobType(JobType.CRON);
        jobParam.setCronExpression(cronExpression);
        jobParam.setConfigsStr(configs);

        CronExpression expression = new CronExpression(cronExpression);
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

        switch (jobType) {
            case CRON:
                trigger = createCronTrigger();
                jobDetail = createJobDetail();
                break;
            case EVERY_MONTH:
                trigger = createTriggerEveryMonth();
                jobDetail = createJobDetail();
                break;
            case EVERY_2_WEEKS:
                trigger = createTriggerEvery2Weeks();
                jobDetail = createJobDetail();
                break;
            default:
                trigger = createTriggerEveryWeek();
                jobDetail = createJobDetail();
        }
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
        getNextFireTime(trigger).ifPresent(nextFireTime -> jobParam.setNextFireDate(nextFireTime));
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
