package pg.gipter.jobs;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class JobParam {

    private Integer minuteOfHour;
    private Integer hourOfDay;
    private Integer dayOfMonth;
    private DayOfWeek dayOfWeek;
    private String cronExpression;
    private JobType jobType;
    private LocalDate scheduleStart;
    private LocalDateTime nextFireDate;
    private Set<String> configs;
    private transient Map<String, Object> additionalJobParams;

    public JobParam() { }

    JobParam(Integer minuteOfHour, Integer hourOfDay, Integer dayOfMonth, DayOfWeek dayOfWeek, String cronExpression,
             JobType jobType, LocalDate scheduleStart, LocalDateTime nextFireDate, Set<String> configs,
             Map<String, Object> additionalJobParams) {
        this.minuteOfHour = minuteOfHour;
        this.hourOfDay = hourOfDay;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.cronExpression = cronExpression;
        this.jobType = jobType;
        this.scheduleStart = scheduleStart;
        this.nextFireDate = nextFireDate;
        this.configs = configs;
        this.additionalJobParams = additionalJobParams;
    }

    public Integer getMinuteOfHour() {
        return minuteOfHour;
    }

    public void setMinuteOfHour(Integer minuteOfHour) {
        this.minuteOfHour = minuteOfHour;
    }

    public Integer getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDayOfWeekStr() {
        if (dayOfWeek != null) {
            return dayOfWeek.name();
        }
        return null;
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

    public LocalDate getScheduleStart() {
        return scheduleStart;
    }

    public void setScheduleStart(LocalDate scheduleStart) {
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

    public Map<String, Object> getAdditionalJobParams() {
        return additionalJobParams;
    }

    public void setAdditionalJobParams(Map<String, Object> additionalJobParams) {
        this.additionalJobParams = additionalJobParams;
    }
}
