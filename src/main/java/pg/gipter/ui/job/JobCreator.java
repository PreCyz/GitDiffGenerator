package pg.gipter.ui.job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static org.quartz.TriggerBuilder.newTrigger;

/** Created by Pawel Gawedzki on 12-Mar-2019. */
public class JobCreator {

    private Properties data;
    private JobType jobType;
    private LocalDate startDateTime;
    private int dayOfMonth;
    private int hourOfDay;
    private int minuteOfHour;
    private DayOfWeek dayOfWeek;
    private String cronExpression;

    private Trigger trigger;
    private JobDetail jobDetail;
    private Scheduler scheduler;

    public JobCreator(Properties data, JobType jobType, LocalDate startDateTime, int dayOfMonth, int hourOfDay, int minuteOfHour,
                      DayOfWeek dayOfWeek, String cronExpression, Scheduler scheduler) {
        this.data = data;
        this.jobType = jobType;
        this.startDateTime = startDateTime;
        this.dayOfMonth = dayOfMonth;
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minuteOfHour;
        this.dayOfWeek = dayOfWeek;
        this.cronExpression = cronExpression;
        this.scheduler = scheduler;
    }

    private void clearProperties() {
        data.remove(JobKey.TYPE.value());
        data.remove(JobKey.DAY_OF_MONTH.value());
        data.remove(JobKey.SCHEDULE_START.value());
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.HOUR_OF_THE_DAY.value());
        data.remove(JobKey.DAY_OF_WEEK.value());
    }

    private Trigger createTriggerEveryMonth() {
        //0 0 12 1 * ? 	Every month on the 1st, at noon
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        clearProperties();
        data.put(JobKey.TYPE.value(), JobType.EVERY_MONTH.name());
        data.put(JobKey.DAY_OF_MONTH.value(), String.valueOf(dayOfMonth));
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.put(JobKey.HOUR_OF_THE_DAY.value(), String.format("%d:%d", hourOfDay, minuteOfHour));

        return newTrigger()
                .withIdentity("everyMonthTrigger", "everyMonthTriggerGroup")
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(dayOfMonth, hourOfDay, minuteOfHour))
                .build();
    }

    private Trigger createTriggerEvery2Weeks() throws ParseException {
        //0 0 12 */14 * ? 	Every 14 days at noon
        clearProperties();
        data.put(JobKey.TYPE.value(), JobType.EVERY_2_WEEKS.name());
        data.put(JobKey.HOUR_OF_THE_DAY.value(), String.format("%d:%d", hourOfDay, minuteOfHour));
        data.put(JobKey.SCHEDULE_START.value(), startDateTime.format(ApplicationProperties.yyyy_MM_dd));

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
                .withIdentity("every2WeeksTrigger", "every2WeeksTriggerGroup")
                .startAt(startDate)
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    private Trigger createTriggerEveryWeek() {
        //0 0 12 * * FRI - Every Friday at noon
        String hourOfThDay = String.format("%s:%s", hourOfDay, minuteOfHour);
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        clearProperties();
        data.put(JobKey.TYPE.value(), JobType.EVERY_WEEK.name());
        data.put(JobKey.DAY_OF_WEEK.value(), dayOfWeek.name());
        data.put(JobKey.HOUR_OF_THE_DAY.value(), hourOfThDay);
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);

        int second = 0;
        Date startDate = DateBuilder.dateOf(
                hourOfDay,
                minuteOfHour,
                second,
                startDateTime.getDayOfMonth(),
                startDateTime.getMonthValue(),
                startDateTime.getYear()
        );
        return newTrigger()
                .withIdentity("everyWeekTrigger", "everyWeekTriggerGroup")
                .startAt(startDate)
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(dayOfWeek.getValue(), hourOfDay, minuteOfHour))
                .build();
    }

    private Trigger createCronTrigger() throws ParseException {
        clearProperties();
        data.put(JobKey.TYPE.value(), JobType.CRON.name());
        data.put(JobKey.CRON.value(), cronExpression);

        CronExpression expression = new CronExpression(cronExpression);
        return newTrigger()
                .withIdentity("cronTrigger", "cronTriggerGroup")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    private JobDetail createJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        for (JobKey key : JobKey.values()) {
            if (data.containsKey(key.value())) {
                jobDataMap.put(key.value(), data.getProperty(key.value()));
            }
        }
        return JobBuilder.newJob(GipterJob.class)
                .withIdentity(GipterJob.NAME, GipterJob.GROUP)
                .setJobData(jobDataMap)
                .build();
    }

    private Trigger createTrigger() throws ParseException {
        if (trigger != null) {
            return trigger;
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
        return trigger;
    }

    Properties getDataProperties() {
        return data;
    }

    public Scheduler scheduleJob(Map<String, Object> additionalJobParameters) throws ParseException, SchedulerException {

        Trigger trigger = createTrigger();

        if (additionalJobParameters != null && !additionalJobParameters.isEmpty()) {
            jobDetail.getJobDataMap().putAll(additionalJobParameters);
        }

        if (scheduler != null) {
            scheduler.shutdown();
        }

        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();

        return scheduler;
    }
}
