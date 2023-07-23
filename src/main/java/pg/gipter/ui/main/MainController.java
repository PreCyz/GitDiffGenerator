package pg.gipter.ui.main;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.DataService;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.ui.*;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class MainController extends AbstractController {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private MenuItem applicationMenuItem;
    @FXML
    private MenuItem toolkitMenuItem;
    @FXML
    private MenuItem instructionMenuItem;
    @FXML
    private MenuItem upgradeMenuItem;
    @FXML
    private MenuItem wizardMenuItem;
    @FXML
    private MenuItem wikiMenuItem;

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
    private TextField toolkitProjectListNamesTextField;

    @FXML
    private TextField toolkitUsernameTextField;
    @FXML
    private PasswordField toolkitPasswordField;
    @FXML
    private Hyperlink verifyCredentialsHyperlink;
    @FXML
    private ProgressIndicator verifyProgressIndicator;

    @FXML
    private CheckBox deleteDownloadedFilesCheckBox;
    @FXML
    private CheckBox skipRemoteCheckBox;
    @FXML
    private CheckBox fetchAllCheckBox;
    @FXML
    private TextField fetchTimeoutTextField;

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
    private Button trayButton;
    @FXML
    private ProgressIndicator loadProgressIndicator;
    @FXML
    private Label infoLabel;
    @FXML
    private ComboBox<String> configurationNameComboBox;
    @FXML
    private Button addConfigurationButton;
    @FXML
    private Button removeConfigurationButton;
    @FXML
    private Button saveConfigurationButton;
    @FXML
    private Label currentWeekNumberLabel;

    private final DataService dataService;

    private final ToolkitSectionController toolkitSectionController;
    private final ConfigurationSectionController configurationSectionController;
    private final MenuSectionController menuSectionController;
    private final PathsSectionController pathsSectionController;
    private final DatesSectionController datesSectionController;
    private final AdditionalSettingsSectionController additionalSettingsSectionController;
    private final CsvDetailsSectionController csvDetailsSectionController;
    private final ButtonController buttonController;

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.dataService = DataService.getInstance();
        toolkitSectionController = new ToolkitSectionController(uiLauncher, applicationProperties);
        configurationSectionController = new ConfigurationSectionController(uiLauncher, applicationProperties, this);
        menuSectionController = new MenuSectionController(uiLauncher, applicationProperties, this);
        pathsSectionController = new PathsSectionController(uiLauncher, applicationProperties, this);
        datesSectionController = new DatesSectionController(uiLauncher, applicationProperties);
        additionalSettingsSectionController = new AdditionalSettingsSectionController(uiLauncher, applicationProperties);
        csvDetailsSectionController = new CsvDetailsSectionController(uiLauncher, applicationProperties, this);
        buttonController = new ButtonController(uiLauncher, applicationProperties, this);
    }

    public void setVcsService(VcsService vcsService) {
        csvDetailsSectionController.setVcsService(vcsService);
    }

    private Map<String, Control> initToolkitSectionMap() {
        Map<String, Control> map = new HashMap<>();
        map.put("toolkitUsernameTextField", toolkitUsernameTextField);
        map.put("toolkitPasswordField", toolkitPasswordField);
        map.put("verifyCredentialsHyperlink", verifyCredentialsHyperlink);
        map.put("verifyProgressIndicator", verifyProgressIndicator);
        return map;
    }

    private Map<String, Control> initConfigurationSectionMap() {
        Map<String, Control> map = new HashMap<>();
        map.put("configurationNameComboBox", configurationNameComboBox);
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
        map.put("instructionMenuItem", instructionMenuItem);
        map.put("upgradeMenuItem", upgradeMenuItem);
        map.put("wizardMenuItem", wizardMenuItem);
        map.put("wikiMenuItem", wikiMenuItem);
        return map;
    }

    private Map<String, Control> initPathsSectionMap() {
        Map<String, Control> map = new HashMap<>();
        map.put("projectPathLabel", projectPathLabel);
        map.put("itemPathLabel", itemPathLabel);
        map.put("itemFileNamePrefixTextField", itemFileNamePrefixTextField);
        map.put("projectPathButton", projectPathButton);
        map.put("itemPathButton", itemPathButton);
        return map;
    }

    private Map<String, Control> initDatesSectionMap() {
        Map<String, Control> map = new HashMap<>();
        map.put("startDatePicker", startDatePicker);
        map.put("endDatePicker", endDatePicker);
        map.put("useLastItemDateCheckbox", useLastItemDateCheckbox);
        return map;
    }

    private Map<String, Control> initAdditionalSettingsSectionMap() {
        Map<String, Control> map = new HashMap<>();
        map.put("deleteDownloadedFilesCheckBox", deleteDownloadedFilesCheckBox);
        map.put("skipRemoteCheckBox", skipRemoteCheckBox);
        map.put("fetchAllCheckBox", fetchAllCheckBox);
        map.put("fetchTimeoutTextField", fetchTimeoutTextField);
        return map;
    }

    private Map<String, Object> initCsvDetailsSectionMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("authorsTextField", authorsTextField);
        map.put("committerEmailTextField", committerEmailTextField);
        map.put("gitAuthorTextField", gitAuthorTextField);
        map.put("mercurialAuthorTextField", mercurialAuthorTextField);
        map.put("svnAuthorTextField", svnAuthorTextField);
        map.put("itemTypeComboBox", itemTypeComboBox);
        map.put("toolkitProjectListNamesTextField", toolkitProjectListNamesTextField);
        map.put("useDefaultAuthorCheckBox", useDefaultAuthorCheckBox);
        map.put("useDefaultEmailCheckBox", useDefaultEmailCheckBox);
        return map;
    }

    private Map<String, Button> initButtonMap() {
        Map<String, Button> map = new HashMap<>();
        map.put("executeButton", executeButton);
        map.put("executeAllButton", executeAllButton);
        map.put("jobButton", jobButton);
        map.put("exitButton", exitButton);
        map.put("trayButton", trayButton);
        return map;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        toolkitSectionController.initialize(location, resources, initToolkitSectionMap());
        configurationSectionController.initialize(location, resources, initConfigurationSectionMap());
        menuSectionController.initialize(location, resources, initMenuSectionMap());
        //this is executed through configurationSectionController
        //pathsSectionController.initialize(location, resources, initPathsSectionMap());
        //datesSectionController.initialize(location, resources, initDatesSectionMap());
        additionalSettingsSectionController.initialize(location, resources, initAdditionalSettingsSectionMap());
        //this is executed later in the flow
        //csvDetailsSectionController.initialize(location, resources, initCsvDetailsSectionMap());
        buttonController.initialize(location, resources, initButtonMap());

        setProperties();
        setAccelerators();
    }

    private void setProperties() {
        loadProgressIndicator.setVisible(false);
        configurationSectionController.setDisableDependOnConfigurations();

        setDisable(applicationProperties.itemType());
    }

    private void setAccelerators() {
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isAltDown() || KeyCode.ALT_GRAPH == e.getCode()) {
                e.consume();
            } else if (e.isControlDown()) {
                switch (e.getCode()) {
                    case ENTER:
                        if (e.isShiftDown()) {
                            buttonController.executeAll();
                        } else {
                            buttonController.execute();
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
                    default:
                        break;
                }
            }
        });
    }

    void setDisableDependOnConfigurations() {
        Map<String, RunConfig> runConfigMap = applicationProperties.getRunConfigMap();
        buttonController.setDisableDependOnConfigurations(runConfigMap.isEmpty());
        pathsSectionController.setDisableProjectPathButton(
                runConfigMap.isEmpty() || csvDetailsSectionController.getItemType() == ItemType.STATEMENT
        );
    }

    void updateLastItemUploadDate() {
        try {
            ProgramData programData = dataService.loadProgramData();
            if (programData.getUploadStatus() != null && UploadStatus.isSuccess(programData.getUploadStatus())) {
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

    RunConfig createRunConfigFromUI() {
        RunConfig runConfig = new RunConfig();

        if (!StringUtils.nullOrEmpty(csvDetailsSectionController.getAuthors())) {
            runConfig.setAuthor(csvDetailsSectionController.getAuthors());
        }
        if (!StringUtils.nullOrEmpty(csvDetailsSectionController.getCommitterEmail())) {
            runConfig.setCommitterEmail(csvDetailsSectionController.getCommitterEmail());
        }
        if (!StringUtils.nullOrEmpty(csvDetailsSectionController.getGitAuthor())) {
            runConfig.setGitAuthor(csvDetailsSectionController.getGitAuthor());
        }
        if (!StringUtils.nullOrEmpty(csvDetailsSectionController.getMercurialAuthor())) {
            runConfig.setMercurialAuthor(csvDetailsSectionController.getMercurialAuthor());
        }
        if (!StringUtils.nullOrEmpty(csvDetailsSectionController.getSvnAuthor())) {
            runConfig.setSvnAuthor(csvDetailsSectionController.getSvnAuthor());
        }
        runConfig.setItemType(csvDetailsSectionController.getItemType());
        if (!StringUtils.nullOrEmpty(csvDetailsSectionController.getToolkitProjectListNames())) {
            runConfig.setToolkitProjectListNames(csvDetailsSectionController.getToolkitProjectListNames());
        }

        runConfig.setDeleteDownloadedFiles(additionalSettingsSectionController.getDeleteDownloadedFiles());
        runConfig.setSkipRemote(additionalSettingsSectionController.getSkipRemote());
        runConfig.setFetchAll(additionalSettingsSectionController.getFetchAll());
        runConfig.setFetchTimeout(additionalSettingsSectionController.getFetchTimeout());

        runConfig.setProjectPath(pathsSectionController.getProjectPaths());
        runConfig.setItemPath(pathsSectionController.getItemPathLabelValue());
        if (!StringUtils.nullOrEmpty(pathsSectionController.getItemFileNamePrefix())) {
            runConfig.setItemFileNamePrefix(pathsSectionController.getItemFileNamePrefix());
        }

        runConfig.setStartDate(datesSectionController.getStartDate());
        runConfig.setEndDate(datesSectionController.getEndDate());

        runConfig.setConfigurationName(configurationSectionController.getConfigurationName());
        runConfig.setPreferredArgSource(PreferredArgSource.UI);

        return runConfig;
    }

    void resetIndicatorProperties(Task<?> task) {
        loadProgressIndicator.setVisible(true);
        loadProgressIndicator.progressProperty().unbind();
        loadProgressIndicator.progressProperty().bind(task.progressProperty());
        infoLabel.textProperty().unbind();
        infoLabel.textProperty().bind(task.messageProperty());
    }

    void setDisable(ItemType itemType) {
        boolean disable = !ItemType.isCodeRelated(itemType);
        datesSectionController.disableEndDatePicker(itemType);
        additionalSettingsSectionController.disableSkipRemote(disable);
        additionalSettingsSectionController.disableFetchAll(disable);
    }

    private void saveConfiguration() {
        configurationSectionController.saveConfiguration();
    }

    void updateRunConfig() {
        ToolkitConfig toolkitConfigFromUI = toolkitSectionController.createToolkitConfigFromUI();
        applicationProperties.updateToolkitConfig(toolkitConfigFromUI);
        configurationSectionController.getConfigurationName();
        RunConfig runConfigWithoutDates = getRunConfigWithoutDates();
        applicationProperties.updateCurrentRunConfig(runConfigWithoutDates);
        configurationSectionController.setDisableDependOnConfigurations();
        applicationProperties.save();
    }

    RunConfig getRunConfigWithoutDates() {
        RunConfig runConfigFromUI = createRunConfigFromUI();
        runConfigFromUI.setStartDate(null);
        runConfigFromUI.setEndDate(null);
        return runConfigFromUI;
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
        return csvDetailsSectionController.getItemType();
    }

    String getConfigurationName() {
        return configurationSectionController.getConfigurationName();
    }

    void updateConfigurationNameComboBox(String oldValue, String newValue) {
        configurationSectionController.updateConfigurationNameComboBox(oldValue, newValue);
    }

    void setLastItemSubmissionDate() {
        datesSectionController.setLastItemSubmissionDate();
    }

    void setEndDatePicker(LocalDate localDate) {
        datesSectionController.setEndDatePicker(localDate);
    }

    void disableDeleteDownloadedFiles(boolean disable) {
        additionalSettingsSectionController.disableDeleteDownloadedFiles(disable);
    }

    ToolkitConfig createToolkitConfigFromUI() {
        return toolkitSectionController.createToolkitConfigFromUI();
    }

    void setInitValues(ApplicationProperties applicationProperties) {
        setApplicationProperties(applicationProperties);

        toolkitSectionController.initialize(location, resources, initToolkitSectionMap());
        pathsSectionController.initialize(location, resources, initPathsSectionMap());
        datesSectionController.initialize(location, resources, initDatesSectionMap());
        additionalSettingsSectionController.initialize(location, resources, initAdditionalSettingsSectionMap());
        csvDetailsSectionController.initialize(location, resources, initCsvDetailsSectionMap());
        buttonController.initialize(location, resources, initButtonMap());
    }

    @Override
    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        super.setApplicationProperties(applicationProperties);
        toolkitSectionController.setApplicationProperties(applicationProperties);
        configurationSectionController.setApplicationProperties(applicationProperties);
        menuSectionController.setApplicationProperties(applicationProperties);
        pathsSectionController.setApplicationProperties(applicationProperties);
        datesSectionController.setApplicationProperties(applicationProperties);
        additionalSettingsSectionController.setApplicationProperties(applicationProperties);
        csvDetailsSectionController.setApplicationProperties(applicationProperties);
        buttonController.setApplicationProperties(applicationProperties);
    }
}
