package pg.gipter.jobs.upload;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Properties;

public class UploadItemJobBuilder {
    private Properties data;
    private JobType jobType;
    private LocalDate startDateTime;
    private int dayOfMonth;
    private int hourOfDay;
    private int minuteOfHour;
    private DayOfWeek dayOfWeek;
    private String cronExpression;
    private String configs;

    public UploadItemJobBuilder withData(Properties data) {
        this.data = data;
        return this;
    }

    public UploadItemJobBuilder withJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    public UploadItemJobBuilder withStartDateTime(LocalDate startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public UploadItemJobBuilder withDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    public UploadItemJobBuilder withHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
        return this;
    }

    public UploadItemJobBuilder withMinuteOfHour(int minuteOfHour) {
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
        return new UploadJobCreator(data, jobType, startDateTime, dayOfMonth, hourOfDay, minuteOfHour, dayOfWeek, cronExpression, configs);
    }
}