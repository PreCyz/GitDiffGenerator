package pg.gipter.testfx.applicationconfig;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.testfx.BaseWindowObject;

import java.util.Iterator;

public class ApplicationConfigWindowObject extends BaseWindowObject {

    private final String languageId = "languageComboBox";
    private final String languageLabelId = "languageLabel";
    private final String activateTrayCheckBoxId = "activateTrayCheckBox";
    private final String confirmationWindowCheckBoxId = "confirmationWindowCheckBox";
    private final String autostartCheckBoxId = "autostartCheckBox";
    private final String preferredArgSourceComboBoxId = "preferredArgSourceComboBox";
    private final String useUICheckBoxId = "useUICheckBox";
    private final String checkLastItemCheckBoxId = "checkLastItemCheckBox";

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
        final Label label = getLabel(languageLabelId);
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

    public ApplicationConfigWindowObject deselectConfirmationWindow() {
        deselectCheckBox(confirmationWindowCheckBoxId);
        return this;
    }

    public ApplicationConfigWindowObject selectCheckLastItem() {
        selectCheckBox(checkLastItemCheckBoxId);
        return this;
    }

    public ApplicationConfigWindowObject deselectCheckLastItem() {
        deselectCheckBox(checkLastItemCheckBoxId);
        return this;
    }

    public CheckBox getActiveTrayCheckBox() {
        return getCheckBox(activateTrayCheckBoxId);
    }

    public CheckBox getAutostartCheckBox() {
        return getCheckBox(autostartCheckBoxId);
    }

    public CheckBox getUseUICheckBox() {
        return getCheckBox(useUICheckBoxId);
    }

    public ComboBox<PreferredArgSource> getPreferredArgSourceCheckbox() {
        return getComboBox(preferredArgSourceComboBoxId);
    }

    public CheckBox getCheckLastItemCheckBox() {
        return getCheckBox(checkLastItemCheckBoxId);
    }
}
