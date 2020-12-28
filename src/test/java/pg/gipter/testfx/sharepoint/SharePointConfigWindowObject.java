package pg.gipter.testfx.sharepoint;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.testfx.api.FxRobot;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.testfx.BaseWindowObject;

import java.util.List;
import java.util.Optional;

public class SharePointConfigWindowObject extends BaseWindowObject {

    private final String nameId = "nameTextField";
    private final String usernameId = "usernameTextField";
    private final String passwordId = "passwordField";
    private final String domainId = "domainTextField";
    private final String addButtonId = "add-button";
    private final String removeButtonId = "remove-button";
    private final String urlId = "urlTextField";
    private final String projectId = "projectTextField";
    private final String listNameId = "listNameTextField";
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

    public SharePointConfigWindowObject writePassword(String value) {
        clickAndWrite(passwordId, value);
        return this;
    }

    public SharePointConfigWindowObject writeDomain(String value) {
        clickAndWrite(domainId, value);
        return this;
    }

    public SharePointConfigWindowObject clickAddButton() {
        final Button addConfigButton = getButton(addButtonId);
        robot.clickOn(addConfigButton);
        return this;
    }

    public SharePointConfigWindowObject clickRemoveButton() {
        final Button removeConfiguration = getButton(removeButtonId);
        robot.clickOn(removeConfiguration);
        return this;
    }

    public SharePointConfigWindowObject chooseComboBoxEntry(int entryIndex) {
        choseComboBoxEntry(sharePointProjectsComboBoxId, entryIndex);
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
        return getTextField(nameId).getText();
    }

    public String getUsername() {
        return getTextField(usernameId).getText();
    }

    public String getPassword() {
        return getPasswordField(passwordId).getText();
    }

    public String getDomain() {
        return getTextField(domainId).getText();
    }

    public String getUrl() {
        return getTextField(urlId).getText();
    }

    public String getProject() {
        return getTextField(projectId).getText();
    }

    public String getListNames() {
        return getTextField(listNameId).getText();
    }
}
