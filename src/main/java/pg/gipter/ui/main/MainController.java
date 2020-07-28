package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import pg.gipter.core.*;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.services.*;
import pg.gipter.services.keystore.CertificateServiceFactory;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.services.vcs.VcsServiceFactory;
import pg.gipter.ui.*;
import pg.gipter.utils.*;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static pg.gipter.core.ApplicationProperties.yyyy_MM_dd;

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
    private CheckBox skipRemoteCheckBox;
    @FXML
    private CheckBox fetchAllCheckBox;
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
    private CheckBox deleteDownloadedFilesCheckBox;
    @FXML
    private ProgressIndicator verifyProgressIndicator;

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
    @FXML
    private CheckBox useLastItemDateCheckbox;

    private final DataService dataService;

    private final Set<String> definedPatterns;
    private String currentItemName = "";
    private String inteliSense = "";
    private boolean useInteliSense = false;
    private VcsService vcsService;
    private final ToolkitSectionController toolkitSectionController;
    private final ConfigurationSectionController configurationSectionController;
    private final MenuSectionController menuSectionController;

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.dataService = DataService.getInstance();
        this.definedPatterns = EnumSet.allOf(NamePatternValue.class)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.vcsService = VcsServiceFactory.getInstance();
        toolkitSectionController = new ToolkitSectionController(uiLauncher, applicationProperties);
        configurationSectionController = new ConfigurationSectionController(uiLauncher, applicationProperties, this);
        menuSectionController = new MenuSectionController(uiLauncher, applicationProperties, this);
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

    public void setVcsService(VcsService vcsService) {
        this.vcsService = vcsService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        toolkitSectionController.initialize(location, resources, initToolkitSectionMap());
        configurationSectionController.initialize(location, resources, initConfigurationSectionMap());
        menuSectionController.initialize(location, resources, initMenuSectionMap());

        setInitValues();
        setProperties(resources);
        setActions(resources);
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
        skipRemoteCheckBox.setSelected(applicationProperties.isSkipRemote());
        fetchAllCheckBox.setSelected(applicationProperties.isFetchAll());

        projectPathLabel.setText(String.join(",", applicationProperties.projectPaths()));
        String itemFileName = Paths.get(applicationProperties.itemPath()).getFileName().toString();
        String itemPath = applicationProperties.itemPath().substring(0, applicationProperties.itemPath().indexOf(itemFileName) - 1);
        itemPathLabel.setText(itemPath);
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            itemPathLabel.setText(applicationProperties.itemPath());
        }
        projectPathLabel.setTooltip(buildPathTooltip(projectPathLabel.getText()));
        itemPathLabel.setTooltip(buildPathTooltip(itemPathLabel.getText()));
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());
        toolkitProjectListNamesTextField.setText(String.join(",", applicationProperties.toolkitProjectListNames()));
        deleteDownloadedFilesCheckBox.setSelected(applicationProperties.isDeleteDownloadedFiles());

        LocalDate now = LocalDate.now();
        LocalDate initStartDate = now.minusDays(applicationProperties.periodInDays());
        startDatePicker.setValue(initStartDate);
        if (!initStartDate.isEqual(applicationProperties.startDate())) {
            startDatePicker.setValue(applicationProperties.startDate());
        }
        endDatePicker.setValue(now);
        if (!now.isEqual(applicationProperties.endDate())) {
            endDatePicker.setValue(applicationProperties.endDate());
        }

        setLastItemSubmissionDate();
    }

    void setLastItemSubmissionDate() {
        uiLauncher.executeOutsideUIThread(() -> {
            if (uiLauncher.getLastItemSubmissionDate() == null) {
                Optional<String> submissionDate = new ToolkitService(applicationProperties).lastItemUploadDate();
                if (submissionDate.isPresent()) {
                    uiLauncher.setLastItemSubmissionDate(LocalDateTime.parse(submissionDate.get(), DateTimeFormatter.ISO_DATE_TIME));
                    Platform.runLater(() -> {
                        useLastItemDateCheckbox.setDisable(uiLauncher.getLastItemSubmissionDate() == null);
                        useLastItemDateCheckbox.setText(BundleUtils.getMsg(
                                "main.lastUploadDate",
                                uiLauncher.getLastItemSubmissionDate().format(DateTimeFormatter.ISO_DATE)
                        ));
                    });
                } else {
                    uiLauncher.setLastItemSubmissionDate(null);
                    Platform.runLater(() -> {
                        useLastItemDateCheckbox.setDisable(uiLauncher.getLastItemSubmissionDate() == null);
                        useLastItemDateCheckbox.setText(BundleUtils.getMsg("main.lastUploadDate.unavailable"));
                    });
                }
            } else {
                Platform.runLater(() -> {
                    useLastItemDateCheckbox.setDisable(uiLauncher.getLastItemSubmissionDate() == null);
                    useLastItemDateCheckbox.setText(BundleUtils.getMsg(
                            "main.lastUploadDate",
                            uiLauncher.getLastItemSubmissionDate().format(DateTimeFormatter.ISO_DATE)
                    ));
                });
            }
        });
    }

    private void setProperties(ResourceBundle resources) {
        toolkitProjectListNamesTextField.setDisable(
                !EnumSet.of(ItemType.TOOLKIT_DOCS).contains(applicationProperties.itemType())
        );
        setTooltipOnProjectListNames();
        deleteDownloadedFilesCheckBox.setDisable(
                !EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS).contains(applicationProperties.itemType())
        );

        if (applicationProperties.projectPaths().isEmpty()) {
            projectPathButton.setText(resources.getString("button.add"));
        } else {
            projectPathButton.setText(resources.getString("button.change"));
        }

        if (StringUtils.nullOrEmpty(applicationProperties.itemPath())) {
            itemPathButton.setText(resources.getString("button.add"));
        } else {
            itemPathButton.setText(resources.getString("button.change"));
        }

        startDatePicker.setConverter(dateConverter());
        endDatePicker.setConverter(dateConverter());

        setDisable(applicationProperties.itemType());

        loadProgressIndicator.setVisible(false);
        instructionMenuItem.setDisable(!(Paths.get("Gipter-ui-description.pdf").toFile().exists() && Desktop.isDesktopSupported()));
        configurationSectionController.setDisableDependOnConfigurations();

        TextFields.bindAutoCompletion(itemFileNamePrefixTextField, itemNameSuggestionsCallback());
        setUpgradeMenuItemDisabled();

        final boolean enableImportCert = StringUtils.notEmpty(System.getProperty("java.home")) &&
                applicationProperties.isCertImportEnabled() &&
                CertificateServiceFactory.getInstance(true).hasCertToImport();
        importCertMenuItem.setDisable(!enableImportCert);
        importCertProgrammaticMenuItem.setDisable(!enableImportCert);
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

    private StringConverter<LocalDate> dateConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate object) {
                return object.format(yyyy_MM_dd);
            }

            @Override
            public LocalDate fromString(String string) {
                return LocalDate.parse(string, yyyy_MM_dd);
            }
        };
    }

    void setDisableDependOnConfigurations() {
        Map<String, RunConfig> runConfigMap = applicationProperties.getRunConfigMap();
        executeButton.setDisable(runConfigMap.isEmpty());
        executeAllButton.setDisable(runConfigMap.isEmpty());
        jobButton.setDisable(runConfigMap.isEmpty());
        projectPathButton.setDisable(runConfigMap.isEmpty() || itemTypeComboBox.getValue() == ItemType.STATEMENT);
    }

    private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> itemNameSuggestionsCallback() {
        return param -> {
            Collection<String> result = new HashSet<>();
            if (useInteliSense) {
                result = definedPatterns;
                if (!inteliSense.isEmpty()) {
                    result = definedPatterns.stream()
                            .filter(pattern -> pattern.startsWith(inteliSense))
                            .collect(toCollection(LinkedHashSet::new));
                }
            }
            return result;
        };
    }

    private void setUpgradeMenuItemDisabled() {
        uiLauncher.executeOutsideUIThread(() -> {
            logger.info("Checking new version.");
            GithubService service = new GithubService(applicationProperties.version());
            final boolean newVersion = service.isNewVersion();
            if (newVersion) {
                logger.info("New version [{}] available.", service.getServerVersion());
            } else {
                logger.info("This version is up to date.");
            }
            Platform.runLater(() -> upgradeMenuItem.setDisable(!newVersion));
        });
    }

    private void setActions(ResourceBundle resources) {
        projectPathButton.setOnAction(projectPathActionEventHandler());
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        itemTypeComboBox.setOnAction(uploadTypeActionEventHandler());
        executeButton.setOnAction(executeActionEventHandler());
        executeAllButton.setOnAction(executeAllActionEventHandler());
        jobButton.setOnAction(jobActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
        itemFileNamePrefixTextField.setOnKeyReleased(itemNameKeyReleasedEventHandler());
    }

    private EventHandler<ActionEvent> projectPathActionEventHandler() {
        return event -> {
            RunConfig runConfig = createRunConfigFromUI();
            applicationProperties.updateCurrentRunConfig(runConfig);
            uiLauncher.setApplicationProperties(applicationProperties);
            String configurationName = configurationNameTextField.getText();
            configurationSectionController.updateConfigurationNameComboBox(configurationName, configurationName);
            uiLauncher.showProject(itemTypeComboBox.getValue());
        };
    }

    private Tooltip buildPathTooltip(String result) {
        String[] paths = result.split(",");
        StringBuilder builder = new StringBuilder();
        Arrays.asList(paths).forEach(path -> builder.append(path).append("\n"));
        Tooltip tooltip = new Tooltip(builder.toString());
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.setFont(Font.font("Courier New", 14));
        return tooltip;
    }

    private EventHandler<ActionEvent> itemPathActionEventHandler(final ResourceBundle resources) {
        return event -> {
            if (itemTypeComboBox.getValue() == ItemType.STATEMENT) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle(resources.getString("directory.item.statement.title"));
                File statementFile = fileChooser.showOpenDialog(uiLauncher.currentWindow());
                if (statementFile != null && statementFile.exists() && statementFile.isFile()) {
                    itemPathLabel.setText(statementFile.getAbsolutePath());
                    itemPathButton.setText(resources.getString("button.open"));
                    itemPathLabel.getTooltip().setText(statementFile.getAbsolutePath());
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                directoryChooser.setTitle(resources.getString("directory.item.store"));
                File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
                if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                    itemPathLabel.setText(itemPathDirectory.getAbsolutePath());
                    itemPathButton.setText(resources.getString("button.change"));
                    itemPathLabel.getTooltip().setText(itemPathDirectory.getAbsolutePath());
                }
            }
        };
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
            setLastItemSubmissionDate();
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

        runConfig.setProjectPath(projectPathLabel.getText());
        runConfig.setItemPath(itemPathLabel.getText());
        if (!StringUtils.nullOrEmpty(itemFileNamePrefixTextField.getText())) {
            runConfig.setItemFileNamePrefix(itemFileNamePrefixTextField.getText());
        }

        runConfig.setStartDate(startDatePicker.getValue());
        runConfig.setEndDate(endDatePicker.getValue());

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
            projectPathButton.setDisable(disableProjectButton);
            if (itemTypeComboBox.getValue() == ItemType.TOOLKIT_DOCS) {
                endDatePicker.setValue(LocalDate.now());
            }
            toolkitProjectListNamesTextField.setDisable(itemTypeComboBox.getValue() != ItemType.TOOLKIT_DOCS);
            deleteDownloadedFilesCheckBox.setDisable(
                    !EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS).contains(itemTypeComboBox.getValue())
            );
            setDisable(itemTypeComboBox.getValue());
            setTooltipOnProjectListNames();
        };
    }

    private void setDisable(ItemType uploadType) {
        boolean disable = EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.STATEMENT, ItemType.SHARE_POINT_DOCS).contains(uploadType);
        authorsTextField.setDisable(disable);
        committerEmailTextField.setDisable(disable);
        gitAuthorTextField.setDisable(disable);
        svnAuthorTextField.setDisable(disable);
        mercurialAuthorTextField.setDisable(disable);
        skipRemoteCheckBox.setDisable(disable);
        fetchAllCheckBox.setDisable(disable);
        endDatePicker.setDisable(EnumSet.of(ItemType.TOOLKIT_DOCS, ItemType.SHARE_POINT_DOCS).contains(uploadType));
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

    private EventHandler<KeyEvent> itemNameKeyReleasedEventHandler() {
        return event -> {
            if (event.getCode() == KeyCode.ENTER) {
                itemFileNamePrefixTextField.setText(currentItemName);
                itemFileNamePrefixTextField.positionCaret(currentItemName.length());
            }
        };
    }

    private void setListeners() {
        useLastItemDateCheckbox.selectedProperty().addListener(useListItemCheckBoxListener());
        itemFileNamePrefixTextField.textProperty().addListener(itemFileNameChangeListener());
        useDefaultAuthorCheckBox.selectedProperty().addListener(useDefaultAuthorChangeListener());
        useDefaultEmailCheckBox.selectedProperty().addListener(useDefaultEmailChangeListener());
        gitAuthorTextField.focusedProperty().addListener(gitAuthorFocusChangeListener());
        committerEmailTextField.focusedProperty().addListener(committerEmailFocusChangeListener());
    }

    private ChangeListener<Boolean> useListItemCheckBoxListener() {
        return (observable, oldValue, newValue) -> {
            startDatePicker.setDisable(newValue);
            if (newValue) {
                startDatePicker.setValue(uiLauncher.getLastItemSubmissionDate().toLocalDate());
            } else {
                startDatePicker.setValue(LocalDate.now().minusDays(applicationProperties.periodInDays()));
            }
        };
    }

    private ChangeListener<String> itemFileNameChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue.endsWith("{")) {
                useInteliSense = true;
                inteliSense = "";
            } else if (newValue.endsWith("}") || newValue.isEmpty()) {
                useInteliSense = false;
            }
            if (definedPatterns.contains(newValue)) {
                useInteliSense = false;
                inteliSense = "";
                currentItemName = oldValue.substring(0, oldValue.lastIndexOf("{")) + newValue;
                itemFileNamePrefixTextField.setText(currentItemName);
                itemFileNamePrefixTextField.positionCaret(currentItemName.length());
            } else {
                currentItemName = newValue;
            }

            if (useInteliSense) {
                //letter was added
                if (newValue.length() > oldValue.length()) {
                    inteliSense += newValue.replace(oldValue, "");
                } else { //back space was pressed
                    if (oldValue.endsWith("{")) {
                        inteliSense = "";
                        useInteliSense = false;
                    } else {
                        inteliSense = newValue.substring(newValue.lastIndexOf("{"));
                    }
                }
            }
        };
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
            if (!newValue) {
                if (gitAuthorTextField.getLength() > 0) {
                    vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                    vcsService.getUserEmail().ifPresent(userName ->
                            useDefaultAuthorCheckBox.setDisable(gitAuthorTextField.getText().equals(userName))
                    );
                }
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
        if (useLastItemDateCheckbox.isSelected()) {
            useLastItemDateCheckbox.setSelected(false);
        }
    }

    void setDisableProjectPathButton(boolean value) {
        projectPathButton.setDisable(value);
    }

    void setToolkitCredentialsIfAvailable() {
        toolkitSectionController.setToolkitCredentialsIfAvailable();
    }

    public String getConfigurationNameComboBoxValue() {
        return configurationNameComboBox.getValue();
    }
}
