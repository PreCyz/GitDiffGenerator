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

/**Created by Pawel Gawedzki on 12-Mar-2019.*/
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

    private Trigger createTriggerEveryMonth() {
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        data.put(JobKey.TYPE.value(), JobType.EVERY_MONTH.name());
        data.put(JobKey.DAY_OF_MONTH.value(), String.valueOf(dayOfMonth));
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.HOUR_OF_THE_DAY.value());
        data.remove(JobKey.DAY_OF_WEEK.value());

        return newTrigger()
                .withIdentity("everyMonthTrigger", "everyMonthTriggerGroup")
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(
                        dayOfMonth, hourOfDay, minuteOfHour)
                )
                .build();
    }

    private Trigger createTriggerEvery2Weeks() {
        String hourOfThDay = String.format("%d:%d", hourOfDay, minuteOfHour);
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        data.put(JobKey.TYPE.value(), JobType.EVERY_2_WEEKS.name());
        data.put(JobKey.HOUR_OF_THE_DAY.value(), hourOfThDay);
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.remove(JobKey.DAY_OF_MONTH.value());
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.DAY_OF_WEEK.value());

        Date startDate = DateBuilder.dateOf(hourOfDay, minuteOfHour, 0,
                startDateTime.getDayOfMonth(), startDateTime.getMonthValue(), startDateTime.getYear()
        );
        return TriggerBuilder.newTrigger()
                .withIdentity("every2WeeksTrigger", "every2WeeksTriggerGroup")
                .startAt(startDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(14 * 24) // interval is actually set at 14 * 24 hours' worth of milliseconds
                        .repeatForever())
                .build();
    }

    private Trigger createTriggerEveryWeek() {
        String hourOfThDay = String.format("%s:%s", hourOfDay, minuteOfHour);
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        data.put(JobKey.TYPE.value(), JobType.EVERY_WEEK.name());
        data.put(JobKey.DAY_OF_WEEK.value(), dayOfWeek.name());
        data.put(JobKey.HOUR_OF_THE_DAY.value(), hourOfThDay);
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.DAY_OF_MONTH.value());

        Date startDate = DateBuilder.dateOf(hourOfDay, minuteOfHour, 0,
                startDateTime.getDayOfMonth(), startDateTime.getMonthValue(), startDateTime.getYear()
        );
        return newTrigger()
                .withIdentity("everyWeekTrigger", "everyWeekTriggerGroup")
                .startAt(startDate)
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(dayOfWeek.getValue(), hourOfDay, 0))
                .build();
    }

    private Trigger createCronTrigger() throws ParseException {
        data.put(JobKey.TYPE.value(), JobType.CRON.name());
        data.put(JobKey.CRON.value(), cronExpression);
        data.remove(JobKey.HOUR_OF_THE_DAY.value());
        data.remove(JobKey.DAY_OF_MONTH.value());
        data.remove(JobKey.DAY_OF_WEEK.value());
        data.remove(JobKey.SCHEDULE_START.value());

        CronExpression expression = new CronExpression(cronExpression);
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(expression);

        return newTrigger()
                .withIdentity("cronTrigger", "cronTriggerGroup")
                .startNow()
                .withSchedule(cronScheduleBuilder)
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
