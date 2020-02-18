package pg.gipter.job.upload;

import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import pg.gipter.core.ApplicationProperties;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.quartz.TriggerBuilder.newTrigger;

/** Created by Pawel Gawedzki on 12-Mar-2019. */
public class UploadJobCreator {

    public static final String CONFIG_DELIMITER = ",";
    public static final TriggerKey CRON_TRIGGER_KEY = new TriggerKey("cronTrigger", "cronTriggerGroup");
    public static final TriggerKey EVERY_WEEK_TRIGGER_KEY = new TriggerKey("everyWeekTrigger", "everyWeekTriggerGroup");
    public static final TriggerKey EVERY_2_WEEKS_CRON_TRIGGER_KEY = new TriggerKey("every2WeeksTrigger", "every2WeeksTriggerGroup");
    public static final TriggerKey EVERY_MONTH_TRIGGER_KEY = new TriggerKey("everyMonthTrigger", "everyMonthTriggerGroup");

    private static Properties data;
    private JobType jobType;
    private LocalDate startDateTime;
    private int dayOfMonth;
    private int hourOfDay;
    private int minuteOfHour;
    private DayOfWeek dayOfWeek;
    private String cronExpression;
    private String configs;

    private Trigger trigger;
    private JobDetail jobDetail;

    UploadJobCreator(Properties data, JobType jobType, LocalDate startDateTime, int dayOfMonth, int hourOfDay, int minuteOfHour,
                     DayOfWeek dayOfWeek, String cronExpression, String configs) {
        UploadJobCreator.data = data;
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
        data.remove(JobProperty.TYPE.key());
        data.remove(JobProperty.DAY_OF_MONTH.key());
        data.remove(JobProperty.SCHEDULE_START.key());
        data.remove(JobProperty.CRON.key());
        data.remove(JobProperty.HOUR_OF_THE_DAY.key());
        data.remove(JobProperty.DAY_OF_WEEK.key());
        data.remove(JobProperty.NEXT_FIRE_DATE.key());
        data.remove(JobProperty.CONFIGS.key());
    }

    private Trigger createTriggerEveryMonth() {
        //0 0 12 1 * ? 	Every month on the 1st, at noon
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        clearProperties();
        data.put(JobProperty.TYPE.key(), JobType.EVERY_MONTH.name());
        data.put(JobProperty.DAY_OF_MONTH.key(), String.valueOf(dayOfMonth));
        data.put(JobProperty.SCHEDULE_START.key(), scheduleStart);
        data.put(JobProperty.HOUR_OF_THE_DAY.key(), String.format("%d:%d", hourOfDay, minuteOfHour));
        data.put(JobProperty.CONFIGS.key(), configs);

        return newTrigger()
                .withIdentity(EVERY_MONTH_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(dayOfMonth, hourOfDay, minuteOfHour))
                .build();
    }

    private Trigger createTriggerEvery2Weeks() throws ParseException {
        //0 0 12 */14 * ? 	Every 14 days at noon
        clearProperties();
        data.put(JobProperty.TYPE.key(), JobType.EVERY_2_WEEKS.name());
        data.put(JobProperty.HOUR_OF_THE_DAY.key(), String.format("%d:%d", hourOfDay, minuteOfHour));
        data.put(JobProperty.SCHEDULE_START.key(), startDateTime.format(ApplicationProperties.yyyy_MM_dd));
        data.put(JobProperty.CONFIGS.key(), configs);

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
        data.put(JobProperty.TYPE.key(), JobType.EVERY_WEEK.name());
        data.put(JobProperty.DAY_OF_WEEK.key(), dayOfWeek.name());
        data.put(JobProperty.HOUR_OF_THE_DAY.key(), hourOfThDay);
        data.put(JobProperty.SCHEDULE_START.key(), LocalDate.now().format(ApplicationProperties.yyyy_MM_dd));
        data.put(JobProperty.CONFIGS.key(), configs);

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
        data.put(JobProperty.TYPE.key(), JobType.CRON.name());
        data.put(JobProperty.CRON.key(), cronExpression);
        data.put(JobProperty.CONFIGS.key(), configs);

        CronExpression expression = new CronExpression(cronExpression);
        return newTrigger()
                .withIdentity(CRON_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    private JobDetail createJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        for (JobProperty key : JobProperty.values()) {
            if (data.containsKey(key.key())) {
                jobDataMap.put(key.key(), data.getProperty(key.key()));
            }
        }
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

    public Properties getDataProperties() {
        return data;
    }

    private Optional<String> getNextFireTime(Trigger trigger) {
        try {
            CronExpression cronExpression = new CronExpression(((CronTriggerImpl) trigger).getCronExpression());
            Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(trigger.getStartTime());
            return Optional.of(LocalDateTime.ofInstant(nextValidTimeAfter.toInstant(), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_DATE_TIME));
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
        getNextFireTime(trigger).ifPresent(nextFireTime -> data.put(JobProperty.NEXT_FIRE_DATE.key(), nextFireTime));
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
