package pg.gipter.testfx.applicationconfig;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.testfx.AbstractWindowObject;

import java.util.Iterator;

public class ApplicationConfigWindowObject extends AbstractWindowObject {

    private final String languageId = "languageComboBox";
    private final String languageLabelId = "#languageLabel";
    private final String activateTrayCheckBoxId = "#activateTrayCheckBox";
    private final String confirmationWindowCheckBoxId = "#confirmationWindowCheckBox";
    private final String autostartCheckBoxId = "#autostartCheckBox";
    private final String preferredArgSourceComboBoxId = "preferredArgSourceComboBox";
    private final String useUICheckBoxId = "#useUICheckBox";

    public ApplicationConfigWindowObject(FxRobot robot) {
        super(robot);
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
