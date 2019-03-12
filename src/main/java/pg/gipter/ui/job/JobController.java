package pg.gipter.ui.job;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

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
        scheduler = uiLauncher.getScheduler();
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
        startDatePicker.setValue(LocalDate.now());
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
                setDisable(empty || date.compareTo(today) < 0);
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
                JobType jobType = JobType.EVERY_WEEK;
                if (!StringUtils.nullOrEmpty(cronExpressionTextField.getText())) {
                    jobType = JobType.CRON;
                } else if (everyMonthRadioButton.isSelected()) {
                    jobType = JobType.EVERY_MONTH;
                } else if (every2WeeksRadioButton.isSelected()) {
                    jobType = JobType.EVERY_2_WEEKS;
                }

                Properties data = propertiesHelper.loadDataProperties().orElseGet(Properties::new);
                JobCreator jobCreator = new JobCreator(data, jobType, startDatePicker.getValue(),
                        dayOfMonthComboBox.getValue(), hourOfDayComboBox.getValue(), minuteComboBox.getValue(),
                        dayNameComboBox.getValue(), cronExpressionTextField.getText(), scheduler);

                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), uiLauncher);
                scheduler = jobCreator.scheduleJob(additionalJobParams);
                propertiesHelper.saveDataProperties(jobCreator.getDataProperties());

                uiLauncher.setScheduler(scheduler);
                uiLauncher.hideJobWindow();
                uiLauncher.updateTray();

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

}
