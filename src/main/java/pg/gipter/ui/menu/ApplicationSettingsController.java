package pg.gipter.ui.menu;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.services.StartupService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.*;

public class ApplicationSettingsController extends AbstractController {

    @FXML
    private Label confirmationWindowLabel;
    @FXML
    private Label activateTrayLabel;
    @FXML
    private Label autoStartLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label preferredArgSourceLabel;
    @FXML
    private TitledPane titledPane;

    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private CheckBox confirmationWindowCheckBox;
    @FXML
    private ComboBox<PreferredArgSource> preferredArgSourceComboBox;
    @FXML
    private CheckBox useUICheckBox;
    @FXML
    private CheckBox activateTrayCheckBox;
    @FXML
    private CheckBox autostartCheckBox;
    @FXML
    private CheckBox silentModeCheckBox;
    @FXML
    private ComboBox<String> languageComboBox;

    private final Map<String, Labeled> labelsAffectedByLanguage;

    public ApplicationSettingsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        labelsAffectedByLanguage = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues(resources);
        setProperties();
        setListeners();
        setAccelerators();
        createLabelsMap();
    }

    private void setInitValues(ResourceBundle resources) {
        confirmationWindowCheckBox.setSelected(applicationProperties.isConfirmationWindow());
        preferredArgSourceComboBox.setItems(FXCollections.observableArrayList(PreferredArgSource.values()));
        preferredArgSourceComboBox.setValue(PreferredArgSource.UI);
        useUICheckBox.setSelected(applicationProperties.isUseUI());
        activateTrayCheckBox.setSelected(uiLauncher.isTrayActivated());
        autostartCheckBox.setSelected(applicationProperties.isEnableOnStartup() && uiLauncher.isTrayActivated());
        silentModeCheckBox.setSelected(applicationProperties.isSilentMode());

        if (languageComboBox.getItems().isEmpty()) {
            languageComboBox.setItems(FXCollections.observableList(Arrays.asList(BundleUtils.SUPPORTED_LANGUAGES)));
        }
        languageComboBox.setValue(applicationProperties.uiLanguage());
    }

    private void setProperties() {
        activateTrayCheckBox.setDisable(!uiLauncher.isTraySupported());
        autostartCheckBox.setDisable(!uiLauncher.isTraySupported());
        useUICheckBox.setDisable(true);
        preferredArgSourceComboBox.setDisable(true);
        silentModeCheckBox.setDisable(true);
    }

    private void setListeners() {
        languageComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    BundleUtils.changeBundle(languageComboBox.getValue());
                    labelsAffectedByLanguage.forEach((key, labeled) -> labeled.setText(BundleUtils.getMsg(key)));
                    uiLauncher.changeApplicationSettingsWindowTitle();
                    applicationProperties.updateApplicationConfig(createApplicationConfigFromUI());
                    applicationProperties.save();
                    uiLauncher.setApplicationProperties(applicationProperties);
                });

        final StartupService startupService = new StartupService();
        activateTrayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                uiLauncher.setApplicationProperties(applicationProperties);
                uiLauncher.initTrayHandler();
                uiLauncher.currentWindow().setOnCloseRequest(uiLauncher.trayOnCloseEventHandler());
                autostartCheckBox.setDisable(false);
            } else {
                uiLauncher.currentWindow().setOnCloseRequest(AbstractController.regularOnCloseEventHandler());
                uiLauncher.removeTray();
                autostartCheckBox.setDisable(true);
                autostartCheckBox.setSelected(false);
                startupService.disableStartOnStartup();
            }
            saveNewSettings();
        });

        autostartCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startupService.startOnStartup();
            } else {
                startupService.disableStartOnStartup();
            }
            saveNewSettings();
        });

        confirmationWindowCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> saveNewSettings());
    }

    private void saveNewSettings() {
        ApplicationConfig applicationConfig = createApplicationConfigFromUI();
        applicationProperties.updateApplicationConfig(applicationConfig);
        applicationProperties.save();
        logger.info("New application settings saved. [{}]", applicationConfig.toString());
    }

    private ApplicationConfig createApplicationConfigFromUI() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setConfirmationWindow(confirmationWindowCheckBox.isSelected());
        applicationConfig.setPreferredArgSource(preferredArgSourceComboBox.getValue());
        applicationConfig.setUseUI(useUICheckBox.isSelected());
        applicationConfig.setActiveTray(activateTrayCheckBox.isSelected());
        applicationConfig.setEnableOnStartup(autostartCheckBox.isSelected());
        applicationConfig.setSilentMode(silentModeCheckBox.isSelected());
        applicationConfig.setUiLanguage(languageComboBox.getValue());
        return applicationConfig;
    }

    private void setAccelerators() {
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (KeyCode.ESCAPE == e.getCode()) {
                uiLauncher.closeApplicationWindow();
            }
        });
    }

    private void createLabelsMap() {
        labelsAffectedByLanguage.put("launch.panel.confirmationWindow", confirmationWindowLabel);
        labelsAffectedByLanguage.put("launch.panel.activateTray", activateTrayLabel);
        labelsAffectedByLanguage.put("launch.panel.autoStart", autoStartLabel);
        labelsAffectedByLanguage.put("launch.panel.language", languageLabel);
        labelsAffectedByLanguage.put("launch.panel.preferredArgSource", preferredArgSourceLabel);
        labelsAffectedByLanguage.put("launch.panel.useUI", useUICheckBox);
        labelsAffectedByLanguage.put("launch.panel.silentMode", silentModeCheckBox);
        labelsAffectedByLanguage.put("launch.panel.title", titledPane);
    }
}
