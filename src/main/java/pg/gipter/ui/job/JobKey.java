package pg.gipter.ui.job;

public enum JobKey {

    HOUR_OF_THE_DAY("job.hourOfTheDay"),
    DAY_OF_MONTH("job.dayOfMonth"),
    DAY_OF_WEEK("job.dayOfWeek"),
    CRON("job.cron"),
    TYPE("job.type"),
    SCHEDULE_START("job.scheduleStart");

    private String value;

    JobKey(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
