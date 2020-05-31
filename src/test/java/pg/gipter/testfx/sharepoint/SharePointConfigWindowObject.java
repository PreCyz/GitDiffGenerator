package pg.gipter.testfx.sharepoint;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.testfx.AbstractWindowObject;

import java.util.List;
import java.util.Optional;

public class SharePointConfigWindowObject extends AbstractWindowObject {

    private final String nameId = "#nameTextField";
    private final String usernameId = "#usernameTextField";
    private final String passwordId = "#passwordField";
    private final String domainId = "#domainTextField";
    private final String addButtonId = "#add-button";
    private final String removeButtonId = "#remove-button";
    private final String urlId = "#urlTextField";
    private final String projectId = "#projectTextField";
    private final String listNameId = "#listNameTextField";
    private final String sharePointProjectsComboBoxId = "sharePointProjectsComboBox";
    private final String sharePointLinkId = "sharePointLink";

    public SharePointConfigWindowObject(FxRobot robot) {
        super(robot);
    }

    public SharePointConfigWindowObject writeName(String value) {
        clickAndWrite(nameId, value);
        return this;
    }

    public SharePointConfigWindowObject writeUsername(String value) {
        clickAndWrite(usernameId, value);
        return this;
    }

    private void clickAndWrite(String nodeId, String value) {
        robot.clickOn(nodeId);
        robot.write(value);
    }

    public SharePointConfigWindowObject writePassword(String value) {
        clickAndWrite(passwordId, value);
        return this;
    }

    public SharePointConfigWindowObject writeDomain(String value) {
        clickAndWrite(domainId, value);
        return this;
    }

    public SharePointConfigWindowObject clickAddButton() {
        final Button addConfigButton = robot.lookup(addButtonId).queryAs(Button.class);
        robot.clickOn(addConfigButton);
        return this;
    }

    public SharePointConfigWindowObject clickRemoveButton() {
        final Button removeConfiguration = robot.lookup(removeButtonId).queryAs(Button.class);
        robot.clickOn(removeConfiguration);
        return this;
    }

    public SharePointConfigWindowObject chooseComboBoxEntry(int entryIndex) {
        final ComboBox<SharePointConfig> comboBox = getComboBox(sharePointProjectsComboBoxId);
        final FxRobotInterface fxRobotInterface = robot.clickOn(comboBox);
        for (int idx = 1; idx <= entryIndex; ++idx) {
            fxRobotInterface.type(KeyCode.DOWN);
        }
        fxRobotInterface.type(KeyCode.ENTER);
        return this;
    }

    public int getComboBoxSize() {
        final ComboBox<SharePointConfig> comboBox = getComboBox(sharePointProjectsComboBoxId);
        return Optional.ofNullable(comboBox.getItems()).map(List::size).orElseGet(() -> 0);
    }

    public ObservableList<SharePointConfig> getComboBoxItems() {
        final ComboBox<SharePointConfig> comboBox = getComboBox(sharePointProjectsComboBoxId);
        return Optional.ofNullable(comboBox.getItems()).orElseGet(FXCollections::observableArrayList);
    }

    public String getSharePointLink() {
        final Hyperlink sharePointLink = getHyperLink(sharePointLinkId);
        return sharePointLink.getText();
    }

    public String getName() {
        return getTextFieldText(nameId);
    }

    private String getTextFieldText(String nameId) {
        final TextField name = robot.lookup(nameId).queryAs(TextField.class);
        return name.getText();
    }

    public String getUsername() {
        return getTextFieldText(usernameId);
    }

    public String getPassword() {
        final PasswordField username = robot.lookup(passwordId).queryAs(PasswordField.class);
        return username.getText();
    }

    public String getDomain() {
        return getTextFieldText(domainId);
    }

    public String getUrl() {
        return getTextFieldText(urlId);
    }

    public String getProject() {
        return getTextFieldText(projectId);
    }

    public String getListNames() {
        return getTextFieldText(listNameId);
    }
}
