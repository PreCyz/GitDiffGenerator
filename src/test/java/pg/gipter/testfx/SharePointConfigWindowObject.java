package pg.gipter.testfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import pg.gipter.core.model.SharePointConfig;

import java.util.List;
import java.util.Optional;

public class SharePointConfigWindowObject {

    private final String usernameId = "#usernameTextField";
    private final String passwordId = "#passwordField";
    private final String domainId = "#domainTextField";
    private final String addButtonId = "#add-button";
    private final String removeButtonId = "#remove-button";
    private final String comboBoxId = ".combo-box";
    private final String sharePointLinkId = ".hyperlink";
    private final String urlId = "#urlTextField";
    private final String projectId = "#projectTextField";
    private final String listNameId = "#listNameTextField";

    private final FxRobot robot;

    public SharePointConfigWindowObject(FxRobot robot) {
        this.robot = robot;
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
        final ComboBox<SharePointConfig> comboBox = robot.lookup(comboBoxId).queryAs(ComboBox.class);
        final FxRobotInterface fxRobotInterface = robot.clickOn(comboBox);
        for (int idx = 1; idx <= entryIndex; ++idx) {
            fxRobotInterface.type(KeyCode.DOWN);
        }
        fxRobotInterface.type(KeyCode.ENTER);
        return this;
    }

    public int getComboBoxSize() {
        final ComboBox<SharePointConfig> comboBox = robot.lookup(comboBoxId).queryAs(ComboBox.class);
        return Optional.ofNullable(comboBox.getItems()).map(List::size).orElseGet(() -> 0);
    }

    public ObservableList<SharePointConfig> getComboBoxItems() {
        final ComboBox<SharePointConfig> comboBox = robot.lookup(comboBoxId).queryAs(ComboBox.class);
        return Optional.ofNullable(comboBox.getItems()).orElseGet(FXCollections::observableArrayList);
    }

    public String getSharePointLink() {
        final Hyperlink sharePointLink = robot.lookup(sharePointLinkId).queryAs(Hyperlink.class);
        return sharePointLink.getText();
    }

    public String getUsername() {
        final TextField username = robot.lookup(usernameId).queryAs(TextField.class);
        return username.getText();
    }

    public String getPassword() {
        final PasswordField username = robot.lookup(passwordId).queryAs(PasswordField.class);
        return username.getText();
    }

    public String getDomain() {
        final TextField domain = robot.lookup(domainId).queryAs(TextField.class);
        return domain.getText();
    }

    public String getUrl() {
        final TextField url = robot.lookup(urlId).queryAs(TextField.class);
        return url.getText();
    }

    public String getProject() {
        final TextField project = robot.lookup(projectId).queryAs(TextField.class);
        return project.getText();
    }

    public String getListNames() {
        final TextField listName = robot.lookup(listNameId).queryAs(TextField.class);
        return listName.getText();
    }
}
