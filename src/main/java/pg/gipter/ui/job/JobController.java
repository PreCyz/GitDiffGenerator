package pg.gipter.ui.job;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.quartz.TriggerBuilder.newTrigger;

public class JobController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @FXML
    private TextField cronExpressionTextField;
    @FXML
    private ComboBox<DayOfWeek> dayNameComboBox;
    @FXML
    private ComboBox<Integer> dayOfMonthComboBox;
    @FXML
    private ComboBox<Integer> hourOfDayComboBox;
    @FXML
    private RadioButton everyMonthRadioButton;
    @FXML
    private RadioButton every2WeeksRadioButton;
    @FXML
    private RadioButton everyWeekRadioButton;
    @FXML
    private Button scheduleButton;
    @FXML
    private ToggleGroup triggerOptionsToggleGroup;

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
        dayOfMonthComboBox.setItems(FXCollections.observableList(IntStream.range(1, 29).boxed().collect(toList())));
        dayOfMonthComboBox.setValue(Integer.valueOf(1));
        hourOfDayComboBox.setItems(FXCollections.observableList(IntStream.range(7, 18).boxed().collect(toList())));
        hourOfDayComboBox.setValue(Integer.valueOf(7));
        scheduleButton.setOnAction(scheduleJobActionEvent());
    }

    private EventHandler<ActionEvent> scheduleJobActionEvent() {
        return event -> {
            try {
                Trigger trigger;
                String triggerName;
                String triggerGroup;
                if (!StringUtils.nullOrEmpty(cronExpressionTextField.getText())) {
                    triggerName = "cronTrigger";
                    triggerGroup = "cronTriggerGroup";
                    trigger = createCronTrigger(triggerName, triggerGroup);
                } else if (everyMonthRadioButton.isSelected()) {
                    triggerName = "everyMonthTrigger";
                    triggerGroup = "everyMonthTriggerGroup";
                    trigger = createTriggerEveryMonth(triggerName, triggerGroup);
                } else if (every2WeeksRadioButton.isSelected()) {
                    triggerName = "every2WeeksTrigger";
                    triggerGroup = "every2WeeksTriggerGroup";
                    trigger = createTriggerEvery2Weeks(triggerName, triggerGroup);
                } else {
                    triggerName = "everyWeekTrigger";
                    triggerGroup = "everyWeekTriggerGroup";
                    trigger = createTriggerEveryWeek(triggerName, triggerGroup);
                }

                // Grab the Scheduler instance from the Factory
                if (scheduler == null) {
                    scheduler = StdSchedulerFactory.getDefaultScheduler();
                    scheduler.scheduleJob(trigger);
                } else {
                    scheduler.rescheduleJob(TriggerKey.triggerKey(triggerName, triggerGroup), trigger);
                }

                // and start it off
                scheduler.start();



                //scheduler.shutdown();

            } catch (SchedulerException | ParseException se) {
                se.printStackTrace();
            }
        };
    }

    private Trigger createTriggerEveryMonth(String triggerName, String triggerGroup) {
        return newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 15, 30))
                .forJob(createJobDetail())
                .build();
    }

    private Trigger createTriggerEvery2Weeks(String triggerName, String triggerGroup) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startAt(DateBuilder.tomorrowAt(15, 0, 0))  // first fire time 15:00:00 tomorrow
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(14 * 24) // interval is actually set at 14 * 24 hours' worth of milliseconds
                        .repeatForever())
                .forJob(createJobDetail())
                .build();
    }

    private Trigger createTriggerEveryWeek(String triggerName, String triggerGroup) {
        return newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 15 ? * WED")) // fire every wednesday at 15:00
                .forJob(createJobDetail())
                .build();
    }

    private Trigger createCronTrigger(String triggerName, String triggerGroup) throws ParseException {
        CronExpression cronExpression = new CronExpression(cronExpressionTextField.getText());
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

        return newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(cronScheduleBuilder)
                .forJob(createJobDetail())
                .build();
    }

    private JobDetail createJobDetail() {
        // define the job and tie it to our HelloJob class
        return JobBuilder.newJob(GipterJob.class)
                .withIdentity(GipterJob.NAME, GipterJob.GROUP)
                .build();
    }

}
