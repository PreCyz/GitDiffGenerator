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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManager;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.service.GithubService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.FXRunner;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

import static pg.gipter.settings.ApplicationProperties.yyyy_MM_dd;

public class MainController extends AbstractController {

    @FXML
    private MenuItem applicationMenuItem;
    @FXML
    private MenuItem toolkitMenuItem;
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

    private ApplicationProperties applicationProperties;

    private String userFolderUrl;
    private static boolean useComboBoxValueChangeListener = true;
    private final String PDF_DESCRIPTION_FILE = "Gipter-ui-description.pdf";

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
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
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());
        useAsFileNameCheckBox.setSelected(applicationProperties.isUseAsFileName());
        toolkitProjectListNamesTextField.setText(String.join(",", applicationProperties.toolkitProjectListNames()));
        deleteDownloadedFilesCheckBox.setSelected(applicationProperties.isDeleteDownloadedFiles());

        startDatePicker.setValue(LocalDate.now().minusDays(applicationProperties.periodInDays()));
        endDatePicker.setValue(LocalDate.now());
        periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));

        userFolderUrl = applicationProperties.toolkitUserFolder();
        if (applicationProperties.toolkitUserFolder().equals(ArgName.toolkitUserFolder.defaultValue())) {
            userFolderUrl = "";
        }
    }

    private void initConfigurationName() {
        Set<String> confNames = propertiesHelper.loadAllApplicationProperties().keySet();
        if (!StringUtils.nullOrEmpty(configurationNameComboBox.getValue())) {
            confNames.add(configurationNameComboBox.getValue());
        }
        configurationNameComboBox.setItems(FXCollections.observableList(new ArrayList<>(confNames)));
        configurationNameComboBox.setValue(applicationProperties.configurationName());
        configurationNameTextField.setText(applicationProperties.configurationName());
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
        projectPathButton.setDisable(uploadTypeComboBox.getValue() == UploadType.STATEMENT);

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
        projectPathLabel.setTooltip(buildProjectPathsTooltip(projectPathLabel.getText()));
        instructionMenuItem.setDisable(!(Paths.get(PDF_DESCRIPTION_FILE).toFile().exists() && Desktop.isDesktopSupported()));
        setDisablePropertyOnRemoveConfigurationButton();
    }

    private boolean isConfigComboDisabled() {
        boolean result = configurationNameComboBox.getItems().isEmpty();
        result |= configurationNameComboBox.getItems().size() == 1 && ArgName.configurationName.defaultValue().equals(configurationNameComboBox.getValue());
        return result;
    }

    private void setDisablePropertyOnRemoveConfigurationButton() {
        removeConfigurationButton.setDisable(isConfigComboDisabled());
        configurationNameComboBox.setDisable(isConfigComboDisabled());
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

    private void setActions(ResourceBundle resources) {
        applicationMenuItem.setOnAction(applicationActionEventHandler());
        toolkitMenuItem.setOnAction(toolkitActionEventHandler());
        readMeMenuItem.setOnAction(readMeActionEventHandler());
        instructionMenuItem.setOnAction(instructionActionEventHandler());
        checkUpdatesMenuItem.setOnAction(checkUpdatesActionEventHandler());
        projectPathButton.setOnAction(projectPathActionEventHandler());
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        uploadTypeComboBox.setOnAction(uploadTypeActionEventHandler());
        executeButton.setOnAction(executeActionEventHandler());
        jobButton.setOnAction(jobActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
        saveConfigurationButton.setOnAction(saveConfigurationActionEventHandler());
        addConfigurationButton.setOnAction(addConfigurationEventHandler());
        removeConfigurationButton.setOnAction(removeConfigurationEventHandler());
        toolkitUserFolderHyperlink.setOnMouseClicked(toolkitUserFolderOnMouseClickEventHandler());
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

    private EventHandler<ActionEvent> readMeActionEventHandler() {
        return event -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(GithubService.GITHUB_URL + "#gitdiffgenerator");
        };
    }

    private EventHandler<ActionEvent> instructionActionEventHandler() {
        return event -> {
            String pdfFileName = "Gipter-ui-description.pdf";
            try {
                File pdfFile = Paths.get(pdfFileName).toFile();
                if (pdfFile.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile);
                    } else {
                        logger.error("AWT Desktop is not supported by the platform.");
                        Platform.runLater(() -> new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg("popup.warning.desktopNotSupported"))
                                .withLink(applicationProperties.toolkitUserFolder())
                                .withWindowType(WindowType.LOG_WINDOW)
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImage()
                                .buildAndDisplayWindow()
                        );
                    }
                }
            } catch (IOException e) {
                logger.error("Could not find [{}] file with instructions.", pdfFileName, e);
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.warning.desktopNotSupported"))
                        .withLink(applicationProperties.toolkitUserFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImage()
                        .buildAndDisplayWindow()
                );
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

    private Tooltip buildProjectPathsTooltip(String result) {
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

            FXRunner runner = new FXRunner(uiAppProperties);
            resetIndicatorProperties(runner);
            uiLauncher.execute(() -> {
                runner.call();
                if (uiAppProperties.isActiveTray()) {
                    uiLauncher.updateTray(uiAppProperties);
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
            projectPathButton.setDisable(uploadTypeComboBox.getValue() == UploadType.STATEMENT);
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
            String[] args = createArgsFromUI();
            Properties properties = propertiesHelper.createProperties(args);
            properties.remove(ArgName.startDate.name());
            properties.remove(ArgName.endDate.name());
            propertiesHelper.saveToolkitSettings(properties);
            propertiesHelper.saveRunConfig(properties);
            applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            String configurationName = configurationNameTextField.getText();
            String comboConfigName = configurationNameComboBox.getValue();
            if (!configurationName.equals(comboConfigName)) {
                updateConfigurationNameComboBox(comboConfigName, configurationName);
                propertiesHelper.removeConfig(comboConfigName);
                removeConfigurationButton.setDisable(false);
            }
            uiLauncher.updateTray(applicationProperties);
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWindowType(WindowType.CONFIRMATION_WINDOW)
                    .withImage()
                    .buildAndDisplayWindow()
            );
        };
    }

    private EventHandler<ActionEvent> addConfigurationEventHandler() {
        return event -> {
            String[] args = createArgsFromUI();
            String configurationName = configurationNameTextField.getText();
            Optional<Properties> properties = propertiesHelper.loadApplicationProperties(configurationName);
            boolean operationDone = false;
            if (properties.isPresent()) {
                boolean result = new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.overrideProperties.message", configurationName))
                        .withAlertType(Alert.AlertType.CONFIRMATION)
                        .withWindowType(WindowType.OVERRIDE_WINDOW)
                        .withImage()
                        .withOkButtonText(BundleUtils.getMsg("popup.overrideProperties.buttonOk"))
                        .withCancelButtonText(BundleUtils.getMsg("popup.overrideProperties.buttonNo"))
                        .buildAndDisplayOverrideWindow();
                if (result) {
                    Properties currentProperties = propertiesHelper.createProperties(args);
                    currentProperties.put(ArgName.configurationName.name(), configurationName);
                    propertiesHelper.saveRunConfig(currentProperties);
                    updateConfigurationNameComboBox(configurationNameComboBox.getValue(), configurationName);
                    operationDone = true;
                } else {
                    configurationNameTextField.setText(configurationNameComboBox.getValue());
                }
            } else {
                propertiesHelper.saveRunConfig(propertiesHelper.createProperties(args));
                updateConfigurationNameComboBox(ArgName.configurationName.defaultValue(), configurationName);
                operationDone = true;
            }
            if (operationDone) {
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWindowType(WindowType.CONFIRMATION_WINDOW)
                        .withImage()
                        .buildAndDisplayWindow()
                );
            }

        };
    }

    private EventHandler<ActionEvent> removeConfigurationEventHandler() {
        return event -> {
            try {
                propertiesHelper.removeConfig(configurationNameComboBox.getValue());
                Map<String, Properties> propertiesMap = propertiesHelper.loadAllApplicationProperties();
                String newConfiguration = ArgName.configurationName.defaultValue();
                if (!propertiesMap.isEmpty()) {
                    Properties currentConfig = new ArrayList<>(propertiesMap.entrySet()).get(0).getValue();
                    newConfiguration = currentConfig.getProperty(ArgName.configurationName.name());
                }
                removeConfigurationNameFromComboBox(configurationNameComboBox.getValue(), newConfiguration);
                String[] currentArgs = propertiesHelper.loadArgumentArray(newConfiguration);
                applicationProperties = ApplicationPropertiesFactory.getInstance(currentArgs);
                setInitValues();
                configurationNameTextField.setText(configurationNameComboBox.getValue());
                setDisablePropertyOnRemoveConfigurationButton();
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.removed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withWindowType(WindowType.CONFIRMATION_WINDOW)
                        .withImage()
                        .buildAndDisplayWindow()
                );
            } catch (IllegalStateException ex) {
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(ex.getMessage())
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage()
                        .buildAndDisplayWindow()
                );
            }
        };
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
        setDisablePropertyOnRemoveConfigurationButton();
    }

    private void removeConfigurationNameFromComboBox(String oldValue, String newValue) {
        List<String> items = new ArrayList<>(configurationNameComboBox.getItems());
        items.remove(oldValue);
        updateItemsForConfigComboBox(newValue, FXCollections.observableList(items));
    }
    private void setListeners() {
        toolkitUsernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            userFolderUrl = applicationProperties.toolkitUserFolder();
            userFolderUrl = userFolderUrl.substring(0, userFolderUrl.lastIndexOf("/") + 1) + newValue;
        });

        configurationNameComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener(comboBoxValueChangeListener());
    }

    @NotNull
    private ChangeListener<String> comboBoxValueChangeListener() {
        return (options, oldValue, newValue) -> {
            if (useComboBoxValueChangeListener) {
                String[] args = propertiesHelper.loadArgumentArray(newValue);
                applicationProperties = ApplicationPropertiesFactory.getInstance(args);
                setInitValues();
                configurationNameTextField.setText(newValue);
            }
        };
    }

    @NotNull
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

}
