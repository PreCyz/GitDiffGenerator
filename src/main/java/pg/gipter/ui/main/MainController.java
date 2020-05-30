package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.NotNull;
import pg.gipter.core.*;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.*;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.ui.*;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
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
    private TextField periodInDaysTextField;

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
    private CheckBox useLastItemDateCheckbox;
    @FXML
    private Label currentWeekNumberLabel;

    private final DataService dataService;

    private String userFolderUrl;
    private final Set<String> definedPatterns;
    private String currentItemName = "";
    private String inteliSense = "";
    private boolean useInteliSense = false;
    private static boolean useComboBoxValueChangeListener = true;

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.dataService = DataService.getInstance();
        this.definedPatterns = EnumSet.allOf(NamePatternValue.class)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        initConfigurationName();
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

        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
        toolkitDomainTextField.setText(applicationProperties.toolkitDomain());

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
        periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));

        userFolderUrl = applicationProperties.toolkitUserFolder();
        if (applicationProperties.toolkitUserFolder().equals(ArgName.toolkitUserFolder.defaultValue())) {
            userFolderUrl = "";
        }
        setLastItemSubmissionDate();
        currentWeekNumberLabel.setText(String.valueOf(applicationProperties.getWeekNumber(LocalDate.now())));
    }

    private void setLastItemSubmissionDate() {
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

    private void setProperties(ResourceBundle resources) {
        toolkitDomainTextField.setEditable(false);
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
        verifyProgressIndicator.setVisible(false);
        instructionMenuItem.setDisable(!(Paths.get("Gipter-ui-description.pdf").toFile().exists() && Desktop.isDesktopSupported()));
        setDisableDependOnConfigurations();

        TextFields.bindAutoCompletion(itemFileNamePrefixTextField, itemNameSuggestionsCallback());
        setUpgradeMenuItemDisabled();
    }

    private void setAccelerators() {
        applicationMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));
        toolkitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));
        upgradeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));
        wizardMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));
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

    private void setDisableDependOnConfigurations() {
        Map<String, RunConfig> runConfigMap = applicationProperties.getRunConfigMap();
        addConfigurationButton.setDisable(runConfigMap.isEmpty());
        removeConfigurationButton.setDisable(runConfigMap.isEmpty());
        executeButton.setDisable(runConfigMap.isEmpty());
        executeAllButton.setDisable(runConfigMap.isEmpty());
        jobButton.setDisable(runConfigMap.isEmpty());
        configurationNameComboBox.setDisable(runConfigMap.isEmpty());
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
        applicationMenuItem.setOnAction(applicationActionEventHandler());
        toolkitMenuItem.setOnAction(toolkitActionEventHandler());
        readMeMenuItem.setOnAction(readMeActionEventHandler());
        instructionMenuItem.setOnAction(instructionActionEventHandler());
        upgradeMenuItem.setOnAction(upgradeActionEventHandler());
        wizardMenuItem.setOnAction(launchWizardActionEventHandler());
        projectPathButton.setOnAction(projectPathActionEventHandler());
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        itemTypeComboBox.setOnAction(uploadTypeActionEventHandler());
        executeButton.setOnAction(executeActionEventHandler());
        executeAllButton.setOnAction(executeAllActionEventHandler());
        jobButton.setOnAction(jobActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
        saveConfigurationButton.setOnAction(saveConfigurationActionEventHandler());
        addConfigurationButton.setOnAction(addConfigurationEventHandler());
        removeConfigurationButton.setOnAction(removeConfigurationEventHandler());
        verifyCredentialsHyperlink.setOnMouseClicked(verifyCredentialsHyperlinkOnMouseClickEventHandler());
        itemFileNamePrefixTextField.setOnKeyReleased(itemNameKeyReleasedEventHandler());
    }

    private EventHandler<ActionEvent> applicationActionEventHandler() {
        return event -> uiLauncher.showApplicationSettingsWindow();
    }

    private EventHandler<ActionEvent> toolkitActionEventHandler() {
        return event -> {
            uiLauncher.setApplicationProperties(applicationProperties);
            uiLauncher.showToolkitSettingsWindow();
        };
    }

    private EventHandler<ActionEvent> readMeActionEventHandler() {
        return event -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(GithubService.GITHUB_URL + "#gitdiffgenerator");
        };
    }

    private EventHandler<ActionEvent> instructionActionEventHandler() {
        return event -> {
            String pdfFileName = "Gipter-ui-description.pdf";
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.warning.desktopNotSupported"))
                    .withLink(applicationProperties.toolkitUserFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withImage(ImageFile.ERROR_CHICKEN_PNG);
            try {
                File pdfFile = Paths.get(pdfFileName).toFile();
                if (pdfFile.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile);
                    } else {
                        logger.error("AWT Desktop is not supported by the platform.");
                        Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
                    }
                }
            } catch (IOException e) {
                logger.error("Could not find [{}] file with instructions.", pdfFileName, e);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
            }
        };
    }

    private EventHandler<ActionEvent> upgradeActionEventHandler() {
        return event -> {
            uiLauncher.hideMainWindow();
            uiLauncher.showUpgradeWindow();
        };
    }

    private EventHandler<ActionEvent> launchWizardActionEventHandler() {
        return event -> {
            uiLauncher.hideMainWindow();
            new WizardLauncher(uiLauncher.currentWindow(), configurationNameComboBox.getValue()).execute();
        };
    }

    private EventHandler<ActionEvent> projectPathActionEventHandler() {
        return event -> {
            RunConfig runConfig = createRunConfigFromUI();
            applicationProperties.updateCurrentRunConfig(runConfig);
            uiLauncher.setApplicationProperties(applicationProperties);
            String configurationName = configurationNameTextField.getText();
            updateConfigurationNameComboBox(configurationName, configurationName);
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
        ToolkitConfig toolkitConfig = createToolkitConfigFromUI();
        ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(Stream.concat(
                Arrays.stream(runConfig.toArgumentArray()),
                Arrays.stream(toolkitConfig.toArgumentArray())
        ).toArray(String[]::new));

        FXMultiRunner runner = new FXMultiRunner(
                Stream.of(uiAppProperties).collect(Collectors.toList()),
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
            Optional<Properties> dataProperties = dataService.loadDataProperties();
            if (dataProperties.isPresent() && dataProperties.get().containsKey(DaoConstants.UPLOAD_STATUS_KEY)) {
                UploadStatus status = UploadStatus.valueOf(dataProperties.get().getProperty(DaoConstants.UPLOAD_STATUS_KEY));
                if (EnumSet.of(UploadStatus.SUCCESS, UploadStatus.PARTIAL_SUCCESS).contains(status)) {
                    uiLauncher.setLastItemSubmissionDate(null);
                }
            }
        } catch (Exception ex) {
            logger.warn("Could not determine the status of the upload. {}. Forcing to refresh last upload date.", ex.getMessage());
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

    private RunConfig createRunConfigFromUI() {
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
        if (!ArgName.periodInDays.defaultValue().equals(periodInDaysTextField.getText())) {
            runConfig.setPeriodInDays(Integer.parseInt(periodInDaysTextField.getText()));
        }

        runConfig.setConfigurationName(configurationNameTextField.getText());
        runConfig.setPreferredArgSource(PreferredArgSource.UI);

        return runConfig;
    }

    private ToolkitConfig createToolkitConfigFromUI() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(toolkitUsernameTextField.getText());
        toolkitConfig.setToolkitPassword(toolkitPasswordField.getText());
        return toolkitConfig;
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
            disableProjectButton |= applicationProperties.getRunConfigMap().isEmpty() && configurationNameTextField.getText().isEmpty();
            projectPathButton.setDisable(disableProjectButton);
            if (itemTypeComboBox.getValue() == ItemType.TOOLKIT_DOCS) {
                endDatePicker.setValue(LocalDate.now());
            }
            toolkitProjectListNamesTextField.setDisable(itemTypeComboBox.getValue() != ItemType.TOOLKIT_DOCS);
            deleteDownloadedFilesCheckBox.setDisable(
                    !EnumSet.of(ItemType.TOOLKIT_DOCS,  ItemType.SHARE_POINT_DOCS).contains(itemTypeComboBox.getValue())
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

    private EventHandler<ActionEvent> saveConfigurationActionEventHandler() {
        return event -> saveConfiguration();
    }

    private void saveConfiguration() {
        String configurationName = configurationNameTextField.getText();
        String comboConfigName = configurationNameComboBox.getValue();

        RunConfig runConfigFromUI = createRunConfigFromUI();
        applicationProperties.updateCurrentRunConfig(runConfigFromUI);
        CacheManager.removeFromCache(applicationProperties.configurationName());
        uiLauncher.executeOutsideUIThread(() -> updateRunConfig(comboConfigName, configurationName));
        setLastItemSubmissionDate();

        updateConfigurationNameComboBox(comboConfigName, configurationName);
        uiLauncher.updateTray(applicationProperties);
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                .withAlertType(Alert.AlertType.INFORMATION)
                .withWindowType(WindowType.CONFIRMATION_WINDOW)
                .withImage(ImageFile.FINGER_UP_PNG);
        Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
    }

    private void updateRunConfig(String oldConfigName, String newConfigName) {
        ToolkitConfig toolkitConfigFromUI = createToolkitConfigFromUI();
        applicationProperties.updateToolkitConfig(toolkitConfigFromUI);
        if (!StringUtils.nullOrEmpty(configurationNameTextField.getText())) {
            RunConfig runConfigWithoutDates = getRunConfigWithoutDates();
            applicationProperties.updateCurrentRunConfig(runConfigWithoutDates);
            new JobHelper().updateJobConfigs(oldConfigName, newConfigName);
            setDisableDependOnConfigurations();
        }
        applicationProperties.save();
        if (!StringUtils.nullOrEmpty(oldConfigName) && !newConfigName.equals(oldConfigName)) {
            applicationProperties.removeConfig(oldConfigName);
        }
    }

    @NotNull
    private RunConfig getRunConfigWithoutDates() {
        RunConfig runConfigFromUI = createRunConfigFromUI();
        runConfigFromUI.setStartDate(null);
        runConfigFromUI.setEndDate(null);
        return runConfigFromUI;
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
                        .withWindowType(WindowType.OVERRIDE_WINDOW)
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
                applicationProperties.updateCurrentRunConfig(getRunConfigWithoutDates());
                applicationProperties.save();
                updateConfigurationNameComboBox(ArgName.configurationName.defaultValue(), configurationName);
                operationDone = true;
            }
            if (operationDone) {
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWindowType(WindowType.CONFIRMATION_WINDOW)
                        .withImage(ImageFile.FINGER_UP_PNG);
                alertWindowBuilder.buildAndDisplayWindow();
            }

        };
    }

    private void saveNewConfig(String configurationName) {
        RunConfig currentRunConfig = getRunConfigWithoutDates();
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
                setToolkitCredentialsIfAvailable();
                alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.removed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWindowType(WindowType.CONFIRMATION_WINDOW)
                        .withImage(ImageFile.FINGER_UP_PNG);
            } catch (IllegalStateException ex) {
                alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(ex.getMessage())
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage(ImageFile.ERROR_CHICKEN_PNG);
            }
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        };
    }

    private EventHandler<MouseEvent> verifyCredentialsHyperlinkOnMouseClickEventHandler() {
        return event -> {
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() {
                    RunConfig runConfigFromUI = createRunConfigFromUI();
                    ApplicationProperties uiAppProps = ApplicationPropertiesFactory.getInstance(runConfigFromUI.toArgumentArray());
                    boolean hasProperCredentials = new ToolkitService(uiAppProps).hasProperCredentials();
                    AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
                    if (hasProperCredentials) {
                        alertWindowBuilder.withHeaderText(BundleUtils.getMsg("toolkit.panel.credentialsVerified"))
                                .withWindowType(WindowType.CONFIRMATION_WINDOW)
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImage(ImageFile.FINGER_UP_PNG);
                    } else {
                        alertWindowBuilder.withHeaderText(BundleUtils.getMsg("toolkit.panel.credentialsWrong"))
                                .withLink(AlertHelper.logsFolder())
                                .withWindowType(WindowType.LOG_WINDOW)
                                .withAlertType(Alert.AlertType.ERROR)
                                .withImage(ImageFile.MINION_IOIO_GIF);
                    }
                    Platform.runLater(() -> {
                        verifyProgressIndicator.setVisible(false);
                        alertWindowBuilder.buildAndDisplayWindow();
                    });
                    return null;
                }
            };
            verifyProgressIndicator.setVisible(true);
            uiLauncher.executeOutsideUIThread(task);
            verifyCredentialsHyperlink.setVisited(false);
        };
    }

    private EventHandler<KeyEvent> itemNameKeyReleasedEventHandler() {
        return event -> {
            if (event.getCode() == KeyCode.ENTER) {
                itemFileNamePrefixTextField.setText(currentItemName);
                itemFileNamePrefixTextField.positionCaret(currentItemName.length());
            }
        };
    }

    private void setToolkitCredentialsIfAvailable() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
    }

    private void updateConfigurationNameComboBox(String oldValue, String newValue) {
        List<String> items = new ArrayList<>(configurationNameComboBox.getItems());
        items.remove(oldValue);
        items.add(newValue);
        updateItemsForConfigComboBox(newValue, FXCollections.observableArrayList(items));
    }

    private void updateItemsForConfigComboBox(String newValue, ObservableList<String> items) {
        useComboBoxValueChangeListener = false;
        configurationNameComboBox.setItems(items);
        configurationNameComboBox.setValue(newValue);
        useComboBoxValueChangeListener = true;
        setDisableDependOnConfigurations();
    }

    private void removeConfigurationNameFromComboBox(String oldValue, String newValue) {
        List<String> items = new ArrayList<>(configurationNameComboBox.getItems());
        items.remove(oldValue);
        updateItemsForConfigComboBox(newValue, FXCollections.observableList(items));
    }

    private void setListeners() {
        toolkitUsernameTextField.textProperty().addListener(toolkitUsernameListener());
        configurationNameComboBox.getSelectionModel().selectedItemProperty().addListener(configurationNameComboBoxListener());
        configurationNameTextField.textProperty().addListener(configurationNameListener());
        useLastItemDateCheckbox.selectedProperty().addListener(useListItemCheckBoxListener());
        itemFileNamePrefixTextField.textProperty().addListener(itemFileNameChangeListener());
    }

    private ChangeListener<String> toolkitUsernameListener() {
        return (observable, oldValue, newValue) -> {
            userFolderUrl = applicationProperties.toolkitUserFolder();
            userFolderUrl = userFolderUrl.substring(0, userFolderUrl.lastIndexOf("/") + 1) + newValue;
        };
    }

    private ChangeListener<String> configurationNameComboBoxListener() {
        return (options, oldValue, newValue) -> {
            if (useComboBoxValueChangeListener) {
                RunConfig runConfigFromUI = createRunConfigFromUI();
                ApplicationProperties uiApplicationProperties = ApplicationPropertiesFactory.getInstance(runConfigFromUI.toArgumentArray());
                CacheManager.addToCache(oldValue, uiApplicationProperties);

                applicationProperties = CacheManager.getApplicationProperties(newValue);
                setInitValues();
                configurationNameTextField.setText(newValue);
                if (useLastItemDateCheckbox.isSelected()) {
                    useLastItemDateCheckbox.setSelected(false);
                }
            }
        };
    }

    private ChangeListener<String> configurationNameListener() {
        return (observable, oldValue, newValue) -> {
            if (StringUtils.nullOrEmpty(oldValue) && !StringUtils.nullOrEmpty(newValue)) {
                addConfigurationButton.setDisable(false);
                projectPathButton.setDisable(false);
            } else if (StringUtils.nullOrEmpty(newValue)) {
                addConfigurationButton.setDisable(true);
                projectPathButton.setDisable(true);
            }
        };
    }

    private ChangeListener<Boolean> useListItemCheckBoxListener() {
        return (observable, oldValue, newValue) -> {
            startDatePicker.setDisable(newValue);
            periodInDaysTextField.setDisable(newValue);
            if (newValue) {
                startDatePicker.setValue(uiLauncher.getLastItemSubmissionDate().toLocalDate());
                int newPeriodInDays = endDatePicker.getValue().getDayOfYear() - startDatePicker.getValue().getDayOfYear();
                periodInDaysTextField.setText(String.valueOf(newPeriodInDays));
            } else {
                periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));
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

}
