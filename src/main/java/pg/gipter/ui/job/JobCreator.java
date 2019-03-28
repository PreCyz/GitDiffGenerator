package pg.gipter.ui.job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(JobCreator.class);
    private static final TriggerKey UPGRADE_TRIGGER_KEY = new TriggerKey("checkUpgradesTrigger", "checkUpgradesTriggerGroup");
    private static final TriggerKey CRON_TRIGGER_KEY = new TriggerKey("cronTrigger", "cronTriggerGroup");
    private static final TriggerKey EVERY_WEEK_TRIGGER_KEY = new TriggerKey("everyWeekTrigger", "everyWeekTriggerGroup");
    private static final TriggerKey EVERY_2_WEEKS_CRON_TRIGGER_KEY = new TriggerKey("every2WeeksTrigger", "every2WeeksTriggerGroup");
    private static final TriggerKey EVERY_MONTH_TRIGGER_KEY = new TriggerKey("everyMonthTrigger", "everyMonthTriggerGroup");

    private static Properties data;
    private JobType jobType;
    private LocalDate startDateTime;
    private int dayOfMonth;
    private int hourOfDay;
    private int minuteOfHour;
    private DayOfWeek dayOfWeek;
    private String cronExpression;

    private Trigger trigger;
    private JobDetail jobDetail;
    private static Scheduler scheduler;

    JobCreator(Properties data, JobType jobType, LocalDate startDateTime, int dayOfMonth, int hourOfDay, int minuteOfHour,
               DayOfWeek dayOfWeek, String cronExpression) {
        JobCreator.data = data;
        this.jobType = jobType;
        this.startDateTime = startDateTime;
        this.dayOfMonth = dayOfMonth;
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minuteOfHour;
        this.dayOfWeek = dayOfWeek;
        this.cronExpression = cronExpression;
    }

    public static boolean isSchedulerInitiated() {
        return scheduler != null;
    }

    public static void cancelUploadJob() throws SchedulerException {
        scheduler.deleteJob(new JobKey(UploadItemJob.NAME, UploadItemJob.GROUP));
        logger.info("Upload job canceled.");
    }

    public static String schedulerClassName() {
        return scheduler.getClass().getName();
    }

    public static boolean isUpgradeJobExists() {
        try {
            return isSchedulerInitiated() && scheduler.checkExists(UPGRADE_TRIGGER_KEY);
        } catch (SchedulerException e) {
            return false;
        }
    }

    private void clearProperties() {
        data.remove(JobProperty.TYPE.value());
        data.remove(JobProperty.DAY_OF_MONTH.value());
        data.remove(JobProperty.SCHEDULE_START.value());
        data.remove(JobProperty.CRON.value());
        data.remove(JobProperty.HOUR_OF_THE_DAY.value());
        data.remove(JobProperty.DAY_OF_WEEK.value());
    }

    private Trigger createTriggerEveryMonth() {
        //0 0 12 1 * ? 	Every month on the 1st, at noon
        String scheduleStart = startDateTime.format(ApplicationProperties.yyyy_MM_dd);

        clearProperties();
        data.put(JobProperty.TYPE.value(), JobType.EVERY_MONTH.name());
        data.put(JobProperty.DAY_OF_MONTH.value(), String.valueOf(dayOfMonth));
        data.put(JobProperty.SCHEDULE_START.value(), scheduleStart);
        data.put(JobProperty.HOUR_OF_THE_DAY.value(), String.format("%d:%d", hourOfDay, minuteOfHour));

        return newTrigger()
                .withIdentity(EVERY_MONTH_TRIGGER_KEY)
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(dayOfMonth, hourOfDay, minuteOfHour))
                .build();
    }

    private Trigger createTriggerEvery2Weeks() throws ParseException {
        //0 0 12 */14 * ? 	Every 14 days at noon
        clearProperties();
        data.put(JobProperty.TYPE.value(), JobType.EVERY_2_WEEKS.name());
        data.put(JobProperty.HOUR_OF_THE_DAY.value(), String.format("%d:%d", hourOfDay, minuteOfHour));
        data.put(JobProperty.SCHEDULE_START.value(), startDateTime.format(ApplicationProperties.yyyy_MM_dd));

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
        data.put(JobProperty.TYPE.value(), JobType.EVERY_WEEK.name());
        data.put(JobProperty.DAY_OF_WEEK.value(), dayOfWeek.name());
        data.put(JobProperty.HOUR_OF_THE_DAY.value(), hourOfThDay);
        data.put(JobProperty.SCHEDULE_START.value(), LocalDate.now().format(ApplicationProperties.yyyy_MM_dd));

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
        data.put(JobProperty.TYPE.value(), JobType.CRON.name());
        data.put(JobProperty.CRON.value(), cronExpression);

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
            if (data.containsKey(key.value())) {
                jobDataMap.put(key.value(), data.getProperty(key.value()));
            }
        }
        return JobBuilder.newJob(UploadItemJob.class)
                .withIdentity(UploadItemJob.NAME, UploadItemJob.GROUP)
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

    static Properties getDataProperties() {
        return data;
    }

    public void scheduleUploadJob(Map<String, Object> additionalJobParameters) throws ParseException, SchedulerException {

        Trigger trigger = createTrigger();

        if (additionalJobParameters != null && !additionalJobParameters.isEmpty()) {
            jobDetail.getJobDataMap().putAll(additionalJobParameters);
        }

        if (!isSchedulerInitiated()) {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } else if (scheduler.checkExists(new JobKey(UploadItemJob.NAME, UploadItemJob.GROUP))) {
            scheduler.deleteJob(new JobKey(UploadItemJob.NAME, UploadItemJob.GROUP));
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static void scheduleCheckUpgradeJob() {
        try {
            Trigger trigger = checkUpgradeTrigger();

            if (!isSchedulerInitiated()) {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.scheduleJob(createCheckUpgradeJobDetail(), trigger);
                scheduler.start();
                logger.info("New upgrade job scheduled and started.");
            } else if (!isUpgradeJobExists()) {
                scheduler.scheduleJob(createCheckUpgradeJobDetail(), trigger);
                logger.info("New upgrade job scheduled.");
            }
        } catch (ParseException | SchedulerException ex) {
            logger.error("Can not schedule upgrade job.", ex);
        }
    }

    private static Trigger checkUpgradeTrigger() throws ParseException {
        String cronExpr = "0 0 12 */14 * ?";
        CronExpression expression = new CronExpression(cronExpr);
        return newTrigger()
                .withIdentity(UPGRADE_TRIGGER_KEY.getName(), UPGRADE_TRIGGER_KEY.getGroup())
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();
    }

    private static JobDetail createCheckUpgradeJobDetail() {
        return JobBuilder.newJob(CheckUpgradeJob.class)
                .withIdentity(CheckUpgradeJob.NAME, CheckUpgradeJob.GROUP)
                .build();
    }

    public static void deleteUpgradeJob() {
        try {
            scheduler.deleteJob(new JobKey(CheckUpgradeJob.NAME, CheckUpgradeJob.GROUP));
            logger.info("Delete upgrade trigger.");
        } catch (SchedulerException e) {
            logger.error("Weird :( can not stop the upgrade job.", e);
        }
    }
}
