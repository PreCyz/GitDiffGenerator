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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
import pg.gipter.dao.DaoConstants;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.DataDao;
import pg.gipter.platform.AppManager;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.service.GithubService;
import pg.gipter.service.ToolkitService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.FXMultiRunner;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.UploadStatus;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.JobHelper;
import pg.gipter.utils.StringUtils;

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

import static java.util.stream.Collectors.toCollection;
import static pg.gipter.settings.ApplicationProperties.yyyy_MM_dd;

public class MainController extends AbstractController {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private MenuItem applicationMenuItem;
    @FXML
    private MenuItem toolkitMenuItem;
    @FXML
    private MenuItem fileNameMenuItem;
    @FXML
    private MenuItem readMeMenuItem;
    @FXML
    private MenuItem instructionMenuItem;
    @FXML
    private MenuItem checkUpdatesMenuItem;

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
    private ComboBox<UploadType> uploadTypeComboBox;
    @FXML
    private CheckBox skipRemoteCheckBox;

    @FXML
    private TextField toolkitUsernameTextField;
    @FXML
    private PasswordField toolkitPasswordField;
    @FXML
    private TextField toolkitDomainTextField;
    @FXML
    private Hyperlink toolkitUserFolderHyperlink;
    @FXML
    private TextField toolkitProjectListNamesTextField;
    @FXML
    private CheckBox deleteDownloadedFilesCheckBox;

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
    private CheckBox useAsFileNameCheckBox;

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
    private ProgressIndicator progressIndicator;
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

    private ApplicationProperties applicationProperties;
    private DataDao dataDao;

    private String userFolderUrl;
    private Set<String> definedPatterns;
    private String currentItemName = "";
    private String inteliSense = "";
    private boolean useInteliSense = false;
    private static boolean useComboBoxValueChangeListener = true;

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.dataDao = DaoFactory.getDataDao();
        this.definedPatterns = new LinkedHashSet<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        initConfigurationName();
        setProperties(resources);
        setActions(resources);
        setListeners();
    }

    private void setInitValues() {
        propertiesDao.loadFileNameSetting().ifPresent(nameSetting -> definedPatterns.addAll(nameSetting.getNameSettings().keySet()));

        authorsTextField.setText(String.join(",", applicationProperties.authors()));
        committerEmailTextField.setText(applicationProperties.committerEmail());
        gitAuthorTextField.setText(applicationProperties.gitAuthor());
        mercurialAuthorTextField.setText(applicationProperties.mercurialAuthor());
        svnAuthorTextField.setText(applicationProperties.svnAuthor());
        uploadTypeComboBox.setItems(FXCollections.observableArrayList(UploadType.values()));
        uploadTypeComboBox.setValue(applicationProperties.uploadType());
        skipRemoteCheckBox.setSelected(applicationProperties.isSkipRemote());

        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
        toolkitDomainTextField.setText(applicationProperties.toolkitDomain());

        projectPathLabel.setText(String.join(",", applicationProperties.projectPaths()));
        String itemFileName = Paths.get(applicationProperties.itemPath()).getFileName().toString();
        String itemPath = applicationProperties.itemPath().substring(0, applicationProperties.itemPath().indexOf(itemFileName) - 1);
        itemPathLabel.setText(itemPath);
        projectPathLabel.setTooltip(buildPathTooltip(projectPathLabel.getText()));
        itemPathLabel.setTooltip(buildPathTooltip(itemPathLabel.getText()));
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());
        useAsFileNameCheckBox.setSelected(applicationProperties.isUseAsFileName() && definedPatterns.isEmpty());
        toolkitProjectListNamesTextField.setText(String.join(",", applicationProperties.toolkitProjectListNames()));
        deleteDownloadedFilesCheckBox.setSelected(applicationProperties.isDeleteDownloadedFiles());

        startDatePicker.setValue(LocalDate.now().minusDays(applicationProperties.periodInDays()));
        endDatePicker.setValue(LocalDate.now());
        periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));

        userFolderUrl = applicationProperties.toolkitUserFolder();
        if (applicationProperties.toolkitUserFolder().equals(ArgName.toolkitUserFolder.defaultValue())) {
            userFolderUrl = "";
        }
        setLastItemSubmissionDate();
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
        Set<String> confNames = propertiesDao.loadAllApplicationProperties().keySet();
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
        toolkitProjectListNamesTextField.setDisable(applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS);
        deleteDownloadedFilesCheckBox.setDisable(applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS);

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

        endDatePicker.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);
        authorsTextField.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);
        committerEmailTextField.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);
        gitAuthorTextField.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);
        svnAuthorTextField.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);
        mercurialAuthorTextField.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);
        skipRemoteCheckBox.setDisable(applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS);

        progressIndicator.setVisible(false);
        instructionMenuItem.setDisable(!(Paths.get("Gipter-ui-description.pdf").toFile().exists() && Desktop.isDesktopSupported()));
        useAsFileNameCheckBox.setDisable(!definedPatterns.isEmpty());
        setDisableDependOnConfigurations();

        TextFields.bindAutoCompletion(itemFileNamePrefixTextField, itemNameSuggestionsCallback());
    }

    private StringConverter<LocalDate> dateConverter() {
        return new StringConverter<LocalDate>() {
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
        Map<String, Properties> map = propertiesDao.loadAllApplicationProperties();
        addConfigurationButton.setDisable(map.isEmpty());
        removeConfigurationButton.setDisable(map.isEmpty());
        executeButton.setDisable(map.isEmpty());
        executeAllButton.setDisable(map.isEmpty());
        jobButton.setDisable(map.isEmpty());
        configurationNameComboBox.setDisable(map.isEmpty());
        projectPathButton.setDisable(map.isEmpty() || uploadTypeComboBox.getValue() == UploadType.STATEMENT);
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

    private void setActions(ResourceBundle resources) {
        applicationMenuItem.setOnAction(applicationActionEventHandler());
        toolkitMenuItem.setOnAction(toolkitActionEventHandler());
        fileNameMenuItem.setOnAction(nameSettingsActionEvent());
        readMeMenuItem.setOnAction(readMeActionEventHandler());
        instructionMenuItem.setOnAction(instructionActionEventHandler());
        checkUpdatesMenuItem.setOnAction(checkUpdatesActionEventHandler());
        projectPathButton.setOnAction(projectPathActionEventHandler());
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        uploadTypeComboBox.setOnAction(uploadTypeActionEventHandler());
        executeButton.setOnAction(executeActionEventHandler());
        executeAllButton.setOnAction(executeAllActionEventHandler());
        jobButton.setOnAction(jobActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
        saveConfigurationButton.setOnAction(saveConfigurationActionEventHandler());
        addConfigurationButton.setOnAction(addConfigurationEventHandler());
        removeConfigurationButton.setOnAction(removeConfigurationEventHandler());
        toolkitUserFolderHyperlink.setOnMouseClicked(toolkitUserFolderOnMouseClickEventHandler());
        itemFileNamePrefixTextField.setOnKeyReleased(itemNameKeyReleasedEventHandler());
    }

    private EventHandler<ActionEvent> applicationActionEventHandler() {
        return event -> {
            uiLauncher.setApplicationProperties(applicationProperties);
            uiLauncher.showApplicationSettingsWindow();
        };
    }

    private EventHandler<ActionEvent> toolkitActionEventHandler() {
        return event -> {
            uiLauncher.setApplicationProperties(applicationProperties);
            uiLauncher.showToolkitSettingsWindow();
        };
    }

    private EventHandler<ActionEvent> nameSettingsActionEvent() {
        return event -> {
            uiLauncher.setApplicationProperties(applicationProperties);
            uiLauncher.showNameSettingsWindow();
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
                    .withImage(ImageFile.ERROR_CHICKEN);
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

    private EventHandler<ActionEvent> checkUpdatesActionEventHandler() {
        return event -> new GithubService(applicationProperties).checkUpgradesWithPopups();
    }

    private EventHandler<ActionEvent> projectPathActionEventHandler() {
        return event -> {
            String[] argsFromUI = createArgsFromUI();
            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(argsFromUI));
            String configurationName = configurationNameTextField.getText();
            updateConfigurationNameComboBox(configurationName, configurationName);
            if (uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS) {
                uiLauncher.showToolkitProjectsWindow();
            } else {
                uiLauncher.showProjectsWindow();
            }
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
            if (uploadTypeComboBox.getValue() == UploadType.STATEMENT) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle(resources.getString("directory.item.statement.title"));
                File statementFile = fileChooser.showOpenDialog(uiLauncher.currentWindow());
                if (statementFile != null && statementFile.exists() && statementFile.isFile()) {
                    itemPathLabel.setText(statementFile.getAbsolutePath());
                    itemPathButton.setText(resources.getString("button.open"));
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                directoryChooser.setTitle(resources.getString("directory.item.store"));
                File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
                if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                    itemPathLabel.setText(itemPathDirectory.getAbsolutePath());
                    itemPathButton.setText(resources.getString("button.change"));
                }
            }
        };
    }

    private EventHandler<ActionEvent> executeActionEventHandler() {
        return event -> {
            String[] args = createArgsFromUI();
            ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(args);

            FXMultiRunner runner = new FXMultiRunner(uiAppProperties, uiLauncher.nonUIExecutor());
            resetIndicatorProperties(runner);
            uiLauncher.executeOutsideUIThread(() -> {
                runner.start();
                if (uiAppProperties.isActiveTray()) {
                    uiLauncher.updateTray(uiAppProperties);
                }
                updateLastItemUploadDate();
            });
        };
    }

    private void updateLastItemUploadDate() {
        try {
            Optional<Properties> dataProperties = dataDao.loadDataProperties();
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
        return event -> {
            Map<String, Properties> map = propertiesDao.loadAllApplicationProperties();
            FXMultiRunner runner = new FXMultiRunner(map.keySet(), uiLauncher.nonUIExecutor());
            resetIndicatorProperties(runner);
            uiLauncher.executeOutsideUIThread(() -> {
                runner.call();
                String[] firstFromConfigs = propertiesDao.loadArgumentArray(new LinkedList<>(map.keySet()).getFirst());
                ApplicationProperties instance = ApplicationPropertiesFactory.getInstance(firstFromConfigs);
                if (instance.isActiveTray()) {
                    uiLauncher.updateTray(instance);
                }
            });
        };
    }

    private String[] createArgsFromUI() {
        List<String> argList = new LinkedList<>();

        argList.add(ArgName.toolkitUsername + "=" + toolkitUsernameTextField.getText());
        argList.add(ArgName.toolkitPassword + "=" + toolkitPasswordField.getText());

        if (!StringUtils.nullOrEmpty(authorsTextField.getText())) {
            argList.add(ArgName.author + "=" + authorsTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(committerEmailTextField.getText())) {
            argList.add(ArgName.committerEmail + "=" + committerEmailTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(gitAuthorTextField.getText())) {
            argList.add(ArgName.gitAuthor + "=" + gitAuthorTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(mercurialAuthorTextField.getText())) {
            argList.add(ArgName.mercurialAuthor + "=" + mercurialAuthorTextField.getText());
        }
        if (!StringUtils.nullOrEmpty(svnAuthorTextField.getText())) {
            argList.add(ArgName.svnAuthor + "=" + svnAuthorTextField.getText());
        }
        argList.add(ArgName.uploadType + "=" + uploadTypeComboBox.getValue());
        argList.add(ArgName.skipRemote + "=" + skipRemoteCheckBox.isSelected());

        if (!StringUtils.nullOrEmpty(toolkitProjectListNamesTextField.getText())) {
            argList.add(ArgName.toolkitProjectListNames + "=" + toolkitProjectListNamesTextField.getText());
        }
        argList.add(ArgName.deleteDownloadedFiles + "=" + deleteDownloadedFilesCheckBox.isSelected());

        argList.add(ArgName.projectPath + "=" + projectPathLabel.getText());
        argList.add(ArgName.itemPath + "=" + itemPathLabel.getText());
        if (!StringUtils.nullOrEmpty(itemFileNamePrefixTextField.getText())) {
            argList.add(ArgName.itemFileNamePrefix + "=" + itemFileNamePrefixTextField.getText());
        }
        argList.add(ArgName.useAsFileName + "=" + useAsFileNameCheckBox.isSelected());

        argList.add(ArgName.startDate + "=" + startDatePicker.getValue().format(yyyy_MM_dd));
        argList.add(ArgName.endDate + "=" + endDatePicker.getValue().format(yyyy_MM_dd));
        if (!ArgName.periodInDays.defaultValue().equals(periodInDaysTextField.getText())) {
            argList.add(ArgName.periodInDays + "=" + periodInDaysTextField.getText());
        }

        argList.add(ArgName.configurationName + "=" + configurationNameTextField.getText());
        argList.add(ArgName.preferredArgSource + "=" + PreferredArgSource.UI);

        return argList.toArray(new String[0]);
    }

    private void resetIndicatorProperties(Task task) {
        progressIndicator.setVisible(true);
        progressIndicator.progressProperty().unbind();
        progressIndicator.progressProperty().bind(task.progressProperty());
        infoLabel.textProperty().unbind();
        infoLabel.textProperty().bind(task.messageProperty());
    }

    private EventHandler<ActionEvent> uploadTypeActionEventHandler() {
        return event -> {
            boolean disableProjectButton = uploadTypeComboBox.getValue() == UploadType.STATEMENT;
            disableProjectButton |= propertiesDao.loadAllApplicationProperties().isEmpty() && configurationNameTextField.getText().isEmpty();
            projectPathButton.setDisable(disableProjectButton);
            if (uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS) {
                endDatePicker.setValue(LocalDate.now());
            }
            toolkitProjectListNamesTextField.setDisable(uploadTypeComboBox.getValue() != UploadType.TOOLKIT_DOCS);
            endDatePicker.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            authorsTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            committerEmailTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            gitAuthorTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            svnAuthorTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            mercurialAuthorTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            skipRemoteCheckBox.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            deleteDownloadedFilesCheckBox.setDisable(uploadTypeComboBox.getValue() != UploadType.TOOLKIT_DOCS);
        };
    }

    private EventHandler<ActionEvent> jobActionEventHandler() {
        return event -> uiLauncher.showJobWindow();
    }

    private EventHandler<ActionEvent> exitActionEventHandler() {
        return event -> UILauncher.platformExit();
    }

    private EventHandler<ActionEvent> saveConfigurationActionEventHandler() {
        return event -> {
            String configurationName = configurationNameTextField.getText();
            String comboConfigName = configurationNameComboBox.getValue();

            String[] args = createArgsFromUI();
            applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            uiLauncher.executeOutsideUIThread(() -> updateRunConfig(comboConfigName, configurationName));
            setLastItemSubmissionDate();

            updateConfigurationNameComboBox(comboConfigName, configurationName);
            uiLauncher.updateTray(applicationProperties);
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWindowType(WindowType.CONFIRMATION_WINDOW)
                    .withImage(ImageFile.FINGER_UP);
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        };
    }

    private void updateRunConfig(String oldConfigName, String newConfigName) {
        Properties properties = getPropertiesWithoutDates();
        propertiesDao.saveToolkitSettings(properties);
        if (!StringUtils.nullOrEmpty(configurationNameTextField.getText())) {
            propertiesDao.saveRunConfig(properties);
            new JobHelper().updateJobConfigs(oldConfigName, newConfigName);
            setDisableDependOnConfigurations();
        }
        if (!StringUtils.nullOrEmpty(oldConfigName) && !newConfigName.equals(oldConfigName)) {
            propertiesDao.removeConfig(oldConfigName);
        }
    }

    @NotNull
    private Properties getPropertiesWithoutDates() {
        String[] args = createArgsFromUI();
        Properties properties = propertiesDao.createProperties(args);
        properties.remove(ArgName.startDate.name());
        properties.remove(ArgName.endDate.name());
        return properties;
    }

    private EventHandler<ActionEvent> addConfigurationEventHandler() {
        return event -> {
            String configurationName = configurationNameTextField.getText();
            Optional<Properties> properties = propertiesDao.loadApplicationProperties(configurationName);
            boolean operationDone = false;
            if (properties.isPresent()) {
                boolean result = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.overrideProperties.message", configurationName))
                        .withAlertType(Alert.AlertType.CONFIRMATION)
                        .withWindowType(WindowType.OVERRIDE_WINDOW)
                        .withImage(ImageFile.OVERRIDE)
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
                propertiesDao.saveRunConfig(getPropertiesWithoutDates());
                updateConfigurationNameComboBox(ArgName.configurationName.defaultValue(), configurationName);
                operationDone = true;
            }
            if (operationDone) {
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWindowType(WindowType.CONFIRMATION_WINDOW)
                        .withImage(ImageFile.FINGER_UP);
                alertWindowBuilder.buildAndDisplayWindow();
            }

        };
    }

    private void saveNewConfig(String configurationName) {
        Properties currentProperties = getPropertiesWithoutDates();
        currentProperties.put(ArgName.configurationName.name(), configurationName);
        propertiesDao.saveRunConfig(currentProperties);
    }

    private EventHandler<ActionEvent> removeConfigurationEventHandler() {
        return event -> {
            try {
                propertiesDao.removeConfig(configurationNameComboBox.getValue());
                Map<String, Properties> propertiesMap = propertiesDao.loadAllApplicationProperties();
                String newConfiguration = ArgName.configurationName.defaultValue();
                if (!propertiesMap.isEmpty()) {
                    Properties currentConfig = new ArrayList<>(propertiesMap.entrySet()).get(0).getValue();
                    newConfiguration = currentConfig.getProperty(ArgName.configurationName.name());
                }
                removeConfigurationNameFromComboBox(configurationNameComboBox.getValue(), newConfiguration);
                String[] currentArgs = propertiesDao.loadArgumentArray(newConfiguration);
                applicationProperties = ApplicationPropertiesFactory.getInstance(currentArgs);
                setInitValues();
                configurationNameTextField.setText(configurationNameComboBox.getValue());
                setDisableDependOnConfigurations();
                setToolkitCredentialsIfAvailable();
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.removed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWindowType(WindowType.CONFIRMATION_WINDOW)
                        .withImage(ImageFile.FINGER_UP);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
            } catch (IllegalStateException ex) {
                AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                        .withHeaderText(ex.getMessage())
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage(ImageFile.ERROR_CHICKEN);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
            }
        };
    }

    private EventHandler<MouseEvent> toolkitUserFolderOnMouseClickEventHandler() {
        return event -> Platform.runLater(() -> {
            String userFolder = applicationProperties.toolkitUserFolder();
            if (!applicationProperties.toolkitUserFolder().equalsIgnoreCase(userFolderUrl)) {
                userFolder = userFolderUrl;
            }
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(userFolder);
            toolkitUserFolderHyperlink.setVisited(false);
        });
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
        Properties properties = propertiesDao.loadToolkitCredentials();
        toolkitUsernameTextField.setText(properties.getProperty(ArgName.toolkitUsername.name()));
        toolkitPasswordField.setText(properties.getProperty(ArgName.toolkitPassword.name()));
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
        configurationNameComboBox.getSelectionModel().selectedItemProperty().addListener(comboBoxValueChangeListener());
        configurationNameTextField.textProperty().addListener(configurationNameListener());
        useLastItemDateCheckbox.selectedProperty().addListener(useListItemCheckBoxListener());
        itemFileNamePrefixTextField.textProperty().addListener(itemFileNameChangeListener());
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isAltDown() || KeyCode.ALT_GRAPH == e.getCode()) {
                e.consume();
            }
        });
    }

    private ChangeListener<String> toolkitUsernameListener() {
        return (observable, oldValue, newValue) -> {
            userFolderUrl = applicationProperties.toolkitUserFolder();
            userFolderUrl = userFolderUrl.substring(0, userFolderUrl.lastIndexOf("/") + 1) + newValue;
        };
    }

    private ChangeListener<String> comboBoxValueChangeListener() {
        return (options, oldValue, newValue) -> {
            if (useComboBoxValueChangeListener) {
                String[] args = propertiesDao.loadArgumentArray(newValue);
                applicationProperties = ApplicationPropertiesFactory.getInstance(args);
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
