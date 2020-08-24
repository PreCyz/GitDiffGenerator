package pg.gipter.ui.main;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import pg.gipter.core.*;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.services.DataService;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.services.vcs.VcsServiceFactory;
import pg.gipter.ui.*;
import pg.gipter.utils.*;

import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class MainController extends AbstractController {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private MenuItem applicationMenuItem;
    @FXML
    private MenuItem toolkitMenuItem;
    @FXML
    private MenuItem readMeMenuItem;
    @FXML
    private MenuItem instructionMenuItem;
    @FXML
    private MenuItem upgradeMenuItem;
    @FXML
    private MenuItem wizardMenuItem;
    @FXML
    private MenuItem wikiMenuItem;
    @FXML
    private MenuItem importCertMenuItem;
    @FXML
    private MenuItem importCertProgrammaticMenuItem;

    @FXML
    private TextField authorsTextField;
    @FXML
    private TextField committerEmailTextField;
    @FXML
    private TextField gitAuthorTextField;
    @FXML
    private TextField mercurialAuthorTextField;
    @FXML
    private TextField svnAuthorTextField;
    @FXML
    private ComboBox<ItemType> itemTypeComboBox;
    @FXML
    private CheckBox useDefaultAuthorCheckBox;
    @FXML
    private CheckBox useDefaultEmailCheckBox;

    @FXML
    private TextField toolkitUsernameTextField;
    @FXML
    private PasswordField toolkitPasswordField;
    @FXML
    private TextField toolkitDomainTextField;
    @FXML
    private Hyperlink verifyCredentialsHyperlink;
    @FXML
    private TextField toolkitProjectListNamesTextField;
    @FXML
    private ProgressIndicator verifyProgressIndicator;

    @FXML
    private CheckBox deleteDownloadedFilesCheckBox;
    @FXML
    private CheckBox skipRemoteCheckBox;
    @FXML
    private CheckBox fetchAllCheckBox;

    @FXML
    private Label projectPathLabel;
    @FXML
    private Label itemPathLabel;
    @FXML
    private TextField itemFileNamePrefixTextField;
    @FXML
    private Button projectPathButton;
    @FXML
    private Button itemPathButton;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private CheckBox useLastItemDateCheckbox;

    @FXML
    private Button executeButton;
    @FXML
    private Button executeAllButton;
    @FXML
    private Button jobButton;
    @FXML
    private Button exitButton;
    @FXML
    private ProgressIndicator loadProgressIndicator;
    @FXML
    private Label infoLabel;
    @FXML
    private ComboBox<String> configurationNameComboBox;
    @FXML
    private TextField configurationNameTextField;
    @FXML
    private Button addConfigurationButton;
    @FXML
    private Button removeConfigurationButton;
    @FXML
    private Button saveConfigurationButton;
    @FXML
    private Label currentWeekNumberLabel;

    private final DataService dataService;

    private VcsService vcsService;
    private final ToolkitSectionController toolkitSectionController;
    private final ConfigurationSectionController configurationSectionController;
    private final MenuSectionController menuSectionController;
    private final PathsSectionController pathsSectionController;
    private final DatesSectionController datesSectionController;
    private final AdditionalSettingsSectionController additionalSettingsSectionController;

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.dataService = DataService.getInstance();
        this.vcsService = VcsServiceFactory.getInstance();
        toolkitSectionController = new ToolkitSectionController(uiLauncher, applicationProperties);
        configurationSectionController = new ConfigurationSectionController(uiLauncher, applicationProperties, this);
        menuSectionController = new MenuSectionController(uiLauncher, applicationProperties, this);
        pathsSectionController = new PathsSectionController(uiLauncher, applicationProperties, this);
        datesSectionController = new DatesSectionController(uiLauncher, applicationProperties);
        additionalSettingsSectionController = new AdditionalSettingsSectionController(uiLauncher, applicationProperties);
    }

    private Map<String, Object> initToolkitSectionMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("toolkitUsernameTextField", toolkitUsernameTextField);
        map.put("toolkitPasswordField", toolkitPasswordField);
        map.put("toolkitDomainTextField", toolkitDomainTextField);
        map.put("verifyCredentialsHyperlink", verifyCredentialsHyperlink);
        map.put("verifyProgressIndicator", verifyProgressIndicator);
        return map;
    }

    private Map<String, Object> initConfigurationSectionMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("configurationNameComboBox", configurationNameComboBox);
        map.put("configurationNameTextField", configurationNameTextField);
        map.put("addConfigurationButton", addConfigurationButton);
        map.put("removeConfigurationButton", removeConfigurationButton);
        map.put("saveConfigurationButton", saveConfigurationButton);
        map.put("currentWeekNumberLabel", currentWeekNumberLabel);
        return map;
    }

    private Map<String, MenuItem> initMenuSectionMap() {
        Map<String, MenuItem> map = new HashMap<>();
        map.put("applicationMenuItem", applicationMenuItem);
        map.put("toolkitMenuItem", toolkitMenuItem);
        map.put("readMeMenuItem", readMeMenuItem);
        map.put("instructionMenuItem", instructionMenuItem);
        map.put("upgradeMenuItem", upgradeMenuItem);
        map.put("wizardMenuItem", wizardMenuItem);
        map.put("wikiMenuItem", wikiMenuItem);
        map.put("importCertMenuItem", importCertMenuItem);
        map.put("importCertProgrammaticMenuItem", importCertProgrammaticMenuItem);
        return map;
    }

    private Map<String, Object> initPathsSectionMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("projectPathLabel", projectPathLabel);
        map.put("itemPathLabel", itemPathLabel);
        map.put("itemFileNamePrefixTextField", itemFileNamePrefixTextField);
        map.put("projectPathButton", projectPathButton);
        map.put("itemPathButton", itemPathButton);
        return map;
    }

    private Map<String, Object> initDatesSectionMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("startDatePicker", startDatePicker);
        map.put("endDatePicker", endDatePicker);
        map.put("useLastItemDateCheckbox", useLastItemDateCheckbox);
        return map;
    }

    private Map<String, CheckBox> initAdditionalSettingsSectionMap() {
        Map<String, CheckBox> map = new HashMap<>();
        map.put("deleteDownloadedFilesCheckBox", deleteDownloadedFilesCheckBox);
        map.put("skipRemoteCheckBox", skipRemoteCheckBox);
        map.put("fetchAllCheckBox", fetchAllCheckBox);
        return map;
    }

    public void setVcsService(VcsService vcsService) {
        this.vcsService = vcsService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        toolkitSectionController.initialize(location, resources, initToolkitSectionMap());
        configurationSectionController.initialize(location, resources, initConfigurationSectionMap());
        menuSectionController.initialize(location, resources, initMenuSectionMap());
        pathsSectionController.initialize(location, resources, initPathsSectionMap());
        datesSectionController.initialize(location, resources, initDatesSectionMap());
        additionalSettingsSectionController.initialize(location, resources, initAdditionalSettingsSectionMap());

        setInitValues();
        setProperties();
        setActions();
        setListeners();
        setAccelerators();
    }

    private void setInitValues() {
        authorsTextField.setText(String.join(",", applicationProperties.authors()));
        committerEmailTextField.setText(applicationProperties.committerEmail());
        gitAuthorTextField.setText(applicationProperties.gitAuthor());
        mercurialAuthorTextField.setText(applicationProperties.mercurialAuthor());
        svnAuthorTextField.setText(applicationProperties.svnAuthor());
        itemTypeComboBox.setItems(FXCollections.observableArrayList(ItemType.values()));
        itemTypeComboBox.setValue(applicationProperties.itemType());

        toolkitProjectListNamesTextField.setText(String.join(",", applicationProperties.toolkitProjectListNames()));
    }

    private void setProperties() {
        toolkitProjectListNamesTextField.setDisable(
                !EnumSet.of(ItemType.TOOLKIT_DOCS).contains(applicationProperties.itemType())
        );
        setTooltipOnProjectListNames();

        setDisable(applicationProperties.itemType());

        loadProgressIndicator.setVisible(false);
        configurationSectionController.setDisableDependOnConfigurations();

        useDefaultAuthorCheckBox.setDisable(disableDefaultAuthor());
        setTooltipOnUseDefaultAuthor();
        useDefaultEmailCheckBox.setDisable(disableDefaultEmail());
        setTooltipOnUseDefaultEmail();
    }

    private boolean disableDefaultAuthor() {
        boolean disabled = true;
        if (EnumSet.of(ItemType.STATEMENT, ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS)
                .contains(applicationProperties.itemType())) {

            Set<VersionControlSystem> vcsSet = applicationProperties.projectPaths()
                    .stream()
                    .map(projectPath -> VersionControlSystem.valueFrom(Paths.get(projectPath).toFile()))
                    .collect(toSet());

            if (vcsSet.contains(VersionControlSystem.GIT)) {
                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                Optional<String> userName = vcsService.getUserName();
                if (userName.isPresent()) {
                    disabled = applicationProperties.authors().contains(userName.get());
                    disabled |= StringUtils.notEmpty(applicationProperties.gitAuthor()) &&
                            !userName.get().equals(applicationProperties.gitAuthor());
                }
            }
        }
        return disabled;
    }

    private void setTooltipOnUseDefaultAuthor() {
        vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
        String userName = vcsService.getUserName().orElseGet(() -> "");
        Tooltip tooltip = new Tooltip(BundleUtils.getMsg("vcs.panel.useDefaultAuthor.tooltip", userName));
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.setFont(Font.font("Courier New", 16));
        useDefaultAuthorCheckBox.setTooltip(tooltip);
    }

    private boolean disableDefaultEmail() {
        boolean disabled = true;
        if (EnumSet.of(ItemType.STATEMENT, ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS)
                .contains(applicationProperties.itemType())) {

            Set<VersionControlSystem> vcsSet = applicationProperties.projectPaths()
                    .stream()
                    .map(projectPath -> VersionControlSystem.valueFrom(Paths.get(projectPath).toFile()))
                    .collect(toSet());
            if (vcsSet.contains(VersionControlSystem.GIT) &&
                    StringUtils.notEmpty(applicationProperties.committerEmail())) {

                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                Optional<String> userEmail = vcsService.getUserEmail();

                if (userEmail.isPresent()) {
                    disabled = userEmail.get().equals(applicationProperties.committerEmail());
                }
            }
        }
        return disabled;
    }

    private void setTooltipOnUseDefaultEmail() {
        vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
        String userEmail = vcsService.getUserEmail().orElseGet(() -> "");
        Tooltip tooltip = new Tooltip(BundleUtils.getMsg("vcs.panel.useDefaultEmail.tooltip", userEmail));
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.setFont(Font.font("Courier New", 16));
        useDefaultEmailCheckBox.setTooltip(tooltip);
    }

    private void setAccelerators() {
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isAltDown() || KeyCode.ALT_GRAPH == e.getCode()) {
                e.consume();
            } else if (e.isControlDown()) {
                switch (e.getCode()) {
                    case ENTER:
                        if (e.isShiftDown()) {
                            executeAll();
                        } else {
                            execute();
                        }
                        break;
                    case J:
                        uiLauncher.showJobWindow();
                        break;
                    case ESCAPE:
                        UILauncher.platformExit();
                        break;
                    case M:
                        uiLauncher.hideMainWindow();
                        CacheManager.clearAllCache();
                        break;
                    case S:
                        saveConfiguration();
                        break;
                }
            }
        });
    }

    private void setTooltipOnProjectListNames() {
        if (!toolkitProjectListNamesTextField.isDisabled()) {
            Tooltip tooltip = new Tooltip(BundleUtils.getMsg("toolkit.panel.projectListNames.tooltip"));
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.setFont(Font.font("Courier New", 16));
            toolkitProjectListNamesTextField.setTooltip(tooltip);
        } else {
            toolkitProjectListNamesTextField.setTooltip(null);
        }
    }

    void setDisableDependOnConfigurations() {
        Map<String, RunConfig> runConfigMap = applicationProperties.getRunConfigMap();
        executeButton.setDisable(runConfigMap.isEmpty());
        executeAllButton.setDisable(runConfigMap.isEmpty());
        jobButton.setDisable(runConfigMap.isEmpty());
        pathsSectionController.setDisableProjectPathButton(
                runConfigMap.isEmpty() || itemTypeComboBox.getValue() == ItemType.STATEMENT
        );
    }

    private void setActions() {
        itemTypeComboBox.setOnAction(uploadTypeActionEventHandler());
        executeButton.setOnAction(executeActionEventHandler());
        executeAllButton.setOnAction(executeAllActionEventHandler());
        jobButton.setOnAction(jobActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
    }

    private EventHandler<ActionEvent> executeActionEventHandler() {
        return event -> execute();
    }

    private void execute() {
        RunConfig runConfig = createRunConfigFromUI();
        ToolkitConfig toolkitConfig = toolkitSectionController.createToolkitConfigFromUI();
        ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(Stream.concat(
                Arrays.stream(runConfig.toArgumentArray()),
                Arrays.stream(toolkitConfig.toArgumentArray())
        ).toArray(String[]::new));

        FXMultiRunner runner = new FXMultiRunner(
                Stream.of(uiAppProperties).collect(toList()),
                uiLauncher.nonUIExecutor(),
                RunType.EXECUTE
        );
        resetIndicatorProperties(runner);
        uiLauncher.executeOutsideUIThread(() -> {
            runner.start();
            if (uiAppProperties.isActiveTray()) {
                uiLauncher.updateTray(uiAppProperties);
            }
            updateLastItemUploadDate();
        });
    }

    private void updateLastItemUploadDate() {
        try {
            ProgramData programData = dataService.loadProgramData();
            if (programData.getUploadStatus() != null &&
                    EnumSet.of(UploadStatus.SUCCESS, UploadStatus.PARTIAL_SUCCESS).contains(programData.getUploadStatus())) {
                uiLauncher.setLastItemSubmissionDate(null);
            }
        } catch (Exception ex) {
            logger.warn("Could not determine the status of the upload. {}. Forcing to refresh last upload date.",
                    ex.getMessage());
            uiLauncher.setLastItemSubmissionDate(null);
        } finally {
            datesSectionController.setLastItemSubmissionDate();
        }
    }

    private EventHandler<ActionEvent> executeAllActionEventHandler() {
        return event -> executeAll();
    }

    private void executeAll() {
        RunConfig runConfig = createRunConfigFromUI();
        ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(runConfig.toArgumentArray());
        Map<String, ApplicationProperties> map = CacheManager.getAllApplicationProperties();
        map.put(uiAppProperties.configurationName(), uiAppProperties);

        FXMultiRunner runner = new FXMultiRunner(map.values(), uiLauncher.nonUIExecutor(), RunType.EXECUTE_ALL);
        resetIndicatorProperties(runner);
        uiLauncher.executeOutsideUIThread(() -> {
            runner.call();
            ApplicationProperties instance = new LinkedList<>(map.values()).getFirst();
            if (instance.isActiveTray()) {
                uiLauncher.updateTray(instance);
            }
        });
    }

    RunConfig createRunConfigFromUI() {
        RunConfig runConfig = new RunConfig();

        if (!StringUtils.nullOrEmpty(authorsTextField.getText())) {
            runConfig.setAuthor(authorsTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(committerEmailTextField.getText())) {
            runConfig.setCommitterEmail(committerEmailTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(gitAuthorTextField.getText())) {
            runConfig.setGitAuthor(gitAuthorTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(mercurialAuthorTextField.getText())) {
            runConfig.setMercurialAuthor(mercurialAuthorTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(svnAuthorTextField.getText())) {
            runConfig.setSvnAuthor(svnAuthorTextField.getText());
        }
        runConfig.setItemType(itemTypeComboBox.getValue());
        runConfig.setSkipRemote(skipRemoteCheckBox.isSelected());
        runConfig.setFetchAll(fetchAllCheckBox.isSelected());

        if (!StringUtils.nullOrEmpty(toolkitProjectListNamesTextField.getText())) {
            runConfig.setToolkitProjectListNames(toolkitProjectListNamesTextField.getText());
        }
        runConfig.setDeleteDownloadedFiles(deleteDownloadedFilesCheckBox.isSelected());

        runConfig.setProjectPath(pathsSectionController.getProjectPathLabelValue());
        runConfig.setItemPath(pathsSectionController.getItemPathLabelValue());
        if (!StringUtils.nullOrEmpty(pathsSectionController.getItemFileNamePrefix())) {
            runConfig.setItemFileNamePrefix(pathsSectionController.getItemFileNamePrefix());
        }

        runConfig.setStartDate(datesSectionController.getStartDate());
        runConfig.setEndDate(datesSectionController.getEndDate());

        runConfig.setConfigurationName(configurationNameTextField.getText());
        runConfig.setPreferredArgSource(PreferredArgSource.UI);

        return runConfig;
    }

    private void resetIndicatorProperties(Task<?> task) {
        loadProgressIndicator.setVisible(true);
        loadProgressIndicator.progressProperty().unbind();
        loadProgressIndicator.progressProperty().bind(task.progressProperty());
        infoLabel.textProperty().unbind();
        infoLabel.textProperty().bind(task.messageProperty());
    }

    private EventHandler<ActionEvent> uploadTypeActionEventHandler() {
        return event -> {
            boolean disableProjectButton = itemTypeComboBox.getValue() == ItemType.STATEMENT;
            disableProjectButton |= applicationProperties.getRunConfigMap().isEmpty() &&
                    configurationNameTextField.getText().isEmpty();
            pathsSectionController.setDisableProjectPathButton(disableProjectButton);
            if (itemTypeComboBox.getValue() == ItemType.TOOLKIT_DOCS) {
                datesSectionController.setEndDatePicker(LocalDate.now());
            }
            toolkitProjectListNamesTextField.setDisable(itemTypeComboBox.getValue() != ItemType.TOOLKIT_DOCS);
            additionalSettingsSectionController.disableDeleteDownloadedFiles(
                    !EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS).contains(itemTypeComboBox.getValue())
            );
            setDisable(itemTypeComboBox.getValue());
            setTooltipOnProjectListNames();
        };
    }

    private void setDisable(ItemType itemType) {
        boolean disable = EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.STATEMENT, ItemType.SHARE_POINT_DOCS).contains(itemType);
        authorsTextField.setDisable(disable);
        committerEmailTextField.setDisable(disable);
        gitAuthorTextField.setDisable(disable);
        svnAuthorTextField.setDisable(disable);
        mercurialAuthorTextField.setDisable(disable);
        datesSectionController.disableEndDatePicker(itemType);
        additionalSettingsSectionController.disableSkipRemote(disable);
        additionalSettingsSectionController.disableFetchAll(disable);
    }

    private EventHandler<ActionEvent> jobActionEventHandler() {
        return event -> uiLauncher.showJobWindow();
    }

    private EventHandler<ActionEvent> exitActionEventHandler() {
        return event -> UILauncher.platformExit();
    }

    private void saveConfiguration() {
        configurationSectionController.saveConfiguration();
    }

    void updateRunConfig(String oldConfigName, String newConfigName) {
        ToolkitConfig toolkitConfigFromUI = toolkitSectionController.createToolkitConfigFromUI();
        applicationProperties.updateToolkitConfig(toolkitConfigFromUI);
        if (!StringUtils.nullOrEmpty(configurationNameTextField.getText())) {
            RunConfig runConfigWithoutDates = getRunConfigWithoutDates();
            applicationProperties.updateCurrentRunConfig(runConfigWithoutDates);
            new JobHelper().updateJobConfigs(oldConfigName, newConfigName);
            configurationSectionController.setDisableDependOnConfigurations();
        }
        applicationProperties.save();
        if (!StringUtils.nullOrEmpty(oldConfigName) && !newConfigName.equals(oldConfigName)) {
            applicationProperties.removeConfig(oldConfigName);
        }
    }

    RunConfig getRunConfigWithoutDates() {
        RunConfig runConfigFromUI = createRunConfigFromUI();
        runConfigFromUI.setStartDate(null);
        runConfigFromUI.setEndDate(null);
        return runConfigFromUI;
    }

    private void setListeners() {
        useDefaultAuthorCheckBox.selectedProperty().addListener(useDefaultAuthorChangeListener());
        useDefaultEmailCheckBox.selectedProperty().addListener(useDefaultEmailChangeListener());
        gitAuthorTextField.focusedProperty().addListener(gitAuthorFocusChangeListener());
        committerEmailTextField.focusedProperty().addListener(committerEmailFocusChangeListener());
    }

    private ChangeListener<? super Boolean> useDefaultAuthorChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                vcsService.getUserName().ifPresent(userName -> gitAuthorTextField.setText(userName));
            } else {
                gitAuthorTextField.setText(applicationProperties.gitAuthor());
            }
        };
    }

    private ChangeListener<? super Boolean> useDefaultEmailChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                vcsService.getUserEmail().ifPresent(userEmail -> committerEmailTextField.setText(userEmail));
            } else {
                committerEmailTextField.setText(applicationProperties.committerEmail());
            }
        };
    }

    private ChangeListener<? super Boolean> gitAuthorFocusChangeListener() {
        return (ChangeListener<Boolean>) (observableValue, oldValue, newValue) -> {
            if (!newValue && !gitAuthorTextField.getText().isEmpty()) {
                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                vcsService.getUserName().ifPresent(userName ->
                        useDefaultAuthorCheckBox.setDisable(gitAuthorTextField.getText().equals(userName))
                );
            }
        };
    }

    private ChangeListener<? super Boolean> committerEmailFocusChangeListener() {
        return (ChangeListener<Boolean>) (observableValue, oldValue, newValue) -> {
            if (!newValue) {
                if (committerEmailTextField.getLength() > 0) {
                    vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                    vcsService.getUserEmail().ifPresent(userEmail ->
                            useDefaultEmailCheckBox.setDisable(committerEmailTextField.getText().equals(userEmail))
                    );
                }
            }
        };
    }

    void deselectUseLastItemDate() {
        datesSectionController.deselectUseLastItemDate();
    }

    void setDisableProjectPathButton(boolean value) {
        pathsSectionController.setDisableProjectPathButton(value);
    }

    void setToolkitCredentialsIfAvailable() {
        toolkitSectionController.setToolkitCredentialsIfAvailable();
    }

    String getConfigurationNameComboBoxValue() {
        return configurationNameComboBox.getValue();
    }

    ItemType getItemType() {
        return itemTypeComboBox.getValue();
    }

    String getConfigurationName() {
        return configurationNameTextField.getText();
    }


    void updateConfigurationNameComboBox(String oldValue, String newValue) {
        configurationSectionController.updateConfigurationNameComboBox(oldValue, newValue);
    }

    public void setLastItemSubmissionDate() {
        datesSectionController.setLastItemSubmissionDate();
    }
}
