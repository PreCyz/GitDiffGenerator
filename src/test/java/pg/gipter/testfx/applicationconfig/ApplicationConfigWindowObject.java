package pg.gipter.testfx.applicationconfig;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import pg.gipter.core.PreferredArgSource;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationConfigWindowObject {

    private CheckBox useUICheckBox;
    private CheckBox activateTrayCheckBox;
    private CheckBox autostartCheckBox;
    private CheckBox silentModeCheckBox;
    private ComboBox<String> languageComboBox;

    private final String comboBoxId = ".combo-box";
    private final String languageId = "languageComboBox";
    private final String languageLabelId = "#languageLabel";
    private final String activateTrayCheckBoxId = "#activateTrayCheckBox";
    private final String confirmationWindowCheckBoxId = "#confirmationWindowCheckBox";
    private final String autostartCheckBoxId = "#autostartCheckBox";
    private final String preferredArgSourceComboBoxId = "preferredArgSourceComboBox";
    private final String useUICheckBoxId = "#useUICheckBox";

    private final FxRobot robot;

    public ApplicationConfigWindowObject(FxRobot robot) {
        this.robot = robot;
    }

    public ApplicationConfigWindowObject chooseLanguage(String language) {
        final ComboBox<String> comboBox = getComboBox(languageId);

        int itemIdx = 0;
        boolean search = true;
        final Iterator<String> langIterator = comboBox.getItems().iterator();
        while (langIterator.hasNext() && search) {
            itemIdx++;
            final String lang = langIterator.next();
            search = !lang.equalsIgnoreCase(language);
        }
        final FxRobotInterface fxRobotInterface = robot.clickOn(comboBox);
        for (int idx = 1; idx <= itemIdx; ++idx) {
            fxRobotInterface.type(KeyCode.DOWN);
        }
        fxRobotInterface.type(KeyCode.ENTER);
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> ComboBox<T> getComboBox(String id) {
        final Set<ComboBox> comboBoxes = robot.lookup(comboBoxId).queryAllAs(ComboBox.class);
        return (ComboBox<T>) Optional.of(
                comboBoxes.stream()
                        .filter(cb -> id.equals(cb.getId()))
                        .collect(Collectors.toCollection(LinkedList::new))
        ).map(LinkedList::getFirst).orElseThrow(NoSuchElementException::new);
    }

    public String getLanguageLabelText() {
        final Label label = robot.lookup(languageLabelId).queryAs(Label.class);
        return label.getText();
    }

    public ApplicationConfigWindowObject selectActivateTray() {
        selectCheckBox(activateTrayCheckBoxId);
        return this;
    }

    public ApplicationConfigWindowObject deselectActivateTray() {
        deselectCheckBox(activateTrayCheckBoxId);
        return this;
    }

    public ApplicationConfigWindowObject selectConfirmationWindow() {
        selectCheckBox(confirmationWindowCheckBoxId);
        return this;
    }

    private void selectCheckBox(String checkBoxId) {
        final CheckBox checkBox = robot.lookup(checkBoxId).queryAs(CheckBox.class);
        robot.clickOn(checkBox);
        if (!checkBox.isSelected()) {
            robot.clickOn(checkBox);
        }
    }

    public ApplicationConfigWindowObject deselectConfirmationWindow() {
        deselectCheckBox(confirmationWindowCheckBoxId);
        return this;
    }

    private void deselectCheckBox(String checkBoxId) {
        final CheckBox checkBox = robot.lookup(checkBoxId).queryAs(CheckBox.class);
        robot.clickOn(checkBox);
        if (checkBox.isSelected()) {
            robot.clickOn(checkBox);
        }
    }

    public CheckBox getActiveTrayCheckBox() {
        return robot.lookup(activateTrayCheckBoxId).queryAs(CheckBox.class);
    }

    public CheckBox getAutostartCheckBox() {
        return robot.lookup(autostartCheckBoxId).queryAs(CheckBox.class);
    }

    public CheckBox getUseUICheckBox() {
        return robot.lookup(useUICheckBoxId).queryAs(CheckBox.class);
    }

    public ComboBox<PreferredArgSource> getPreferredArgSourceCheckbox() {
        return getComboBox(preferredArgSourceComboBoxId);
    }
}
