package pg.gipter.ui.main;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

class AdditionalSettingsSectionController extends AbstractController {

    private CheckBox deleteDownloadedFilesCheckBox;
    private CheckBox skipRemoteCheckBox;
    private CheckBox fetchAllCheckBox;
    private TextField fetchTimeoutTextField;

    AdditionalSettingsSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    void initialize(URL location, ResourceBundle resources, Map<String, Control> controlsMap) {
        super.initialize(location, resources);

        deleteDownloadedFilesCheckBox = (CheckBox) controlsMap.get("deleteDownloadedFilesCheckBox");
        skipRemoteCheckBox = (CheckBox) controlsMap.get("skipRemoteCheckBox");
        fetchAllCheckBox = (CheckBox) controlsMap.get("fetchAllCheckBox");
        fetchTimeoutTextField = (TextField) controlsMap.get("fetchTimeoutTextField");

        setInitValues();
        setProperties();
        setListeners();
    }

    private void setInitValues() {
        skipRemoteCheckBox.setSelected(applicationProperties.isSkipRemote());
        fetchAllCheckBox.setSelected(applicationProperties.isFetchAll());
        deleteDownloadedFilesCheckBox.setSelected(applicationProperties.isDeleteDownloadedFiles());
        fetchTimeoutTextField.setText(String.valueOf(applicationProperties.fetchTimeout()));
    }

    private void setProperties() {
        deleteDownloadedFilesCheckBox.setDisable(!ItemType.isDocsRelated(applicationProperties.itemType()));
        fetchTimeoutTextField.setDisable(fetchAllCheckBox.isSelected() && !fetchAllCheckBox.isDisabled());
    }

    private void setListeners() {
        fetchAllCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fetchTimeoutTextField.setDisable(oldValue));
    }

    void disableDeleteDownloadedFiles(boolean disable) {
        deleteDownloadedFilesCheckBox.setDisable(disable);
    }

    void disableSkipRemote(boolean disable) {
        skipRemoteCheckBox.setDisable(disable);
    }

    void disableFetchAll(boolean disable) {
        fetchAllCheckBox.setDisable(disable);
        fetchTimeoutTextField.setDisable(disable);
    }

    public Boolean getDeleteDownloadedFiles() {
        return deleteDownloadedFilesCheckBox.isSelected();
    }

    public Boolean getSkipRemote() {
        return skipRemoteCheckBox.isSelected();
    }

    public Boolean getFetchAll() {
        return fetchAllCheckBox.isSelected();
    }

    public Integer getFetchTimeout() {
        try {
            return Integer.parseInt(fetchTimeoutTextField.getText());
        } catch (NumberFormatException ex) {
            return Integer.parseInt(ArgName.fetchTimeout.defaultValue());
        }
    }
}
