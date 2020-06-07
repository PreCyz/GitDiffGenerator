package pg.gipter.jobs.upload;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class UploadItemJobBuilder {
    private JobParam jobParam;
    private JobType jobType;
    private LocalDate startDateTime;
    private LocalDateTime nextFireDateTime;
    private Integer dayOfMonth;
    private Integer hourOfDay;
    private Integer minuteOfHour;
    private DayOfWeek dayOfWeek;
    private String cronExpression;
    private String configs;

    public UploadItemJobBuilder withJobParam(JobParam jobParam) {
        this.jobParam = jobParam;
        return this;
    }

    public UploadItemJobBuilder withJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    public UploadItemJobBuilder withStartDate(LocalDate startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public UploadItemJobBuilder withNextFireDateTime(LocalDateTime nextFireDateTime) {
        this.nextFireDateTime = nextFireDateTime;
        return this;
    }

    public UploadItemJobBuilder withDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    public UploadItemJobBuilder withHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
        return this;
    }

    public UploadItemJobBuilder withMinuteOfHour(Integer minuteOfHour) {
        this.minuteOfHour = minuteOfHour;
        return this;
    }

    public UploadItemJobBuilder withDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    public UploadItemJobBuilder withCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public UploadItemJobBuilder withConfigs(String configs) {
        this.configs = configs;
        return this;
    }

    public UploadJobCreator createJobCreator() {
        return new UploadJobCreator(jobParam, jobType, startDateTime, dayOfMonth, hourOfDay, minuteOfHour, dayOfWeek,
                cronExpression, configs);
    }

    public JobParam createJobParam() {
        Set<String> configSet = Optional.ofNullable(configs)
                .map(s-> Stream.of(s.split(",")).collect(toSet()))
                .orElseGet(Collections::emptySet);
        LocalDate scheduleDate = Optional.ofNullable(startDateTime).orElseGet(() -> null);
        return new JobParam(minuteOfHour, hourOfDay, dayOfMonth, dayOfWeek, cronExpression, jobType, scheduleDate,
                nextFireDateTime, configSet);
    }
}