package pg.gipter.ui.job;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.core.model.RunConfig;
import pg.gipter.jobs.*;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class JobController extends AbstractController {

    private static final String NOT_AVAILABLE = "N/A";
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
    private final String ALL_CONFIGS = "all-configs";

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
        initValues();
        runConfigMap = applicationProperties.getRunConfigMap();
        if (!runConfigMap.isEmpty() && !runConfigMap.containsKey(ArgName.configurationName.defaultValue())) {
            ObservableList<String> items = FXCollections.observableArrayList(runConfigMap.keySet());
            items.add(0, ALL_CONFIGS);
            configurationNameComboBox.setItems(items);
            configurationNameComboBox.setValue(configurationNameComboBox.getItems().get(0));
            configsLabel.setText(String.join(JobService.CONFIG_DELIMITER, runConfigMap.keySet()));
        }
        setDefaultsForJobDetailsControls();
        final ProgramData programData = dataDao.readProgramData();
        JobParam jobParam = programData.getJobParam();
        if (jobParam != null) {
            setDefinedJobDetails(jobParam);
            setExecutionDetails(programData);
        } else {
            everyWeekSwitch();
            clearInfo();
        }
    }

    private void clearInfo() {
        jobTypeLabel.setText(NOT_AVAILABLE);
        jobDetailsLabel.setText(NOT_AVAILABLE);
        configsLabel.setText(NOT_AVAILABLE);
    }

    private void initValues() {
        dayNameComboBox.setItems(FXCollections.observableList(new ArrayList<>(EnumSet.allOf(DayOfWeek.class))));
        dayNameComboBox.setValue(DayOfWeek.FRIDAY);
        dayOfMonthComboBox.setItems(FXCollections.observableList(IntStream.range(1, 29).boxed().collect(toList())));
        dayOfMonthComboBox.setValue(dayOfMonthComboBox.getItems().get(0));
        hourOfDayComboBox.setItems(FXCollections.observableList(IntStream.range(7, 24).boxed().collect(toList())));
        hourOfDayComboBox.setValue(hourOfDayComboBox.getItems().get(0));
        minuteComboBox.setItems(FXCollections.observableList(IntStream.range(0, 60).boxed().collect(toList())));
        minuteComboBox.setValue(minuteComboBox.getItems().get(0));
        startDatePicker.setValue(LocalDate.now());
    }

    private void setExecutionDetails(ProgramData programData) {
        if (programData.getUploadStatus() != null && programData.getLastUploadDateTime() != null) {
            String uploadInfo = String.format("%s [%s]",
                    programData.getLastUploadDateTime().format(DaoConstants.DATE_TIME_FORMATTER),
                    programData.getUploadStatus().name()
            );
            lastExecutionLabel.setText(BundleUtils.getMsg("tray.item.lastUpdate", uploadInfo));
        }
        final JobParam jobParam = programData.getJobParam();
        if (jobParam.getNextFireDate() != null) {
            nextExecutionLabel.setText(BundleUtils.getMsg(
                    "tray.item.nextUpdate",
                    jobParam.getNextFireDate().format(DaoConstants.DATE_TIME_FORMATTER)
            ));
        }
    }

    private void setDefinedJobDetails(JobParam jobParam) {
        if (jobParam.getJobType() != null) {
            cancelJobButton.setVisible(true);
            updateJobDetails(jobParam);
            switch (jobParam.getJobType()) {
                case EVERY_MONTH:
                    everyMonthRadioButton.setSelected(true);
                    hourOfDayComboBox.setValue(jobParam.getHourOfDay());
                    minuteComboBox.setValue(jobParam.getMinuteOfHour());
                    startDatePicker.setValue(jobParam.getScheduleStart());
                    dayOfMonthComboBox.setValue(jobParam.getDayOfMonth());

                    everyMonthSwitch();
                    break;
                case EVERY_2_WEEKS:
                    every2WeeksRadioButton.setSelected(true);
                    hourOfDayComboBox.setValue(jobParam.getHourOfDay());
                    minuteComboBox.setValue(jobParam.getMinuteOfHour());
                    startDatePicker.setValue(jobParam.getScheduleStart());

                    every2WeeksSwitch();
                    break;
                case EVERY_WEEK:
                    everyWeekRadioButton.setSelected(true);
                    hourOfDayComboBox.setValue(jobParam.getHourOfDay());
                    minuteComboBox.setValue(jobParam.getMinuteOfHour());
                    startDatePicker.setValue(jobParam.getScheduleStart());
                    dayNameComboBox.setValue(jobParam.getDayOfWeek());

                    everyWeekSwitch();
                    break;
                default:
                    break;
            }
        }
    }

    private void everyMonthSwitch() {
        startDatePicker.setDisable(true);
        dayNameComboBox.setDisable(true);
        dayOfMonthComboBox.setDisable(false);
    }

    private void every2WeeksSwitch() {
        startDatePicker.setDisable(false);
        dayNameComboBox.setDisable(true);
        dayOfMonthComboBox.setDisable(true);
    }

    private void everyWeekSwitch() {
        startDatePicker.setDisable(true);
        dayNameComboBox.setDisable(false);
        dayOfMonthComboBox.setDisable(true);
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
            initValues();
            clearInfo();
        };
    }

    private void setDefaultsForJobDetailsControls() {
        jobTypeLabel.setText(NOT_AVAILABLE);
        jobDetailsLabel.setText(NOT_AVAILABLE);
        cancelJobButton.setVisible(false);
        nextExecutionLabel.setText("");
        if (runConfigMap.isEmpty()) {
            configsLabel.setText(NOT_AVAILABLE);
        } else {
            configsLabel.setText(String.join(JobService.CONFIG_DELIMITER, runConfigMap.keySet()));
        }
    }

    private void setProperties() {
        startDatePicker.setConverter(startDateConverter());
        startDatePicker.setDayCellFactory(datePickerDateCellCallback());
        scheduleButton.setDisable(runConfigMap.isEmpty());
    }

    private Callback<DatePicker, DateCell> datePickerDateCellCallback() {
        return picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today));
            }
        };
    }

    private EventHandler<ActionEvent> everyMonthAction() {
        return event -> {
            everyMonthSwitch();
            jobTypeLabel.setText(JobType.EVERY_MONTH.name());
            UploadItemJobBuilder builder = new UploadItemJobBuilder()
                    .withJobType(JobType.EVERY_MONTH)
                    .withStartDate(startDatePicker.getValue())
                    .withDayOfMonth(dayOfMonthComboBox.getValue())
                    .withHourOfDay(hourOfDayComboBox.getValue())
                    .withMinuteOfHour(minuteComboBox.getValue())
                    .withDayOfWeek(dayNameComboBox.getValue())
                    .withCronExpression(cronExpressionTextField.getText())
                    .withConfigs(configsLabel.getText());
            updateJobDetails(builder.createJobParam());
        };
    }

    private EventHandler<ActionEvent> every2WeeksActionEvent() {
        return event -> {
            every2WeeksSwitch();
            jobTypeLabel.setText(JobType.EVERY_2_WEEKS.name());
            UploadItemJobBuilder builder = new UploadItemJobBuilder()
                    .withJobType(JobType.EVERY_2_WEEKS)
                    .withStartDate(startDatePicker.getValue())
                    .withDayOfMonth(dayOfMonthComboBox.getValue())
                    .withHourOfDay(hourOfDayComboBox.getValue())
                    .withMinuteOfHour(minuteComboBox.getValue())
                    .withDayOfWeek(dayNameComboBox.getValue())
                    .withCronExpression(cronExpressionTextField.getText())
                    .withConfigs(configsLabel.getText());
            updateJobDetails(builder.createJobParam());
        };
    }

    private EventHandler<ActionEvent> everyWeekActionEvent() {
        return event -> {
            everyWeekSwitch();
            jobTypeLabel.setText(JobType.EVERY_WEEK.name());
            UploadItemJobBuilder builder = new UploadItemJobBuilder()
                    .withJobType(JobType.EVERY_WEEK)
                    .withStartDate(startDatePicker.getValue())
                    .withDayOfMonth(dayOfMonthComboBox.getValue())
                    .withHourOfDay(hourOfDayComboBox.getValue())
                    .withMinuteOfHour(minuteComboBox.getValue())
                    .withDayOfWeek(dayNameComboBox.getValue())
                    .withCronExpression(cronExpressionTextField.getText())
                    .withConfigs(configsLabel.getText());
            updateJobDetails(builder.createJobParam());
        };
    }

    private StringConverter<LocalDate> startDateConverter() {
        return new StringConverter<>() {
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
                Map<String, Object> additionalJobParams = new HashMap<>();
                additionalJobParams.put(UILauncher.class.getName(), uiLauncher);

                String configName = configurationNameComboBox.getSelectionModel().getSelectedItem();
                if (ALL_CONFIGS.equals(configName)) {
                    configName = String.join(JobService.CONFIG_DELIMITER, runConfigMap.keySet());
                }
                UploadItemJobBuilder builder = new UploadItemJobBuilder()
                        .withJobType(calculateJobType())
                        .withStartDate(startDatePicker.getValue())
                        .withDayOfMonth(dayOfMonthComboBox.getValue())
                        .withHourOfDay(hourOfDayComboBox.getValue())
                        .withMinuteOfHour(minuteComboBox.getValue())
                        .withDayOfWeek(dayNameComboBox.getValue())
                        .withCronExpression(cronExpressionTextField.getText())
                        .withConfigs(configName)
                        .withAdditionalParams(additionalJobParams);

                JobService jobService = uiLauncher.getJobService();
                final JobCreator uploadJobCreator = builder.createJobCreator();
                jobService.scheduleJob(uploadJobCreator);
                dataDao.saveJobParam(jobService.getJobParam(uploadJobCreator));

                uiLauncher.hideJobWindow();
                uiLauncher.updateTray();

            } catch (SchedulerException se) {
                logger.error("Error when creating a job.", se);
                String errorMsg = BundleUtils.getMsg("popup.job.errorMsg", se.getMessage());
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(errorMsg)
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.ERROR)
                        .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
            }
        };
    }

    private JobType calculateJobType() {
        JobType jobType = JobType.EVERY_WEEK;
        if (!StringUtils.nullOrEmpty(cronExpressionTextField.getText())) {
            jobType = JobType.CRON;
        } else if (everyMonthRadioButton.isSelected()) {
            jobType = JobType.EVERY_MONTH;
        } else if (every2WeeksRadioButton.isSelected()) {
            jobType = JobType.EVERY_2_WEEKS;
        }
        return jobType;
    }

    private void setListeners() {
        configurationNameComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    if (ALL_CONFIGS.equals(newValue)) {
                        configsLabel.setText(String.join(JobService.CONFIG_DELIMITER, runConfigMap.keySet()));
                    } else if (ALL_CONFIGS.equals(oldValue)) {
                        configsLabel.setText(newValue);
                    } else {
                        Set<String> currentSelection = Stream.of(configsLabel.getText().split(JobService.CONFIG_DELIMITER))
                                .filter(v -> !v.isEmpty())
                                .collect(toCollection(LinkedHashSet::new));
                        currentSelection.add(newValue);
                        configsLabel.setText(String.join(JobService.CONFIG_DELIMITER, currentSelection));
                    }
                });
        dayNameComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, dayOfWeek, newDayOfWeek) -> {
                    UploadItemJobBuilder builder = new UploadItemJobBuilder()
                            .withJobType(calculateJobType())
                            .withStartDate(startDatePicker.getValue())
                            .withDayOfMonth(dayOfMonthComboBox.getValue())
                            .withHourOfDay(hourOfDayComboBox.getValue())
                            .withMinuteOfHour(minuteComboBox.getValue())
                            .withDayOfWeek(newDayOfWeek)
                            .withCronExpression(cronExpressionTextField.getText())
                            .withConfigs(configsLabel.getText());
                    updateJobDetails(builder.createJobParam());
                });
        hourOfDayComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, hourOfDay, newHourOfDay) -> {
                    UploadItemJobBuilder builder = new UploadItemJobBuilder()
                            .withJobType(calculateJobType())
                            .withStartDate(startDatePicker.getValue())
                            .withDayOfMonth(dayOfMonthComboBox.getValue())
                            .withHourOfDay(newHourOfDay)
                            .withMinuteOfHour(minuteComboBox.getValue())
                            .withDayOfWeek(dayNameComboBox.getValue())
                            .withCronExpression(cronExpressionTextField.getText())
                            .withConfigs(configsLabel.getText());
                    updateJobDetails(builder.createJobParam());
                });
        minuteComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, minute, newMinute) -> {
                    UploadItemJobBuilder builder = new UploadItemJobBuilder()
                            .withJobType(calculateJobType())
                            .withStartDate(startDatePicker.getValue())
                            .withDayOfMonth(dayOfMonthComboBox.getValue())
                            .withHourOfDay(hourOfDayComboBox.getValue())
                            .withMinuteOfHour(newMinute)
                            .withDayOfWeek(dayNameComboBox.getValue())
                            .withCronExpression(cronExpressionTextField.getText())
                            .withConfigs(configsLabel.getText());
                    updateJobDetails(builder.createJobParam());
                });
        startDatePicker.valueProperty()
                .addListener((observableValue, localDate, newLocalDate) -> {
                    UploadItemJobBuilder builder = new UploadItemJobBuilder()
                            .withJobType(calculateJobType())
                            .withStartDate(newLocalDate)
                            .withDayOfMonth(dayOfMonthComboBox.getValue())
                            .withHourOfDay(hourOfDayComboBox.getValue())
                            .withMinuteOfHour(minuteComboBox.getValue())
                            .withDayOfWeek(dayNameComboBox.getValue())
                            .withCronExpression(cronExpressionTextField.getText())
                            .withConfigs(configsLabel.getText());
                    updateJobDetails(builder.createJobParam());
                });
        dayOfMonthComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, oldDayOfMonth, newDayOfMonth) -> {
                    UploadItemJobBuilder builder = new UploadItemJobBuilder()
                            .withJobType(calculateJobType())
                            .withStartDate(startDatePicker.getValue())
                            .withDayOfMonth(newDayOfMonth)
                            .withHourOfDay(hourOfDayComboBox.getValue())
                            .withMinuteOfHour(minuteComboBox.getValue())
                            .withDayOfWeek(dayNameComboBox.getValue())
                            .withCronExpression(cronExpressionTextField.getText())
                            .withConfigs(configsLabel.getText());
                    updateJobDetails(builder.createJobParam());
                });
    }

    private void updateJobDetails(JobParam jobParam) {
        String details = calculateDetails(jobParam);
        jobTypeLabel.setText(jobParam.getJobType().name());
        configsLabel.setText(jobParam.getConfigsStr());
        jobDetailsLabel.setText(details);
    }

    private static String calculateDetails(JobParam jobParam) {
        String details = "";
        switch (jobParam.getJobType()) {
            case CRON:
                details = String.format("CRON: %s", jobParam.getCronExpression());
                break;
            case EVERY_WEEK:
                details = String.format("SCHEDULE_START: %s%nDAY_OF_WEEK: %s%nHOUR_OF_THE_DAY: %d:%02d",
                        jobParam.getScheduleStart().format(DateTimeFormatter.ISO_DATE),
                        jobParam.getDayOfWeek(),
                        jobParam.getHourOfDay(),
                        jobParam.getMinuteOfHour()
                );
                break;
            case EVERY_2_WEEKS:
                details = String.format("SCHEDULE_START: %s%nHOUR_OF_THE_DAY: %d:%02d",
                        jobParam.getScheduleStart().format(DateTimeFormatter.ISO_DATE),
                        jobParam.getHourOfDay(),
                        jobParam.getMinuteOfHour()
                );
                break;
            case EVERY_MONTH:
                details = String.format("SCHEDULE_START: %s%nDAY_OF_MONTH: %s%nHOUR_OF_THE_DAY: %d:%02d",
                        jobParam.getScheduleStart().format(DateTimeFormatter.ISO_DATE),
                        jobParam.getDayOfMonth(),
                        jobParam.getHourOfDay(),
                        jobParam.getMinuteOfHour()
                );
                break;
        }
        return details;
    }

    public static LinkedList<String> jobTrayLabels(JobParam jobParam) {
        final String details = calculateDetails(jobParam);
        return Stream.of(details.split("\n")).collect(toCollection(LinkedList::new));
    }

}
