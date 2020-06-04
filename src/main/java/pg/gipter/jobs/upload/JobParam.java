package pg.gipter.jobs.upload;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class JobParam {

    private int minuteOfHour;
    private int hourOfDay;
    private int dayOfMonth;
    private DayOfWeek dayOfWeek;
    private String cronExpression;
    private JobType jobType;
    private LocalDateTime scheduleStart;
    private LocalDateTime nextFireDate;
    private Set<String> configs;

    public JobParam() { }

    JobParam(int minuteOfHour, int hourOfDay, int dayOfMonth, DayOfWeek dayOfWeek, String cronExpression, JobType jobType,
             LocalDateTime scheduleStart, LocalDateTime nextFireDate, Set<String> configs) {
        this.hourOfDay = hourOfDay;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.cronExpression = cronExpression;
        this.jobType = jobType;
        this.scheduleStart = scheduleStart;
        this.nextFireDate = nextFireDate;
        this.configs = configs;
    }

    public int getMinuteOfHour() {
        return minuteOfHour;
    }

    public void setMinuteOfHour(int minuteOfHour) {
        this.minuteOfHour = minuteOfHour;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public LocalDateTime getScheduleStart() {
        return scheduleStart;
    }

    public void setScheduleStart(LocalDateTime scheduleStart) {
        this.scheduleStart = scheduleStart;
    }

    public LocalDateTime getNextFireDate() {
        return nextFireDate;
    }

    public void setNextFireDate(LocalDateTime nextFireDate) {
        this.nextFireDate = nextFireDate;
    }

    public Set<String> getConfigs() {
        return configs;
    }

    public void setConfigs(Set<String> configs) {
        this.configs = configs;
    }

    public String getConfigsStr() {
        return String.join(",", getConfigs());
    }

    public void setConfigsStr(String configs) {
        setConfigs(Stream.of(configs.split(",")).collect(toSet()));
    }
}
