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
    private Button addConfigurationButton;
    private Button removeConfigurationButton;
    private Button saveConfigurationButton;
    private Label currentWeekNumberLabel;

    private final MainController mainController;

    private boolean useComboBoxValueChangeListener = true;

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

        RunConfig runConfigFromUI = mainController.createRunConfigFromUI();
        applicationProperties.updateCurrentRunConfig(runConfigFromUI);
        CacheManager.removeFromCache(applicationProperties.configurationName());
        uiLauncher.executeOutsideUIThread(mainController::updateRunConfig);
        mainController.setLastItemSubmissionDate();

        uiLauncher.updateTray(applicationProperties);
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                .withAlertType(Alert.AlertType.INFORMATION)
                .withWebViewDetails(new WebViewDetails(new WebViewService().createImageView(ImageFile.FINGER_UP_PNG)));
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
            TextInputDialog dialog = new TextInputDialog(BundleUtils.getMsg("main.addConfiguration.defaultValue"));
            dialog.setTitle(BundleUtils.getMsg("main.addConfiguration.newConfigDialog"));
            dialog.setHeaderText(BundleUtils.getMsg("main.addConfiguration.enterName"));

            Optional<String> newConfigName = dialog.showAndWait();
            if (newConfigName.isPresent() && !StringUtils.nullOrEmpty(newConfigName.get())) {
                updateConfigurationNameComboBox(ArgName.configurationName.defaultValue(), newConfigName.get());
                Optional<RunConfig> runConfig = applicationProperties.getRunConfig(newConfigName.get());
                if (runConfig.isEmpty()) {
                    applicationProperties.updateCurrentRunConfig(mainController.getRunConfigWithoutDates());
                    applicationProperties.save();
                    setDisableDependOnConfigurations();
                    new AlertWindowBuilder().withHeaderText(BundleUtils.getMsg("main.config.changed"))
                            .withAlertType(Alert.AlertType.INFORMATION)
                            .withWebViewDetails(new WebViewDetails(new WebViewService().createImageView(ImageFile.FINGER_UP_PNG)))
                            .buildAndDisplayWindow();
                }
            }
        };
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
                setDisableDependOnConfigurations();
                mainController.setToolkitCredentialsIfAvailable();
                alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.removed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWebViewDetails(new WebViewDetails(new WebViewService().createImageView(ImageFile.FINGER_UP_PNG)));
            } catch (IllegalStateException ex) {
                alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(ex.getMessage())
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.ERROR)
                        .withWebViewDetails(new WebViewDetails(new WebViewService().createImageView(ImageFile.ERROR_CHICKEN_PNG)));
            }
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        };
    }

    void setDisableDependOnConfigurations() {
        Map<String, RunConfig> runConfigMap = applicationProperties.getRunConfigMap();
        removeConfigurationButton.setDisable(runConfigMap.isEmpty());
        configurationNameComboBox.setDisable(runConfigMap.isEmpty());
        mainController.setDisableDependOnConfigurations();
    }

    private void removeConfigurationNameFromComboBox(String oldValue, String newValue) {
        List<String> items = new ArrayList<>(configurationNameComboBox.getItems());
        items.remove(oldValue);
        updateItemsForConfigComboBox(newValue, FXCollections.observableList(items));
    }

    private void setListeners() {
        configurationNameComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener(configurationNameComboBoxListener());
    }

    private ChangeListener<String> configurationNameComboBoxListener() {
        return (options, oldValue, newValue) -> {
            if (useComboBoxValueChangeListener) {
                RunConfig runConfigFromUI = mainController.createRunConfigFromUI();
                runConfigFromUI.setConfigurationName(oldValue);
                ApplicationProperties uiApplicationProperties =
                        ApplicationPropertiesFactory.getInstance(runConfigFromUI.toArgumentArray());
                CacheManager.addToCache(oldValue, uiApplicationProperties);

                applicationProperties = CacheManager.getApplicationProperties(newValue);
                mainController.setApplicationProperties(applicationProperties);
                setInitValues();
                mainController.deselectUseLastItemDate();
            }
        };
    }

    void updateItemsForConfigComboBox(String newValue, ObservableList<String> items) {
        useComboBoxValueChangeListener = false;
        configurationNameComboBox.setItems(items);
        configurationNameComboBox.setValue(newValue);
        useComboBoxValueChangeListener = true;
    }

    String getConfigurationName() {
        return configurationNameComboBox.getValue();
    }
}
