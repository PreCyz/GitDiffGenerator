package pg.gipter.testfx;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.*;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;

import java.util.*;

import static java.util.stream.Collectors.toCollection;

public class BaseWindowObject {

    protected final FxRobot robot;

    protected BaseWindowObject(FxRobot robot) {
        this.robot = robot;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T> ComboBox<T> getComboBox(String id) {
        final Set<ComboBox> comboBoxes = robot.lookup(".combo-box").queryAllAs(ComboBox.class);
        return (ComboBox<T>) getNode(comboBoxes, id);
    }

    private <T extends Node> T getNode(Set<T> nodes, String id) {
        final LinkedList<T> result = nodes.stream()
                .filter(cb -> id.equals(cb.getId()))
                .collect(toCollection(LinkedList::new));
        return Optional.of(result)
                .map(LinkedList::getFirst)
                .orElseThrow(NoSuchElementException::new);
    }

    protected Hyperlink getHyperLink(String id) {
        final Set<Hyperlink> hyperlinks = robot.lookup(".hyperlink").queryAllAs(Hyperlink.class);
        return getNode(hyperlinks, id);
    }

    protected Button getButton(String id) {
        final Set<Button> buttons = robot.lookup(".button").queryAllAs(Button.class);
        return getNode(buttons, id);
    }

    protected CheckBox getCheckBox(String id) {
        final Set<CheckBox> checkBoxes = robot.lookup(".check-box").queryAllAs(CheckBox.class);
        return getNode(checkBoxes, id);
    }

    protected DatePicker getDatePicker(String id) {
        final Set<DatePicker> datePickers = robot.lookup(".date-picker").queryAllAs(DatePicker.class);
        return getNode(datePickers, id);
    }

    protected void clickAndWrite(String nodeId, String value) {
        if (nodeId != null && !nodeId.startsWith("#")) {
            nodeId = "#" + nodeId;
        }
        robot.clickOn(nodeId);
        robot.write(value);
    }

    protected void choseComboBoxEntry(String comboBoxId, int entryIndex) {
        final ComboBox<?> comboBox = getComboBox(comboBoxId);
        final FxRobotInterface fxRobotInterface = robot.clickOn(comboBox);
        for (int idx = 1; idx <= entryIndex; ++idx) {
            fxRobotInterface.type(KeyCode.DOWN);
        }
        fxRobotInterface.type(KeyCode.ENTER);
    }

    protected <T> void choseComboBoxEntry(String comboBoxId, T entry) {
        final ComboBox<T> comboBox = getComboBox(comboBoxId);
        final FxRobotInterface fxRobotInterface = robot.clickOn(comboBox);

        returnToBeginning(comboBox, fxRobotInterface);

        for (T item : comboBox.getItems()) {
            if (item.equals(entry)) {
                break;
            }
            fxRobotInterface.type(KeyCode.DOWN);
        }
        fxRobotInterface.type(KeyCode.ENTER);
    }

    private <T> void returnToBeginning(ComboBox<T> comboBox, FxRobotInterface fxRobotInterface) {
        final int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
        for (int counter = 0; counter < selectedIndex; ++counter) {
            fxRobotInterface.type(KeyCode.UP);
        }
    }

    protected Stage getTopModalStage() {
        // Get a list of windows but ordered from top[0] to bottom[n] ones.
        // It is needed to get the first found modal window.
        final List<Window> allWindows = new ArrayList<>(robot.robotContext().getWindowFinder().listWindows());
        Collections.reverse(allWindows);

        return (Stage) allWindows
                .stream()
                .filter(window -> window instanceof Stage)
                .filter(window -> ((Stage) window).getModality() == Modality.APPLICATION_MODAL)
                .findFirst()
                .orElse(null);
    }

    protected void clickOnButton(String buttonId) {
        final Button button = getButton(buttonId);
        robot.clickOn(button);
    }

    protected void selectCheckBox(String checkBoxId) {
        final CheckBox checkBox = getCheckBox(checkBoxId);
        robot.clickOn(checkBox);
        if (!checkBox.isSelected()) {
            robot.clickOn(checkBox);
        }
    }

    protected void deselectCheckBox(String checkBoxId) {
        final CheckBox checkBox = getCheckBox(checkBoxId);
        robot.clickOn(checkBox);
        if (checkBox.isSelected()) {
            robot.clickOn(checkBox);
        }
    }

    protected Label getLabel(String id) {
        final Set<Label> labels = robot.lookup(".label").queryAllAs(Label.class);
        return getNode(labels, id);
    }

    protected TextField getTextField(String id) {
        final Set<TextField> textFields = robot.lookup(".text-field").queryAllAs(TextField.class);
        return getNode(textFields, id);
    }

    protected PasswordField getPasswordField(String id) {
        final Set<PasswordField> passwordFields = robot.lookup(".password-field").queryAllAs(PasswordField.class);
        return getNode(passwordFields, id);
    }

    protected void clickHyperLink(String hyperlinkId) {
        final Hyperlink hyperLink = getHyperLink(hyperlinkId);
        robot.clickOn(hyperLink);
    }

}
