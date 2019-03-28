package pg.gipter.ui.job;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Properties;

public class JobCreatorBuilder {
    private Properties data;
    private JobType jobType;
    private LocalDate startDateTime;
    private int dayOfMonth;
    private int hourOfDay;
    private int minuteOfHour;
    private DayOfWeek dayOfWeek;
    private String cronExpression;

    public JobCreatorBuilder withData(Properties data) {
        this.data = data;
        return this;
    }

    public JobCreatorBuilder withJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    public JobCreatorBuilder withStartDateTime(LocalDate startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public JobCreatorBuilder withDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    public JobCreatorBuilder withHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
        return this;
    }

    public JobCreatorBuilder withMinuteOfHour(int minuteOfHour) {
        this.minuteOfHour = minuteOfHour;
        return this;
    }

    public JobCreatorBuilder withDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    public JobCreatorBuilder withCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public JobCreator createJobCreator() {
        return new JobCreator(data, jobType, startDateTime, dayOfMonth, hourOfDay, minuteOfHour, dayOfWeek, cronExpression);
    }
}