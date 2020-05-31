package pg.gipter.testfx.main;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.testfx.AbstractWindowObject;

public class MainWindowObject extends AbstractWindowObject {

    private final String jobButtonId = "jobButton";
    private final String executeButtonId = "executeButton";
    private final String executeAllButtonId = "executeAllButton";
    private final String addConfigurationButtonId = "addConfigurationButton";
    private final String saveConfigurationButtonId = "saveConfigurationButton";
    private final String projectPathButtonId = "projectPathButton";
    private final String removeConfigurationButtonId = "removeConfigurationButton";

    private final String configurationNameTextFieldId = "configurationNameTextField";
    private final String authorsTextFieldId = "authorsTextField";
    private final String committerEmailTextFieldId = "committerEmailTextField";
    private final String gitAuthorTextFieldId = "gitAuthorTextField";
    private final String mercurialAuthorTextFieldId = "mercurialAuthorTextField";
    private final String svnAuthorTextFieldId = "svnAuthorTextField";
    private final String toolkitProjectListNamesTextFieldId = "toolkitProjectListNamesTextField";

    private final String skipRemoteCheckBoxId = "skipRemoteCheckBox";
    private final String fetchAllCheckBoxId = "fetchAllCheckBox";
    private final String deleteDownloadedFilesCheckBoxId = "deleteDownloadedFilesCheckBox";

    private final String itemTypeComboBoxId = "itemTypeComboBox";
    private final String configurationNameComboBoxId = "configurationNameComboBox";

    private final String endDatePickerId = "endDatePicker";
    private final String startDatePickerId = "startDatePicker";

    public MainWindowObject(FxRobot robot) {
        super(robot);
    }

    public MainWindowObject pressJobButton() {
        clickOnButton(jobButtonId);
        return this;
    }

    public MainWindowObject pressAddConfigurationButton() {
        clickOnButton(addConfigurationButtonId);
        return this;
    }

    public MainWindowObject pressSaveButton() {
        clickOnButton(saveConfigurationButtonId);
        return this;
    }

    public MainWindowObject pressOkOnPopup() {
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
        return this;
    }

    public MainWindowObject pressRemoveConfigurationButton() {
        clickOnButton(removeConfigurationButtonId);
        return this;
    }

    public MainWindowObject enterConfigurationName(String value) {
        getConfigurationNameTextField().clear();
        clickAndWrite(configurationNameTextFieldId, value);
        return this;
    }

    public MainWindowObject enterAuthor(String value) {
        getAuthorsTextField().clear();
        clickAndWrite(authorsTextFieldId, value);
        return this;
    }

    public MainWindowObject enterListNames(String value) {
        getToolkitProjectListNamesTextField().clear();
        clickAndWrite(toolkitProjectListNamesTextFieldId, value);
        return this;
    }

    public MainWindowObject checkFetchAll() {
        selectCheckBox(fetchAllCheckBoxId);
        return this;
    }

    public MainWindowObject uncheckSkipRemote() {
        deselectCheckBox(skipRemoteCheckBoxId);
        return this;
    }

    public MainWindowObject chooseItemType(ItemType itemType) {
        choseComboBoxEntry(itemTypeComboBoxId, itemType);
        return this;
    }

    public MainWindowObject chooseConfiguration(String value) {
        choseComboBoxEntry(configurationNameComboBoxId, value);
        return this;
    }

    public Button getJobButton() {
        return getButton(jobButtonId);
    }

    public Button getExecuteButton() {
        return getButton(executeButtonId);
    }

    public Button getExecuteAllButton() {
        return getButton(executeAllButtonId);
    }

    public Button getProjectPathButton() {
        return getButton(projectPathButtonId);
    }

    public Button getAddConfigurationButton() {
        return getButton(addConfigurationButtonId);
    }

    public TextField getAuthorsTextField() {
        return getTextField(authorsTextFieldId);
    }

    public TextField getCommitterEmailTextField() {
        return getTextField(committerEmailTextFieldId);
    }

    public TextField getGitAuthorTextField() {
        return getTextField(gitAuthorTextFieldId);
    }

    public TextField getMercurialAuthorTextField() {
        return getTextField(mercurialAuthorTextFieldId);
    }

    public TextField getSvnAuthorTextField() {
        return getTextField(svnAuthorTextFieldId);
    }

    public TextField getToolkitProjectListNamesTextField() {
        return getTextField(toolkitProjectListNamesTextFieldId);
    }

    public CheckBox getSkipRemoteCheckBox() {
        return getCheckBox(skipRemoteCheckBoxId);
    }

    public CheckBox getFetchAllCheckBox() {
        return getCheckBox(fetchAllCheckBoxId);
    }

    public CheckBox getDeleteDownloadedFilesCheckBox() {
        return getCheckBox(deleteDownloadedFilesCheckBoxId);
    }

    public ComboBox<String> getConfigurationNameComboBox() {
        return getComboBox(configurationNameComboBoxId);
    }

    public DatePicker getEndDatePicker() {
        return getDatePicker(endDatePickerId);
    }

    public DatePicker getStartDatePicker() {
        return getDatePicker(startDatePickerId);
    }

    public TextField getConfigurationNameTextField() {
        return getTextField(configurationNameTextFieldId);
    }

    public ComboBox<ItemType> getItemTypeComboBox() {
        return getComboBox(itemTypeComboBoxId);
    }
}
