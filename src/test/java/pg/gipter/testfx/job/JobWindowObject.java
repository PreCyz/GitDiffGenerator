package pg.gipter.testfx.job;

import javafx.scene.control.ComboBox;
import org.testfx.api.FxRobot;
import pg.gipter.testfx.BaseWindowObject;

import java.time.DayOfWeek;
import java.util.List;

public class JobWindowObject extends BaseWindowObject {
    
    private final String cronExpressionTextFieldId = "cronExpressionTextField";
    private final String dayNameComboBoxId = "dayNameComboBox";
    private final String dayOfMonthComboBoxId = "dayOfMonthComboBox";
    private final String hourOfDayComboBoxId = "hourOfDayComboBox";
    private final String minuteComboBoxId = "minuteComboBox";
    private final String everyMonthRadioButtonId = "everyMonthRadioButton";
    private final String every2WeeksRadioButtonId = "every2WeeksRadioButton";
    private final String everyWeekRadioButtonId = "everyWeekRadioButton";
    private final String startDatePickerId = "startDatePicker";
    private final String configurationNameComboBoxId = "configurationNameComboBox";
    private final String scheduleButtonId = "scheduleButton";
    private final String jobTypeLabelId = "jobTypeLabel";
    private final String jobDetailsLabelId = "jobDetailsLabel";
    private final String cancelJobButtonId = "cancelJobButton";
    private final String lastExecutionLabelId = "lastExecutionLabel";
    private final String nextExecutionLabelId = "nextExecutionLabel";
    private final String configsLabelId = "configsLabel";

    protected JobWindowObject(FxRobot robot) {
        super(robot);
    }

    public List<String> getConfigurationNameComboBoxItems() {
        final ComboBox<String> comboBox = getComboBox(configurationNameComboBoxId);
        return comboBox.getItems();
    }

    public JobWindowObject pressEveryWeekRadioButton() {
        clickOnRadioButton(everyWeekRadioButtonId);
        return this;
    }

    public JobWindowObject chooseDayOfWeek(DayOfWeek dayOfWeek) {
        choseComboBoxEntry(dayNameComboBoxId, dayOfWeek);
        return this;
    }

    public JobWindowObject chooseHourOfTheDay(int hourOfTheDay) {
        choseComboBoxEntry(hourOfDayComboBoxId, Integer.valueOf(hourOfTheDay));
        return this;
    }

    public JobWindowObject chooseMinuteOfHour(int minuteOfHour) {
        choseComboBoxEntry(minuteComboBoxId, Integer.valueOf(minuteOfHour));
        return this;
    }

    public JobWindowObject chooseConfigEntry(int entryIndex) {
        choseComboBoxEntry(configurationNameComboBoxId, entryIndex);
        return this;
    }

    public JobWindowObject pressScheduleButton() {
        clickOnButton(scheduleButtonId);
        return this;
    }

    public String getConfigsLabelText() {
        return getLabel(configsLabelId).getText();
    }

    public String getJobDetailsLabelText() {
        return getLabel(jobDetailsLabelId).getText();
    }

    public String getJobTypeLabelText() {
        return getLabel(jobTypeLabelId).getText();
    }
}
