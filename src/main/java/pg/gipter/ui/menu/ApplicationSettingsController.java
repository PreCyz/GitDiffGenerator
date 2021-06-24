package pg.gipter.ui.menu;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.quartz.SchedulerException;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.CommandPatternValue;
import pg.gipter.core.producers.command.DiffCommandFactory;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.jobs.JobCreator;
import pg.gipter.jobs.JobCreatorFactory;
import pg.gipter.services.IntelliSenseService;
import pg.gipter.services.StartupService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.*;
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
    private Label importCertLabel;
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
    private CheckBox importCertCheckBox;
    @FXML
    private CheckBox checkLastItemCheckBox;
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

    private final Map<String, Labeled> labelsAffectedByLanguage;
    private final IntelliSenseService<CommandPatternValue> intelliSenseService;
    boolean ignoreGitCommandListener = false;
    private String currentGitCommandValue = "";
    private final Set<String> definedPatterns;

    public ApplicationSettingsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        labelsAffectedByLanguage = new HashMap<>();
        intelliSenseService = new IntelliSenseService<>(CommandPatternValue.class);
        definedPatterns = intelliSenseService.getDefinedPatterns();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setProperties();
        setActions();
        setListeners();
        setAccelerators();
        createLabelsMap();
    }

    private void setInitValues() {
        confirmationWindowCheckBox.setSelected(applicationProperties.isConfirmationWindow());
        preferredArgSourceComboBox.setItems(FXCollections.observableArrayList(PreferredArgSource.values()));
        preferredArgSourceComboBox.setValue(PreferredArgSource.UI);
        useUICheckBox.setSelected(applicationProperties.isUseUI());
        activateTrayCheckBox.setSelected(uiLauncher.isTrayActivated());
        autostartCheckBox.setSelected(applicationProperties.isEnableOnStartup() && uiLauncher.isTrayActivated());
        silentModeCheckBox.setSelected(applicationProperties.isSilentMode());

        if (languageComboBox.getItems().isEmpty()) {
            languageComboBox.setItems(FXCollections.observableList(BundleUtils.getSupportedLanguages()));
        }
        languageComboBox.setValue(applicationProperties.uiLanguage());
        importCertCheckBox.setSelected(applicationProperties.isCertImportEnabled());
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
        gitCommandTextField.textProperty().addListener(gitCommandTextFieldChangeListener());
        svnCommandTextField.setDisable(!overrideSvnCheckBox.isSelected());
        mercurialCommandTextField.setDisable(!overrideMercurialCheckBox.isSelected());
        TextFields.bindAutoCompletion(gitCommandTextField, gitCommandSuggestionsCallback());
    }

    private void setActions() {
        gitCommandTextField.setOnKeyReleased(gitCommandTextFieldKeyReleasedEventHandler());
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

        importCertCheckBox.selectedProperty().addListener(saveNewSettingsChangeListener);
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

        applicationSettingsTab.selectedProperty().addListener((observable, oldValue, newValue) -> saveNewSettings());
        customCommandTab.selectedProperty().addListener((observable, oldValue, newValue) -> saveNewSettings());

        gitCommandTextField.textProperty().addListener((observable, oldValue, newValue) -> saveNewSettings());
        svnCommandTextField.textProperty().addListener((observable, oldValue, newValue) -> saveNewSettings());
        mercurialCommandTextField.textProperty().addListener((observable, oldValue, newValue) -> saveNewSettings());
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
        applicationConfig.setCertImportEnabled(importCertCheckBox.isSelected());
        applicationConfig.setCheckLastItemEnabled(checkLastItemCheckBox.isSelected());
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

    private void createLabelsMap() {
        labelsAffectedByLanguage.put("launch.panel.confirmationWindow", confirmationWindowLabel);
        labelsAffectedByLanguage.put("launch.panel.activateTray", activateTrayLabel);
        labelsAffectedByLanguage.put("launch.panel.autoStart", autoStartLabel);
        labelsAffectedByLanguage.put("launch.panel.language", languageLabel);
        labelsAffectedByLanguage.put("launch.panel.preferredArgSource", preferredArgSourceLabel);
        labelsAffectedByLanguage.put("launch.panel.useUI", useUICheckBox);
        labelsAffectedByLanguage.put("launch.panel.silentMode", silentModeCheckBox);
        labelsAffectedByLanguage.put("launch.panel.certImport", importCertLabel);
        labelsAffectedByLanguage.put("launch.panel.lastItemJob", checkLastItemLabel);
        labelsAffectedByLanguage.put("launch.customCommand.override", overrideLabel);
    }

    private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> gitCommandSuggestionsCallback() {
        return param -> {
            if (gitCommandTextField.getCaretPosition() < param.getUserText().length()) {
                gitCommandTextField.positionCaret(param.getUserText().length());
            }
            return intelliSenseService.getFilteredValues(param.getUserText());
        };
    }

    private EventHandler<KeyEvent> gitCommandTextFieldKeyReleasedEventHandler() {
        return event -> {
            if (EnumSet.of(KeyCode.ENTER, KeyCode.TAB).contains(event.getCode())) {
                ignoreGitCommandListener = true;
                gitCommandTextField.setText(currentGitCommandValue);
                gitCommandTextField.positionCaret(currentGitCommandValue.length());
                ignoreGitCommandListener = false;
            } else if (KeyCode.BACK_SPACE == event.getCode()) {
                final Optional<Integer> startPosition = intelliSenseService.getSelectedStartPosition(currentGitCommandValue);
                if (startPosition.isPresent()) {
                    final int endSelectionPosition = gitCommandTextField.getCaretPosition();
                    gitCommandTextField.selectRange(startPosition.get(), endSelectionPosition);
                }
            }
        };
    }

    private ChangeListener<String> gitCommandTextFieldChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (ignoreGitCommandListener) return;

            currentGitCommandValue = newValue;
            if (definedPatterns.contains(newValue)) {
                currentGitCommandValue = intelliSenseService.getValue(oldValue, newValue);
                ignoreGitCommandListener = true;
                gitCommandTextField.setText(currentGitCommandValue);
                ignoreGitCommandListener = false;
            }
            gitCommandTextField.positionCaret(currentGitCommandValue.length());
        };
    }
}
