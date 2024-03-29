package pg.gipter.testfx.main;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.testfx.BaseWindowObject;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class MainWindowObject extends BaseWindowObject {

    private final String jobButtonId = "jobButton";
    private final String executeButtonId = "executeButton";
    private final String executeAllButtonId = "executeAllButton";
    private final String addConfigurationButtonId = "addConfigurationButton";
    private final String saveConfigurationButtonId = "saveConfigurationButton";
    private final String projectPathButtonId = "projectPathButton";
    private final String removeConfigurationButtonId = "removeConfigurationButton";
    private final String trayButtonButtonId = "trayButton";

    private final String authorsTextFieldId = "authorsTextField";
    private final String committerEmailTextFieldId = "committerEmailTextField";
    private final String gitAuthorTextFieldId = "gitAuthorTextField";
    private final String mercurialAuthorTextFieldId = "mercurialAuthorTextField";
    private final String svnAuthorTextFieldId = "svnAuthorTextField";
    private final String toolkitProjectListNamesTextFieldId = "toolkitProjectListNamesTextField";

    private final String skipRemoteCheckBoxId = "skipRemoteCheckBox";
    private final String fetchAllCheckBoxId = "fetchAllCheckBox";
    private final String fetchTimeoutTextFieldId = "fetchTimeoutTextField";
    private final String deleteDownloadedFilesCheckBoxId = "deleteDownloadedFilesCheckBox";

    private final String itemTypeComboBoxId = "itemTypeComboBox";
    private final String configurationNameComboBoxId = "configurationNameComboBox";

    private final String endDatePickerId = "endDatePicker";
    private final String startDatePickerId = "startDatePicker";

    private final String verifyCredentialsHyperlinkId = "verifyCredentialsHyperlink";

    private final String useDefaultAuthorCheckBoxId = "useDefaultAuthorCheckBox";
    private final String useDefaultEmailCheckBoxId = "useDefaultEmailCheckBox";

    public MainWindowObject(FxRobot robot) {
        super(robot);
    }

    public MainWindowObject pressJobButton() {
        clickOnButton(jobButtonId);
        return this;
    }

    public MainWindowObject pressAddConfigurationButton() {
        clickOnButton(addConfigurationButtonId);
        waitInSeconds(0.5d);
        return this;
    }

    public MainWindowObject pressSaveButton() {
        clickOnButton(saveConfigurationButtonId);
        return this;
    }

    public MainWindowObject pressOkOnPopup() {
        return pressEnter();
    }

    public MainWindowObject pressEnter() {
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
        return this;
    }

    public MainWindowObject enterTextInDialog(String text) {
        final Map<String, KeyCode> keyBoardMap = Arrays.stream(KeyCode.values())
                .collect(toMap(KeyCode::getName, keyCode -> keyCode, (v1, v2) -> v1));
        for (char c : text.toCharArray()) {
            final String letter = String.valueOf(c).toUpperCase();
            if (keyBoardMap.containsKey(letter)) {
                final KeyCode keyCode = keyBoardMap.get(letter);
                robot.press(keyCode).release(keyCode);
            }
        }
        return this;
    }

    public MainWindowObject pressRemoveConfigurationButton() {
        clickOnButton(removeConfigurationButtonId);
        return this;
    }

    public MainWindowObject pressTrayButton() {
        clickOnButton(trayButtonButtonId);
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

    public MainWindowObject uncheckFetchAll() {
        deselectCheckBox(fetchAllCheckBoxId);
        return this;
    }

    public MainWindowObject enterFetchTimeout(Integer value) {
        getFetchTimeoutTextField().clear();
        clickAndWrite(fetchTimeoutTextFieldId, value.toString());
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

    public MainWindowObject clickVerifyCredentialsHyperlink() {
        clickHyperLink(verifyCredentialsHyperlinkId);
        return this;
    }

    public MainWindowObject enterGitAuthor(String value) {
        clickAndWrite(gitAuthorTextFieldId, value);
        return this;
    }

    public MainWindowObject checkDefaultAuthor() {
        selectCheckBox(useDefaultAuthorCheckBoxId);
        return this;
    }

    public MainWindowObject uncheckDefaultAuthor() {
        deselectCheckBox(useDefaultAuthorCheckBoxId);
        return this;
    }

    public MainWindowObject checkDefaultEmail() {
        selectCheckBox(useDefaultEmailCheckBoxId);
        return this;
    }

    public MainWindowObject uncheckDefaultEmail() {
        deselectCheckBox(useDefaultEmailCheckBoxId);
        return this;
    }

    public MainWindowObject enterCommitterEmail(String value) {
        clickAndWrite(committerEmailTextFieldId, value);
        return this;
    }

    public MainWindowObject waitInSeconds(double seconds) {
        try {
            final double milliSeconds = 1000 * seconds;
            Thread.sleep((long) milliSeconds);
        } catch (InterruptedException e) {
            System.err.printf("Could not wait. Something went wrong %s\n", e.getMessage());
        }
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

    public Button getTrayButton() {
        return getButton(trayButtonButtonId);
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

    public TextField getFetchTimeoutTextField() {
        return getTextField(fetchTimeoutTextFieldId);
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

    public ComboBox<ItemType> getItemTypeComboBox() {
        return getComboBox(itemTypeComboBoxId);
    }

    public Hyperlink getVerifyCredentialsHyperLink() {
        return getHyperLink(verifyCredentialsHyperlinkId);
    }

    public CheckBox getUseDefaultAuthorCheckBox() {
        return getCheckBox(useDefaultAuthorCheckBoxId);
    }

    public CheckBox getUseDefaultEmailCheckBox() {
        return getCheckBox(useDefaultEmailCheckBoxId);
    }
}
