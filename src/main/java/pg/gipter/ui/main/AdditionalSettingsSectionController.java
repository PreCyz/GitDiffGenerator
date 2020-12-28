package pg.gipter.ui.main;

import javafx.scene.control.CheckBox;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.*;

class AdditionalSettingsSectionController extends AbstractController {

    private CheckBox deleteDownloadedFilesCheckBox;
    private CheckBox skipRemoteCheckBox;
    private CheckBox fetchAllCheckBox;

    AdditionalSettingsSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    void initialize(URL location, ResourceBundle resources, Map<String, CheckBox> controlsMap) {
        super.initialize(location, resources);

        deleteDownloadedFilesCheckBox = controlsMap.get("deleteDownloadedFilesCheckBox");
        skipRemoteCheckBox = controlsMap.get("skipRemoteCheckBox");
        fetchAllCheckBox = controlsMap.get("fetchAllCheckBox");

        setInitValues();
        setProperties();
    }

    private void setInitValues() {
        skipRemoteCheckBox.setSelected(applicationProperties.isSkipRemote());
        fetchAllCheckBox.setSelected(applicationProperties.isFetchAll());
        deleteDownloadedFilesCheckBox.setSelected(applicationProperties.isDeleteDownloadedFiles());
    }

    private void setProperties() {
        deleteDownloadedFilesCheckBox.setDisable(
                !EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS).contains(applicationProperties.itemType())
        );
    }

    void disableDeleteDownloadedFiles(boolean disable) {
        deleteDownloadedFilesCheckBox.setDisable(disable);
    }

    void disableSkipRemote(boolean disable) {
        skipRemoteCheckBox.setDisable(disable);
    }

    void disableFetchAll(boolean disable) {
        fetchAllCheckBox.setDisable(disable);
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
}
