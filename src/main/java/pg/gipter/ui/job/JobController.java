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
import pg.gipter.util.PropertiesHelper;
import pg.gipter.util.StringUtils;

import java.net.URL;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    private static Scheduler scheduler;
    private final PropertiesHelper propertiesHelper;

    public JobController(UILauncher uiLauncher) {
        super(uiLauncher);
        propertiesHelper = new PropertiesHelper();
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
                Properties data = propertiesHelper.loadDataProperties().orElseGet(Properties::new);
                Trigger trigger = createTriggerEveryWeek(data);
                JobDetail jobDetail = createJobDetail(data);

                if (!StringUtils.nullOrEmpty(cronExpressionTextField.getText())) {
                    trigger = createCronTrigger(data);
                    jobDetail = createJobDetail(data);
                } else if (everyMonthRadioButton.isSelected()) {
                    trigger = createTriggerEveryMonth(data);
                    jobDetail = createJobDetail(data);
                } else if (every2WeeksRadioButton.isSelected()) {
                    trigger = createTriggerEvery2Weeks(data);
                    jobDetail = createJobDetail(data);
                }
                propertiesHelper.saveDataProperties(data);
                jobDetail.getJobDataMap().put(UILauncher.class.getName(), uiLauncher);

                if (scheduler != null) {
                    scheduler.shutdown();
                }

                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.start();

                uiLauncher.setScheduler(scheduler);
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

    private Trigger createTriggerEveryMonth(Properties data) {
        String scheduleStart = startDatePicker.getValue().format(ApplicationProperties.yyyy_MM_dd);

        data.put(JobKey.TYPE.value(), JobType.EVERY_MONTH);
        data.put(JobKey.DAY_OF_MONTH.value(), dayOfMonthComboBox.getValue());
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.HOUR_OF_THE_DAY.value());
        data.remove(JobKey.DAY_OF_WEEK.value());

        return newTrigger()
                .withIdentity("everyMonthTrigger", "everyMonthTriggerGroup")
                .startNow()
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(
                        dayOfMonthComboBox.getValue(), hourOfDayComboBox.getValue(), minuteComboBox.getValue())
                )
                .build();
    }

    private Trigger createTriggerEvery2Weeks(Properties data) {
        String hourOfThDay = String.format("%s:%s", hourOfDayComboBox.getValue(), minuteComboBox.getValue());
        String scheduleStart = startDatePicker.getValue().format(ApplicationProperties.yyyy_MM_dd);

        data.put(JobKey.TYPE.value(), JobType.EVERY_2_WEEKS);
        data.put(JobKey.HOUR_OF_THE_DAY.value(), hourOfThDay);
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.remove(JobKey.DAY_OF_MONTH.value());
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.DAY_OF_WEEK.value());

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

    private Trigger createTriggerEveryWeek(Properties data) {
        String hourOfThDay = String.format("%s:%s", hourOfDayComboBox.getValue(), minuteComboBox.getValue());
        String scheduleStart = startDatePicker.getValue().format(ApplicationProperties.yyyy_MM_dd);

        data.put(JobKey.TYPE.value(), JobType.EVERY_WEEK);
        data.put(JobKey.DAY_OF_WEEK.value(), dayNameComboBox.getValue());
        data.put(JobKey.HOUR_OF_THE_DAY.value(), hourOfThDay);
        data.put(JobKey.SCHEDULE_START.value(), scheduleStart);
        data.remove(JobKey.CRON.value());
        data.remove(JobKey.DAY_OF_MONTH.value());

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

    private Trigger createCronTrigger(Properties data) throws ParseException {
        data.put(JobKey.TYPE.value(), JobType.CRON);
        data.put(JobKey.CRON.value(), cronExpressionTextField.getText());
        data.remove(JobKey.HOUR_OF_THE_DAY.value());
        data.remove(JobKey.DAY_OF_MONTH.value());
        data.remove(JobKey.DAY_OF_WEEK.value());
        data.remove(JobKey.SCHEDULE_START.value());

        CronExpression cronExpression = new CronExpression(cronExpressionTextField.getText());
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

        return newTrigger()
                .withIdentity("cronTrigger", "cronTriggerGroup")
                .startNow()
                .withSchedule(cronScheduleBuilder)
                .build();
    }

    private JobDetail createJobDetail(Properties data) {
        JobDataMap jobDataMap = new JobDataMap();
        for (JobKey key: JobKey.values()) {
            if (data.containsKey(key.value())) {
                jobDataMap.put(key.value(), data.getProperty(key.value()));
            }
        }
        return JobBuilder.newJob(GipterJob.class)
                .withIdentity(GipterJob.NAME, GipterJob.GROUP)
                .setJobData(jobDataMap)
                .build();
    }

}
