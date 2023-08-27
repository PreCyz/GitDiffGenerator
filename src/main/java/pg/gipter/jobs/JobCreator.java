package pg.gipter.jobs;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

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
