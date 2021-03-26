package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import pg.gipter.core.*;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.model.RunConfig;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

class ConfigurationSectionController extends AbstractController {

    private ComboBox<String> configurationNameComboBox;
    private TextField configurationNameTextField;
    private Button addConfigurationButton;
    private Button removeConfigurationButton;
    private Button saveConfigurationButton;
    private Label currentWeekNumberLabel;

    private final MainController mainController;

    private static boolean useComboBoxValueChangeListener = true;

    ConfigurationSectionController(UILauncher uiLauncher,
                                   ApplicationProperties applicationProperties,
                                   MainController mainController) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.mainController = mainController;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void initialize(URL location, ResourceBundle resources, Map<String, Object> controlsMap) {
        super.initialize(location, resources);

        configurationNameComboBox = (ComboBox) controlsMap.get("configurationNameComboBox");
        configurationNameTextField = (TextField) controlsMap.get("configurationNameTextField");
        addConfigurationButton = (Button) controlsMap.get("addConfigurationButton");
        removeConfigurationButton = (Button) controlsMap.get("removeConfigurationButton");
        saveConfigurationButton = (Button) controlsMap.get("saveConfigurationButton");
        currentWeekNumberLabel = (Label) controlsMap.get("currentWeekNumberLabel");

        setInitValues();
        initConfigurationName();
        setActions();
        setListeners();
    }

    private void setInitValues() {
        currentWeekNumberLabel.setText(String.valueOf(applicationProperties.getWeekNumber(LocalDate.now())));
        mainController.setInitValues(applicationProperties);
    }

    private void initConfigurationName() {
        Set<String> confNames = new HashSet<>(applicationProperties.configurationNames());
        if (!StringUtils.nullOrEmpty(configurationNameComboBox.getValue())) {
            confNames.add(configurationNameComboBox.getValue());
        }
        configurationNameComboBox.setItems(FXCollections.observableList(new ArrayList<>(confNames)));
        if (confNames.contains(applicationProperties.configurationName())) {
            configurationNameComboBox.setValue(applicationProperties.configurationName());
            configurationNameTextField.setText(applicationProperties.configurationName());
        }
    }

    private void setActions() {
        saveConfigurationButton.setOnAction(saveConfigurationActionEventHandler());
        addConfigurationButton.setOnAction(addConfigurationEventHandler());
        removeConfigurationButton.setOnAction(removeConfigurationEventHandler());
    }

    private EventHandler<ActionEvent> saveConfigurationActionEventHandler() {
        return event -> saveConfiguration();
    }

    void saveConfiguration() {
        String configurationName = configurationNameTextField.getText();
        String comboConfigName = configurationNameComboBox.getValue();

        RunConfig runConfigFromUI = mainController.createRunConfigFromUI();
        applicationProperties.updateCurrentRunConfig(runConfigFromUI);
        CacheManager.removeFromCache(applicationProperties.configurationName());
        uiLauncher.executeOutsideUIThread(() -> mainController.updateRunConfig(comboConfigName, configurationName));
        mainController.setLastItemSubmissionDate();

        updateConfigurationNameComboBox(comboConfigName, configurationName);
        uiLauncher.updateTray(applicationProperties);
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                .withAlertType(Alert.AlertType.INFORMATION)
                .withImage(ImageFile.FINGER_UP_PNG);
        Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
    }

    void updateConfigurationNameComboBox(String oldValue, String newValue) {
        List<String> items = new ArrayList<>(configurationNameComboBox.getItems());
        items.remove(oldValue);
        items.add(newValue);
        updateItemsForConfigComboBox(newValue, FXCollections.observableArrayList(items));
    }

    private EventHandler<ActionEvent> addConfigurationEventHandler() {
        return event -> {
            String configurationName = configurationNameTextField.getText();
            Optional<RunConfig> runConfig = applicationProperties.getRunConfig(configurationName);
            boolean operationDone = false;
            if (runConfig.isPresent()) {
                boolean result = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.overrideProperties.message", configurationName))
                        .withAlertType(Alert.AlertType.CONFIRMATION)
                        .withImage(ImageFile.OVERRIDE_PNG)
                        .withOkButtonText(BundleUtils.getMsg("popup.overrideProperties.buttonOk"))
                        .withCancelButtonText(BundleUtils.getMsg("popup.overrideProperties.buttonNo"))
                        .buildAndDisplayOverrideWindow();
                if (result) {
                    saveNewConfig(configurationName);
                    updateConfigurationNameComboBox(configurationNameComboBox.getValue(), configurationName);
                    operationDone = true;
                } else {
                    configurationNameTextField.setText(configurationNameComboBox.getValue());
                }
            } else {
                applicationProperties.updateCurrentRunConfig(mainController.getRunConfigWithoutDates());
                applicationProperties.save();
                updateConfigurationNameComboBox(ArgName.configurationName.defaultValue(), configurationName);
                operationDone = true;
            }
            if (operationDone) {
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImage(ImageFile.FINGER_UP_PNG);
                alertWindowBuilder.buildAndDisplayWindow();
            }

        };
    }

    private void saveNewConfig(String configurationName) {
        RunConfig currentRunConfig = mainController.getRunConfigWithoutDates();
        currentRunConfig.setConfigurationName(configurationName);
        applicationProperties.updateCurrentRunConfig(currentRunConfig);
        applicationProperties.save();
    }

    private EventHandler<ActionEvent> removeConfigurationEventHandler() {
        return event -> {
            AlertWindowBuilder alertWindowBuilder;
            try {
                CacheManager.removeFromCache(configurationNameComboBox.getValue());
                applicationProperties.removeConfig(configurationNameComboBox.getValue());
                String newConfiguration = ArgName.configurationName.defaultValue();
                if (!applicationProperties.getRunConfigMap().isEmpty()) {
                    newConfiguration = applicationProperties.configurationName();
                }
                removeConfigurationNameFromComboBox(configurationNameComboBox.getValue(), newConfiguration);

                applicationProperties = CacheManager.getApplicationProperties(newConfiguration);
                setInitValues();
                configurationNameTextField.setText(configurationNameComboBox.getValue());
                setDisableDependOnConfigurations();
                mainController.setToolkitCredentialsIfAvailable();
                alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.removed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImage(ImageFile.FINGER_UP_PNG);
            } catch (IllegalStateException ex) {
                alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(ex.getMessage())
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage(ImageFile.ERROR_CHICKEN_PNG);
            }
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        };
    }

    void setDisableDependOnConfigurations() {
        Map<String, RunConfig> runConfigMap = applicationProperties.getRunConfigMap();
        addConfigurationButton.setDisable(runConfigMap.isEmpty());
        removeConfigurationButton.setDisable(runConfigMap.isEmpty());
        configurationNameComboBox.setDisable(runConfigMap.isEmpty());
        mainController.setDisableDependOnConfigurations();
    }

    private void removeConfigurationNameFromComboBox(String oldValue, String newValue) {
        List<String> items = new ArrayList<>(configurationNameComboBox.getItems());
        items.remove(oldValue);
        updateItemsForConfigComboBox(newValue, FXCollections.observableList(items));
    }

    private ChangeListener<String> configurationNameListener() {
        return (observable, oldValue, newValue) -> {
            if (StringUtils.nullOrEmpty(oldValue) && !StringUtils.nullOrEmpty(newValue)) {
                addConfigurationButton.setDisable(false);
                mainController.setDisableProjectPathButton(false);
            } else if (StringUtils.nullOrEmpty(newValue)) {
                addConfigurationButton.setDisable(true);
                mainController.setDisableProjectPathButton(true);
            }
        };
    }

    private void setListeners() {
        configurationNameComboBox.getSelectionModel().selectedItemProperty().addListener(configurationNameComboBoxListener());
        configurationNameTextField.textProperty().addListener(configurationNameListener());
    }

    private ChangeListener<String> configurationNameComboBoxListener() {
        return (options, oldValue, newValue) -> {
            if (useComboBoxValueChangeListener) {
                RunConfig runConfigFromUI = mainController.createRunConfigFromUI();
                ApplicationProperties uiApplicationProperties =
                        ApplicationPropertiesFactory.getInstance(runConfigFromUI.toArgumentArray());
                CacheManager.addToCache(oldValue, uiApplicationProperties);

                applicationProperties = CacheManager.getApplicationProperties(newValue);
                setInitValues();
                configurationNameTextField.setText(newValue);
                mainController.deselectUseLastItemDate();
            }
        };
    }

    void updateItemsForConfigComboBox(String newValue, ObservableList<String> items) {
        useComboBoxValueChangeListener = false;
        configurationNameComboBox.setItems(items);
        configurationNameComboBox.setValue(newValue);
        useComboBoxValueChangeListener = true;
        setDisableDependOnConfigurations();
    }

    String getConfigurationName() {
        return configurationNameTextField.getText();
    }
}
