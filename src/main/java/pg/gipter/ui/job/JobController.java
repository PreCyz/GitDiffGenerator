package pg.gipter.ui.job;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
    private ComboBox<Integer> minuteComboBox;
    @FXML
    private RadioButton everyMonthRadioButton;
    @FXML
    private RadioButton every2WeeksRadioButton;
    @FXML
    private RadioButton everyWeekRadioButton;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private Button scheduleButton;

    private final ApplicationProperties applicationProperties;
    private static Scheduler scheduler;

    public JobController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setActions(resources);
        setProperties();
    }

    private void setInitValues() {
        dayNameComboBox.setItems(FXCollections.observableList(new ArrayList<>(EnumSet.allOf(DayOfWeek.class))));
        dayNameComboBox.setValue(DayOfWeek.FRIDAY);
        dayOfMonthComboBox.setItems(FXCollections.observableList(IntStream.range(1, 29).boxed().collect(toList())));
        dayOfMonthComboBox.setValue(dayOfMonthComboBox.getItems().get(0));
        hourOfDayComboBox.setItems(FXCollections.observableList(IntStream.range(7, 18).boxed().collect(toList())));
        hourOfDayComboBox.setValue(hourOfDayComboBox.getItems().get(0));
        minuteComboBox.setItems(FXCollections.observableList(IntStream.range(0, 60).boxed().collect(toList())));
        minuteComboBox.setValue(minuteComboBox.getItems().get(0));
        startDatePicker.setValue(LocalDate.now().plusDays(1));
    }

    private void setActions(ResourceBundle resources) {
        scheduleButton.setOnAction(scheduleJobActionEvent(resources));
        everyMonthRadioButton.setOnAction(everyMonthAction());
        every2WeeksRadioButton.setOnAction(every2WeeksActionEvent());
        everyWeekRadioButton.setOnAction(everyWeekActionEvent());
    }

    private void setProperties() {
        startDatePicker.setConverter(startDateConverter());
        startDatePicker.setDayCellFactory(datePickerDateCellCallback());
        dayOfMonthComboBox.setDisable(true);
    }

    private Callback<DatePicker, DateCell> datePickerDateCellCallback() {
        return picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) <= 0);
            }
        };
    }

    private EventHandler<ActionEvent> everyMonthAction() {
        return event -> {
            startDatePicker.setDisable(true);
            dayNameComboBox.setDisable(true);
            dayOfMonthComboBox.setDisable(false);
        };
    }

    private EventHandler<ActionEvent> every2WeeksActionEvent() {
        return event -> {
            startDatePicker.setDisable(false);
            dayNameComboBox.setDisable(true);
            dayOfMonthComboBox.setDisable(true);
        };
    }

    private EventHandler<ActionEvent> everyWeekActionEvent() {
        return event -> {
            startDatePicker.setDisable(false);
            dayNameComboBox.setDisable(false);
            dayOfMonthComboBox.setDisable(true);
        };
    }

    private StringConverter<LocalDate> startDateConverter() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate object) {
                return object != null ? object.format(ApplicationProperties.yyyy_MM_dd) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                return LocalDate.parse(string, ApplicationProperties.yyyy_MM_dd);
            }
        };
    }

    private EventHandler<ActionEvent> scheduleJobActionEvent(ResourceBundle resource) {
        return event -> {
            try {
                Trigger trigger = createTriggerEveryWeek();
                if (!StringUtils.nullOrEmpty(cronExpressionTextField.getText())) {
                    trigger = createCronTrigger();
                } else if (everyMonthRadioButton.isSelected()) {
                    trigger = createTriggerEveryMonth();
                } else if (every2WeeksRadioButton.isSelected()) {
                    trigger = createTriggerEvery2Weeks();
                }

                if (scheduler != null) {
                    scheduler.shutdown();
                }

                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.scheduleJob(createJobDetail(), trigger);
                scheduler.start();

                uiLauncher.hideJobWindow();

            } catch (SchedulerException | ParseException se) {
                logger.error("Error when creating a job.", se);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(resource.getString("popup.title"));
                alert.setHeaderText(resource.getString("popup.header.error"));
                alert.setContentText(resource.getString("popup.job.errorMsg"));

                AbstractController.setImageOnAlertWindow(alert);

                alert.showAndWait();
            }
        };
    }

    private Trigger testTrigger() {
        LocalDateTime now = LocalDateTime.now();

        Date startDate = DateBuilder.dateOf(
                now.getHour(), now.getMinute(), now.getSecond() + 30,
                now.getDayOfMonth(), now.getMonthValue(), now.getYear()
        );

        return newTrigger()
                .withIdentity("testTrigger", "testGroup")
                .startAt(startDate)
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(10))
                .build();
    }

    private Trigger createTriggerEveryMonth() {
        return newTrigger()
                .withIdentity("everyMonthTrigger", "everyMonthTriggerGroup")
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(
                        dayOfMonthComboBox.getValue(), hourOfDayComboBox.getValue(), minuteComboBox.getValue())
                )
                .build();
    }

    private Trigger createTriggerEvery2Weeks() {
        Date startDate = DateBuilder.dateOf(
                hourOfDayComboBox.getValue(), minuteComboBox.getValue(), 0,
                startDatePicker.getValue().getDayOfMonth(), startDatePicker.getValue().getMonthValue(), startDatePicker.getValue().getYear()
        );
        return TriggerBuilder.newTrigger()
                .withIdentity("every2WeeksTrigger", "every2WeeksTriggerGroup")
                .startAt(startDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(14 * 24) // interval is actually set at 14 * 24 hours' worth of milliseconds
                        .repeatForever())
                .build();
    }

    private Trigger createTriggerEveryWeek() {
        Date startDate = DateBuilder.dateOf(
                hourOfDayComboBox.getValue(), minuteComboBox.getValue(), 0,
                startDatePicker.getValue().getDayOfMonth(), startDatePicker.getValue().getMonthValue(), startDatePicker.getValue().getYear()
        );
        return newTrigger()
                .withIdentity("everyWeekTrigger", "everyWeekTriggerGroup")
                .startAt(startDate)
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(dayNameComboBox.getValue().getValue(), hourOfDayComboBox.getValue(), 0)) // fire every wednesday at 15:00
                .build();
    }

    private Trigger createCronTrigger() throws ParseException {
        CronExpression cronExpression = new CronExpression(cronExpressionTextField.getText());
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

        return newTrigger()
                .withIdentity("cronTrigger", "cronTriggerGroup")
                .startNow()
                .withSchedule(cronScheduleBuilder)
                .forJob(createJobDetail())
                .build();
    }

    private JobDetail createJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(GipterJob.APP_PROPS_KEY, applicationProperties);
        return JobBuilder.newJob(GipterJob.class)
                .withIdentity(GipterJob.NAME, GipterJob.GROUP)
                .setJobData(jobDataMap)
                .build();
    }

}
