package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.ui.alerts.LogLinkAction;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;
import pg.gipter.utils.SystemUtils;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

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
    void initialize(URL location, ResourceBundle resources, Map<String, Control> controlsMap) {
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
        Optional<AlertWindowBuilder> alertWindow = displayAuthorChangeWarning(runConfigFromUI);
        applicationProperties.updateCurrentRunConfig(runConfigFromUI);
        CacheManager.removeFromCache(applicationProperties.configurationName());
        uiLauncher.executeOutsideUIThread(mainController::updateRunConfig);
        mainController.setLastItemSubmissionDate();

        uiLauncher.updateTray(applicationProperties);
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                .withAlertType(Alert.AlertType.INFORMATION)
                .withImageFile(ImageFile.FINGER_UP_PNG);
        alertWindow.orElseGet(() -> alertWindowBuilder);
        Platform.runLater(() -> alertWindow.orElseGet(() -> alertWindowBuilder).buildAndDisplayWindow());
    }

    private Optional<AlertWindowBuilder> displayAuthorChangeWarning(RunConfig runConfig) {
        Optional<AlertWindowBuilder> result = Optional.empty();
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                .withAlertType(Alert.AlertType.INFORMATION)
                .withWebViewDetails(WebViewService.getInstance().pullPartialSuccessWebView());
        boolean isOverride = applicationProperties.getCustomCommand(VersionControlSystem.GIT).isOverride() ||
                applicationProperties.getCustomCommand(VersionControlSystem.SVN).isOverride() ||
                applicationProperties.getCustomCommand(VersionControlSystem.MERCURIAL).isOverride();
        if (isOverride && Objects.nonNull(runConfig.getAuthor()) &&
                !runConfig.getAuthor().equals(String.join(",", applicationProperties.authors()))) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.author.changed",
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (isOverride && Objects.nonNull(runConfig.getCommitterEmail()) &&
                !runConfig.getCommitterEmail().equals(applicationProperties.committerEmail())) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.committerEmail.changed",
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (Objects.nonNull(runConfig.getGitAuthor()) && Objects.nonNull(applicationProperties.gitAuthor()) &&
                !runConfig.getGitAuthor().equals(applicationProperties.gitAuthor()) &&
                applicationProperties.getCustomCommand(VersionControlSystem.GIT).isOverride()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.cvsAuthor.changed",
                    VersionControlSystem.GIT.name(),
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (Objects.nonNull(runConfig.getSvnAuthor()) && Objects.nonNull(applicationProperties.svnAuthor()) &&
                !runConfig.getSvnAuthor().equals(applicationProperties.svnAuthor()) &&
                applicationProperties.getCustomCommand(VersionControlSystem.SVN).isOverride()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.cvsAuthor.changed",
                    VersionControlSystem.SVN.name(),
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (Objects.nonNull(runConfig.getMercurialAuthor()) && Objects.nonNull(applicationProperties.mercurialAuthor()) &&
                !runConfig.getMercurialAuthor().equals(applicationProperties.mercurialAuthor()) &&
                applicationProperties.getCustomCommand(VersionControlSystem.MERCURIAL).isOverride()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.cvsAuthor.changed",
                    VersionControlSystem.MERCURIAL.name(),
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (isOverride && Objects.isNull(runConfig.getAuthor()) && !applicationProperties.authors().isEmpty()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.author.removed",
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (isOverride && Objects.isNull(runConfig.getCommitterEmail()) &&
                !StringUtils.nullOrEmpty(applicationProperties.committerEmail())) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.committerEmail.removed",
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (Objects.isNull(runConfig.getGitAuthor()) && Objects.nonNull(applicationProperties.gitAuthor()) &&
                applicationProperties.getCustomCommand(VersionControlSystem.GIT).isOverride()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.cvsAuthor.removed",
                    VersionControlSystem.GIT.name(),
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (Objects.isNull(runConfig.getSvnAuthor()) && Objects.nonNull(applicationProperties.svnAuthor()) &&
                applicationProperties.getCustomCommand(VersionControlSystem.SVN).isOverride()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.cvsAuthor.removed",
                    VersionControlSystem.SVN.name(),
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        } else if (Objects.isNull(runConfig.getMercurialAuthor()) && Objects.nonNull(applicationProperties.mercurialAuthor()) &&
                applicationProperties.getCustomCommand(VersionControlSystem.MERCURIAL).isOverride()) {
            alertWindowBuilder.withMessage(BundleUtils.getMsg(
                    "main.cvsAuthor.removed",
                    VersionControlSystem.MERCURIAL.name(),
                    SystemUtils.lineSeparator()
            ));
            result = Optional.of(alertWindowBuilder);
        }
        return result;
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
                            .withImageFile(ImageFile.FINGER_UP_PNG)
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
                        .withImageFile(ImageFile.FINGER_UP_PNG);
            } catch (IllegalStateException ex) {
                alertWindowBuilder = new AlertWindowBuilder()
                        .withMessage(ex.getMessage())
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.ERROR)
                        .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
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
