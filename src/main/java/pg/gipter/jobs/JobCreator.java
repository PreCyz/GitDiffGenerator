package pg.gipter.jobs;

import org.quartz.*;

import java.text.ParseException;

public interface JobCreator {
    JobDetail create();

    void createTrigger() throws ParseException;

    Trigger getTrigger();

    JobKey getJobKey();

    TriggerKey getTriggerKey();

    void schedule(Scheduler scheduler);

    JobParam getJobParam();
}
