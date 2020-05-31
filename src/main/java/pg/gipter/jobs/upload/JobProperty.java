package pg.gipter.jobs.upload;

public enum JobProperty {

    HOUR_OF_THE_DAY("job.hourOfTheDay"),
    DAY_OF_MONTH("job.dayOfMonth"),
    DAY_OF_WEEK("job.dayOfWeek"),
    CRON("job.cron"),
    TYPE("job.type"),
    SCHEDULE_START("job.scheduleStart"),
    NEXT_FIRE_DATE("job.nextUploadDateTime"),
    CONFIGS("job.configurationNames");

    private String value;

    JobProperty(String value) {
        this.value = value;
    }

    public String key() {
        return value;
    }
}