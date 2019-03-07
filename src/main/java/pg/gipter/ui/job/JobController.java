package pg.gipter.ui.job;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Runner;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.text.ParseException;
import java.util.ResourceBundle;

public class JobController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final ApplicationProperties applicationProperties;

    public JobController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }

    private EventHandler<ActionEvent> scheduleJobActionEvent() {
        return event -> {
            try {
                // Grab the Scheduler instance from the Factory
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

                // and start it off
                scheduler.start();

                Trigger trigger = createTrigger();
                scheduler.scheduleJob(trigger);

                scheduler.shutdown();

            } catch (SchedulerException se) {
                se.printStackTrace();
            }
        };
    }

    private Trigger createTrigger() {
        try {
            // Trigger the job to run now, and then repeat every 40 seconds
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(40)
                    .repeatForever();

            CronExpression cronExpression = new CronExpression("0 0/2 8-17 * * ?");
            //ScheduleBuilder<CronTrigger> cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            //CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.weeklyOnDayAndHourAndMinute(
                    DateBuilder.FRIDAY, 15, 30
            );

            return TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    //.withSchedule(scheduleBuilder)
                    .withSchedule(cronScheduleBuilder)
                    .forJob(createJobDetail())
                    .build();
        } catch (ParseException e) {
            logger.error("Wrong cron expression.");
        }
        return null;
    }

    private JobDetail createJobDetail() {
        // define the job and tie it to our HelloJob class
        return JobBuilder.newJob(HelloJob.class)
                .withIdentity("job1", "group1")
                .build();
    }

    private class HelloJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            Runner runner = new Runner(applicationProperties);
            runner.run();
        }
    }

}
