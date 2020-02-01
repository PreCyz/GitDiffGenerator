package pg.gipter.ui.menu;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dto.ApplicationConfig;
import pg.gipter.service.StartupService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ApplicationSettingsController extends AbstractController {

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

    private ApplicationProperties applicationProperties;

    private static String currentLanguage;

    public ApplicationSettingsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues(resources);
        setProperties();
        setListeners();
        setAccelerators();
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
        if (StringUtils.nullOrEmpty(currentLanguage)) {
            if (StringUtils.nullOrEmpty(resources.getLocale().getLanguage())
                    || BundleUtils.SUPPORTED_LANGUAGES[0].equals(resources.getLocale().getLanguage())) {
                currentLanguage = BundleUtils.SUPPORTED_LANGUAGES[0];

            } else if (BundleUtils.SUPPORTED_LANGUAGES[1].equals(resources.getLocale().getLanguage())) {
                currentLanguage = BundleUtils.SUPPORTED_LANGUAGES[1];
            }
        }
        languageComboBox.setValue(currentLanguage);
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
                    currentLanguage = languageComboBox.getValue();
                    String[] arguments = configurationDao.loadArgumentArray(uiLauncher.getConfigurationName());
                    uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(arguments));
                    uiLauncher.changeLanguage(languageComboBox.getValue());
                });

        final StartupService startupService = new StartupService();
        activateTrayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                String[] arguments = configurationDao.loadArgumentArray(uiLauncher.getConfigurationName());
                uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(arguments));
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
        configurationDao.saveApplicationConfig(applicationConfig);
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
        return applicationConfig;
    }

    private void setAccelerators() {
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (KeyCode.ESCAPE == e.getCode()) {
                uiLauncher.closeApplicationWindow();
            }
        });
    }
}
