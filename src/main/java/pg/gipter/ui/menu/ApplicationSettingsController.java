package pg.gipter.ui.menu;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.quartz.SchedulerException;
import pg.gipter.ProgramSettings;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.MongoDaoConfig;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.CommandPatternValue;
import pg.gipter.core.producers.command.DiffCommandFactory;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.jobs.JobCreator;
import pg.gipter.jobs.JobCreatorFactory;
import pg.gipter.services.FXWebService;
import pg.gipter.services.StartupService;
import pg.gipter.services.TextFieldIntelliSense;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.BundleUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationSettingsController extends AbstractController {

    @FXML
    private TabPane applicationSettingsTabPane;

    @FXML
    private Tab applicationSettingsTab;
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
    private Label checkLastItemLabel;

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
    private CheckBox checkLastItemCheckBox;
    @FXML
    private CheckBox uploadItemCheckBox;
    @FXML
    private CheckBox smartZipCheckBox;
    @FXML
    private ComboBox<String> languageComboBox;

    @FXML
    private Tab customCommandTab;
    @FXML
    private TextField gitCommandTextField;
    @FXML
    private TextField svnCommandTextField;
    @FXML
    private TextField mercurialCommandTextField;
    @FXML
    private CheckBox overrideGitCheckBox;
    @FXML
    private CheckBox overrideSvnCheckBox;
    @FXML
    private CheckBox overrideMercurialCheckBox;
    @FXML
    private Label overrideLabel;
    @FXML
    private Button refreshSettingsButton;

    private final Map<String, Labeled> labelsAffectedByLanguage;

    public ApplicationSettingsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        labelsAffectedByLanguage = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setProperties();
        setListeners();
        setAccelerators();
        setActions();
        createLabelsMap();
        TextFieldIntelliSense.init(gitCommandTextField, CommandPatternValue.class);
        TextFieldIntelliSense.init(svnCommandTextField, CommandPatternValue.class);
        TextFieldIntelliSense.init(mercurialCommandTextField, CommandPatternValue.class);
    }

    @Override
    public void executeBeforeClose() {
        saveNewSettings();
    }

    private void setInitValues() {
        confirmationWindowCheckBox.setSelected(applicationProperties.isConfirmationWindow());
        preferredArgSourceComboBox.setItems(FXCollections.observableArrayList(PreferredArgSource.values()));
        preferredArgSourceComboBox.setValue(PreferredArgSource.UI);
        useUICheckBox.setSelected(applicationProperties.isUseUI());
        activateTrayCheckBox.setSelected(uiLauncher.isTrayActivated());
        autostartCheckBox.setSelected(applicationProperties.isEnableOnStartup() && uiLauncher.isTrayActivated());
        silentModeCheckBox.setSelected(applicationProperties.isSilentMode());
        uploadItemCheckBox.setSelected(applicationProperties.isUploadItem());
        smartZipCheckBox.setSelected(applicationProperties.isSmartZip());

        if (languageComboBox.getItems().isEmpty()) {
            languageComboBox.setItems(FXCollections.observableList(BundleUtils.getSupportedLanguages()));
        }
        languageComboBox.setValue(applicationProperties.uiLanguage());
        checkLastItemCheckBox.setSelected(applicationProperties.isCheckLastItemEnabled());

        final CustomCommand gitCustomCommand = applicationProperties.getCustomCommand(VersionControlSystem.GIT);
        overrideGitCheckBox.setSelected(gitCustomCommand.isOverride());
        gitCommandTextField.setText(gitCustomCommand.isOverride() ? gitCustomCommand.getCommand() : String.join(
                " ",
                DiffCommandFactory.getInstance(VersionControlSystem.GIT, applicationProperties).commandAsList())
        );

        final CustomCommand svnCustomCommand = applicationProperties.getCustomCommand(VersionControlSystem.SVN);
        overrideSvnCheckBox.setSelected(svnCustomCommand.isOverride());
        svnCommandTextField.setText(svnCustomCommand.isOverride() ? svnCustomCommand.getCommand() : String.join(
                " ",
                DiffCommandFactory.getInstance(VersionControlSystem.SVN, applicationProperties).commandAsList())
        );

        final CustomCommand mercurialCustomCommand = applicationProperties.getCustomCommand(VersionControlSystem.MERCURIAL);
        overrideMercurialCheckBox.setSelected(mercurialCustomCommand.isOverride());
        mercurialCommandTextField.setText(mercurialCustomCommand.isOverride() ? mercurialCustomCommand.getCommand() :
                String.join(
                        " ",
                        DiffCommandFactory.getInstance(VersionControlSystem.MERCURIAL, applicationProperties).commandAsList()
                )
        );
    }

    private void setProperties() {
        activateTrayCheckBox.setDisable(!uiLauncher.isTraySupported());
        autostartCheckBox.setDisable(!uiLauncher.isTraySupported());
        useUICheckBox.setDisable(true);
        preferredArgSourceComboBox.setDisable(true);
        silentModeCheckBox.setDisable(true);
        gitCommandTextField.setDisable(!overrideGitCheckBox.isSelected());
        svnCommandTextField.setDisable(!overrideSvnCheckBox.isSelected());
        mercurialCommandTextField.setDisable(!overrideMercurialCheckBox.isSelected());
    }

    private void setListeners() {
        languageComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    BundleUtils.changeBundle(languageComboBox.getValue());
                    labelsAffectedByLanguage.forEach((key, labeled) -> labeled.setText(BundleUtils.getMsg(key)));
                    applicationSettingsTab.setText(BundleUtils.getMsg("launch.panel.title"));
                    customCommandTab.setText(BundleUtils.getMsg("launch.customCommand.tab"));
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

        final ChangeListener<Boolean> saveNewSettingsChangeListener = (observable, oldValue, newValue) -> saveNewSettings();
        confirmationWindowCheckBox.selectedProperty().addListener(saveNewSettingsChangeListener);

        checkLastItemCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            processLastItemJob(newValue);
            saveNewSettings();
        });

        overrideGitCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            gitCommandTextField.setDisable(oldValue);
            saveNewSettings();
        });

        overrideSvnCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            svnCommandTextField.setDisable(oldValue);
            saveNewSettings();
        });

        overrideMercurialCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            mercurialCommandTextField.setDisable(oldValue);
            saveNewSettings();
        });
    }

    private void processLastItemJob(Boolean shouldSchedule) {
        try {
            final JobCreator jobCreator = JobCreatorFactory.lastItemJobCreator(applicationProperties);
            if (shouldSchedule) {
                uiLauncher.getJobService().scheduleJob(jobCreator);
            } else {
                uiLauncher.getJobService().deleteJob(jobCreator);
            }
        } catch (SchedulerException ex) {
            logger.error("Can not schedule the last item job.");
        }
    }

    private void saveNewSettings() {
        ApplicationConfig applicationConfig = createApplicationConfigFromUI();
        applicationProperties.updateApplicationConfig(applicationConfig);
        applicationProperties.save();
        logger.info("New application settings saved. [{}]", applicationConfig);
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
        applicationConfig.setCheckLastItemEnabled(checkLastItemCheckBox.isSelected());
        applicationConfig.setUploadItem(uploadItemCheckBox.isSelected());
        applicationConfig.setSmartZip(smartZipCheckBox.isSelected());
        applicationConfig.setCustomCommands(
                Stream.of(
                        new CustomCommand(
                                VersionControlSystem.GIT,
                                gitCommandTextField.getText(),
                                overrideGitCheckBox.isSelected()
                        ),
                        new CustomCommand(
                                VersionControlSystem.SVN,
                                svnCommandTextField.getText(),
                                overrideSvnCheckBox.isSelected()
                        ),
                        new CustomCommand(
                                VersionControlSystem.MERCURIAL,
                                mercurialCommandTextField.getText(),
                                overrideMercurialCheckBox.isSelected()
                        )
                ).collect(Collectors.toSet())
        );
        return applicationConfig;
    }

    private void setAccelerators() {
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (KeyCode.ESCAPE == e.getCode()) {
                uiLauncher.closeApplicationWindow();
            } else if (e.isControlDown() && KeyCode.S == e.getCode()) {
                saveNewSettings();
            }
        });
    }

    private void setActions() {
        refreshSettingsButton.setOnAction(actionEvent -> {
            try {
                ProgramSettings.initProgramSettings();
                MongoDaoConfig.refresh(ProgramSettings.getInstance().getDbProperties());
                new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("launch.panel.refreshed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWebViewDetails(WebViewService.getInstance().pullSuccessWebView())
                        .buildAndDisplayWindow();
            } catch (IOException e) {
                new FXWebService().initSSO();
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
        labelsAffectedByLanguage.put("launch.panel.lastItemJob", checkLastItemLabel);
        labelsAffectedByLanguage.put("launch.customCommand.override", overrideLabel);
        labelsAffectedByLanguage.put("launch.panel.uploadItem", uploadItemCheckBox);
        labelsAffectedByLanguage.put("launch.panel.smartZip", smartZipCheckBox);
        labelsAffectedByLanguage.put("launch.panel.refreshSettings", refreshSettingsButton);
    }
}
