package pg.gipter.ui.job;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.util.StringUtils;

import java.net.URL;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.ResourceBundle;

import static org.quartz.TriggerBuilder.newTrigger;

public class JobController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @FXML
    private TextField cronExpressionTextField;
    @FXML
    private ComboBox<DayOfWeek> dayNameComboBox;
    @FXML
    private RadioButton everyMonthRadioButton;
    @FXML
    private RadioButton every2WeeksRadioButton;
    @FXML
    private RadioButton everyWeekRadioButton;

    private final ApplicationProperties applicationProperties;
    private static Scheduler scheduler;

    public JobController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        dayNameComboBox.setItems(FXCollections.observableList(new ArrayList<>(EnumSet.allOf(DayOfWeek.class))));
        dayNameComboBox.setValue(DayOfWeek.FRIDAY);
    }

    private EventHandler<ActionEvent> scheduleJobActionEvent() {
        return event -> {
            try {
                Trigger trigger;
                String triggrName;
                String triggrGroup;
                if (!StringUtils.nullOrEmpty(cronExpressionTextField.getText())) {
                    triggrName = "cronTrigger";
                    triggrGroup = "cronTriggerGroup";
                    trigger = createCronTrigger(triggrName, triggrGroup);
                } else if (everyMonthRadioButton.isSelected()) {
                    triggrName = "everyMonthTrigger";
                    triggrGroup = "everyMonthTriggerGroup";
                    trigger = createTriggerEveryMonth(triggrName, triggrGroup);
                } else if (every2WeeksRadioButton.isSelected()) {
                    triggrName = "every2WeeksTrigger";
                    triggrGroup = "every2WeeksTriggerGroup";
                    trigger = createTriggerEvery2Weeks(triggrName, triggrGroup);
                } else {
                    triggrName = "everyWeekTrigger";
                    triggrGroup = "everyWeekTriggerGroup";
                    trigger = createTriggerEveryWeek(triggrName, triggrGroup);
                }

                // Grab the Scheduler instance from the Factory
                if (scheduler == null) {
                    scheduler = StdSchedulerFactory.getDefaultScheduler();
                    scheduler.scheduleJob(trigger);
                } else {
                    scheduler.rescheduleJob(TriggerKey.triggerKey(triggrName, triggrGroup), trigger);
                }

                // and start it off
                scheduler.start();



                //scheduler.shutdown();

            } catch (SchedulerException se) {
                se.printStackTrace();
            }
        };
    }

    private Trigger createTriggerEveryMonth(String triggrName, String triggrGroup) {
        return newTrigger()
                .withIdentity(triggrName, triggrGroup)
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 15, 30))
                .forJob(createJobDetail())
                .build();
    }

    private Trigger createTriggerEvery2Weeks(String triggrName, String triggrGroup) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggrName, triggrGroup)
                .startAt(DateBuilder.tomorrowAt(15, 0, 0))  // first fire time 15:00:00 tomorrow
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(14 * 24) // interval is actually set at 14 * 24 hours' worth of milliseconds
                        .repeatForever())
                .forJob(createJobDetail())
                .build();
    }

    private Trigger createTriggerEveryWeek(String triggrName, String triggrGroup) {
        return newTrigger()
                .withIdentity(triggrName, triggrGroup)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 15 ? * WED")) // fire every wednesday at 15:00
                .forJob(createJobDetail())
                .build();
    }

    private Trigger createCronTrigger(String triggrName, String triggrGroup) {
        try {
            CronExpression cronExpression = new CronExpression(cronExpressionTextField.getText());
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            return newTrigger()
                    .withIdentity(triggrName, triggrGroup)
                    .startNow()
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
        return JobBuilder.newJob(GipterJob.class)
                .withIdentity(GipterJob.NAME, GipterJob.GROUP)
                .build();
    }

}
