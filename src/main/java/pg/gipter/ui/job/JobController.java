package pg.gipter.ui.job;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.model.RunConfig;
import pg.gipter.job.JobHandler;
import pg.gipter.job.upload.*;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.*;
import pg.gipter.utils.*;

import java.net.URL;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
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
    private ComboBox<String> configurationNameComboBox;
    @FXML
    private Button scheduleButton;
    @FXML
    private Label jobTypeLabel;
    @FXML
    private Label jobDetailsLabel;
    @FXML
    private Button cancelJobButton;
    @FXML
    private Label lastExecutionLabel;
    @FXML
    private Label nextExecutionLabel;
    @FXML
    private Label configsLabel;

    private Map<String, RunConfig> runConfigMap;
    private final DataDao dataDao;
    private final String NOT_AVAILABLE = "N/A";
    private final String ALL_CONFIGS = "all-configs";
    private final ApplicationProperties applicationProperties;

    public JobController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        dataDao = DaoFactory.getDataDao();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setActions();
        setProperties();
        setListeners();
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
        runConfigMap = applicationProperties.getRunConfigMap();
        if (!runConfigMap.isEmpty() && !runConfigMap.containsKey(ArgName.configurationName.defaultValue())) {
            ObservableList<String> items = FXCollections.observableArrayList(runConfigMap.keySet());
            items.add(0, ALL_CONFIGS);
            configurationNameComboBox.setItems(items);
            configurationNameComboBox.setValue(configurationNameComboBox.getItems().get(0));
            configsLabel.setText(String.join(UploadJobCreator.CONFIG_DELIMITER, runConfigMap.keySet()));
        }
        setDefaultsForJobDetailsControls();
        Optional<Properties> data = dataDao.loadDataProperties();
        if (data.isPresent()) {
            setDefinedJobDetails(data.get());
            setExecutionDetails(data.get());
        } else {
            lastExecutionLabel.setText("");
            nextExecutionLabel.setText("");
        }
    }

    private void setExecutionDetails(Properties data) {
        if (data.containsKey(DaoConstants.UPLOAD_STATUS_KEY) && data.containsKey(DaoConstants.UPLOAD_DATE_TIME_KEY)) {
            String uploadInfo = String.format("%s [%s]",
                    data.getProperty(DaoConstants.UPLOAD_DATE_TIME_KEY),
                    data.getProperty(DaoConstants.UPLOAD_STATUS_KEY)
            );
            lastExecutionLabel.setText(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
        }
        if (data.containsKey(JobProperty.NEXT_FIRE_DATE.key()) &&
                !StringUtils.nullOrEmpty(data.getProperty(JobProperty.NEXT_FIRE_DATE.key(), ""))) {
            nextExecutionLabel.setText(BundleUtils.getMsg(
                    "tray.item.nextUpdate",
                    data.getProperty(JobProperty.NEXT_FIRE_DATE.key())
            ));
        }
    }

    private void setDefinedJobDetails(Properties dataProp) {
        if (dataProp.containsKey(JobProperty.TYPE.key())) {
            cancelJobButton.setVisible(true);
            jobDetailsLabel.setAlignment(Pos.TOP_LEFT);
            JobType jobType = JobType.valueOf(dataProp.getProperty(JobProperty.TYPE.key()));
            jobTypeLabel.setText(jobType.name());
            String details;
            if (jobType == JobType.CRON) {
                details = BundleUtils.getMsg("job.cron.expression", dataProp.getProperty(JobProperty.CRON.key()));
            } else {
                details = buildLabel(dataProp, JobProperty.SCHEDULE_START).map(value -> value + "\n").orElse("");
                details += buildLabel(dataProp, JobProperty.DAY_OF_MONTH).map(value -> value + "\n").orElse("");
                details += buildLabel(dataProp, JobProperty.DAY_OF_WEEK).map(value -> value + "\n").orElse("");
                details += buildLabel(dataProp, JobProperty.HOUR_OF_THE_DAY).orElse("");
            }
            jobDetailsLabel.setText(details);
            configsLabel.setText(dataProp.getProperty(JobProperty.CONFIGS.key()));
        }
    }

    public static Optional<String> buildLabel(Properties data, JobProperty key) {
        if (data.containsKey(key.key())) {
            String value = data.getProperty(key.key());
            if (key == JobProperty.HOUR_OF_THE_DAY) {
                String minuetOfHour = value.substring(value.indexOf(":") + 1);
                if (minuetOfHour.length() == 1) {
                    value = value.substring(0, value.indexOf(":") + 1) + "0" + minuetOfHour;
                }
            }
            if (value.length() == 1 && Character.isDigit(value.charAt(0))) {
                value = "0" + value;
            }
            return Optional.of(String.format("%s: %s", key, value));
        }
        return Optional.empty();
    }

    private void setActions() {
        scheduleButton.setOnAction(scheduleJobActionEvent());
        everyMonthRadioButton.setOnAction(everyMonthAction());
        every2WeeksRadioButton.setOnAction(every2WeeksActionEvent());
        everyWeekRadioButton.setOnAction(everyWeekActionEvent());
        cancelJobButton.setOnAction(cancelJobActionEventHandler());
    }

    private EventHandler<ActionEvent> cancelJobActionEventHandler() {
        return event -> {
            uiLauncher.cancelJob();
            setDefaultsForJobDetailsControls();
        };
    }

    private void setDefaultsForJobDetailsControls() {
        jobTypeLabel.setText(NOT_AVAILABLE);
        jobTypeLabel.setAlignment(Pos.CENTER);
        jobDetailsLabel.setText(NOT_AVAILABLE);
        jobDetailsLabel.setAlignment(Pos.TOP_CENTER);
        cancelJobButton.setVisible(false);
        nextExecutionLabel.setText("");
        configsLabel.setAlignment(Pos.TOP_LEFT);
        if (runConfigMap.isEmpty()) {
            configsLabel.setText(NOT_AVAILABLE);
        } else {
            configsLabel.setText(String.join(UploadJobCreator.CONFIG_DELIMITER, runConfigMap.keySet()));
        }
    }

    private void setProperties() {
        startDatePicker.setConverter(startDateConverter());
        startDatePicker.setDayCellFactory(datePickerDateCellCallback());
        startDatePicker.setDisable(true);
        dayOfMonthComboBox.setDisable(true);
        scheduleButton.setDisable(runConfigMap.isEmpty());
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
            startDatePicker.setDisable(true);
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

    private EventHandler<ActionEvent> scheduleJobActionEvent() {
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

                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), uiLauncher);
                Properties data = dataDao.loadDataProperties().orElseGet(Properties::new);
                UploadItemJobBuilder builder = new UploadItemJobBuilder()
                        .withData(data)
                        .withJobType(jobType)
                        .withStartDateTime(startDatePicker.getValue())
                        .withDayOfMonth(dayOfMonthComboBox.getValue())
                        .withHourOfDay(hourOfDayComboBox.getValue())
                        .withMinuteOfHour(minuteComboBox.getValue())
                        .withDayOfWeek(dayNameComboBox.getValue())
                        .withCronExpression(cronExpressionTextField.getText())
                        .withConfigs(configsLabel.getText());

                JobHandler jobHandler = uiLauncher.getJobHandler();
                jobHandler.scheduleUploadJob(builder, additionalJobParams);
                dataDao.saveDataProperties(jobHandler.getDataProperties());

                uiLauncher.hideJobWindow();
                uiLauncher.updateTray();

            } catch (SchedulerException | ParseException se) {
                logger.error("Error when creating a job.", se);
                String errorMsg = BundleUtils.getMsg("popup.job.errorMsg", se.getMessage());
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(errorMsg)
                        .withLink(AlertHelper.logsFolder())
                        .withAlertType(Alert.AlertType.ERROR)
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withImage(ImageFile.ERROR_CHICKEN_PNG);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
            }
        };
    }

    private void setListeners() {
        configurationNameComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    if (ALL_CONFIGS.equals(newValue)) {
                        configsLabel.setText(String.join(UploadJobCreator.CONFIG_DELIMITER, runConfigMap.keySet()));
                    } else if (oldValue.equals(ALL_CONFIGS)) {
                        configsLabel.setText(newValue);
                    } else {
                        Set<String> currentSelection = Stream.of(configsLabel.getText().split(UploadJobCreator.CONFIG_DELIMITER))
                                .filter(v -> !v.isEmpty())
                                .collect(toCollection(LinkedHashSet::new));
                        currentSelection.add(newValue);
                        configsLabel.setText(String.join(UploadJobCreator.CONFIG_DELIMITER, currentSelection));
                    }
                });
    }

}
