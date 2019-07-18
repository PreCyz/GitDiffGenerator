package pg.gipter.ui.main;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import pg.gipter.producer.command.UploadType;
import pg.gipter.service.StartupService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.FXRunner;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.PropertiesHelper;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static pg.gipter.settings.ApplicationProperties.yyyy_MM_dd;

public class MainController extends AbstractController {

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
    private TextField toolkitListNameTextField;
    @FXML
    private TextField toolkitUrlTextField;
    @FXML
    private TextField toolkitWSTextField;
    @FXML
    private TextField toolkitUserFolderTextField;
    @FXML
    private TextField toolkitProjectListNamesTextField;
    @FXML
    private CheckBox deleteDownloadedFilesCheckBox;
    @FXML
    private CheckBox uploadAsHtmlCheckBox;

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
    private CheckBox confirmationWindowCheckBox;
    @FXML
    private ComboBox<PreferredArgSource> preferredArgSourceComboBox;
    @FXML
    private CheckBox useUICheckBox;
    @FXML
    private CheckBox activeteTrayCheckBox;
    @FXML
    private CheckBox autostartCheckBox;
    @FXML
    private Button saveConfigurationButton;

    @FXML
    private Button executeButton;
    @FXML
    private Button deamonButton;
    @FXML
    private Button exitButton;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label infoLabel;
    @FXML
    private ComboBox<String> configurationName;
    @FXML
    private Button addConfigurationButton;
    @FXML
    private Button deleteConfigurationButton;

    private ApplicationProperties applicationProperties;
    private PropertiesHelper propertiesHelper;

    private static String currentLanguage;

    public MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        propertiesHelper = new PropertiesHelper();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues(resources);
        setProperties(resources);
        setActions(resources);
        setListeners(resources);
    }

    private void setInitValues(ResourceBundle resources) {
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
        toolkitListNameTextField.setText(applicationProperties.toolkitCopyListName());
        toolkitUrlTextField.setText(applicationProperties.toolkitUrl());
        toolkitWSTextField.setText(applicationProperties.toolkitWSUrl());
        toolkitUserFolderTextField.setText(applicationProperties.toolkitUserFolder());
        toolkitProjectListNamesTextField.setText(String.join(",", applicationProperties.toolkitProjectListNames()));
        deleteDownloadedFilesCheckBox.setSelected(applicationProperties.isDeleteDownloadedFiles());
        uploadAsHtmlCheckBox.setSelected(applicationProperties.isUploadAsHtml());

        projectPathLabel.setText(String.join(",", applicationProperties.projectPaths()));
        String itemFileName = Paths.get(applicationProperties.itemPath()).getFileName().toString();
        String itemPath = applicationProperties.itemPath().substring(0, applicationProperties.itemPath().indexOf(itemFileName) - 1);
        itemPathLabel.setText(itemPath);
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());
        useAsFileNameCheckBox.setSelected(applicationProperties.isUseAsFileName());

        startDatePicker.setValue(LocalDate.now().minusDays(applicationProperties.periodInDays()));
        endDatePicker.setValue(LocalDate.now());
        periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));

        confirmationWindowCheckBox.setSelected(applicationProperties.isConfirmationWindow());
        preferredArgSourceComboBox.setItems(FXCollections.observableArrayList(PreferredArgSource.values()));
        preferredArgSourceComboBox.setValue(PreferredArgSource.UI);
        useUICheckBox.setSelected(applicationProperties.isUseUI());
        activeteTrayCheckBox.setSelected(uiLauncher.isTrayActivated());
        autostartCheckBox.setSelected(applicationProperties.isEnableOnStartup() && uiLauncher.isTrayActivated());

        languageComboBox.setItems(FXCollections.observableList(Arrays.asList(BundleUtils.SUPPORTED_LANGUAGES)));
        if (StringUtils.nullOrEmpty(currentLanguage)) {
            if (StringUtils.nullOrEmpty(resources.getLocale().getLanguage())
                    || BundleUtils.SUPPORTED_LANGUAGES[0].equals(resources.getLocale().getLanguage())) {
                currentLanguage = BundleUtils.SUPPORTED_LANGUAGES[0];

            } else if (BundleUtils.SUPPORTED_LANGUAGES[1].equals(resources.getLocale().getLanguage())) {
                currentLanguage = BundleUtils.SUPPORTED_LANGUAGES[1];
            }
        }
        languageComboBox.setValue(currentLanguage);

        Set<String> confNames = propertiesHelper.loadAllApplicationProperties().keySet();
        if (!StringUtils.nullOrEmpty(configurationName.getValue())) {
            confNames.add(configurationName.getValue());
        }
        configurationName.setItems(FXCollections.observableList(new ArrayList<>(confNames)));
        configurationName.setValue(applicationProperties.configurationName());
    }

    private void setProperties(ResourceBundle resources) {
        toolkitDomainTextField.setEditable(false);
        toolkitListNameTextField.setEditable(false);
        toolkitUrlTextField.setEditable(false);
        toolkitWSTextField.setEditable(false);
        toolkitUserFolderTextField.setEditable(false);
        toolkitProjectListNamesTextField.setDisable(applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS);
        deleteDownloadedFilesCheckBox.setDisable(applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS || applicationProperties.isUploadAsHtml());
        uploadAsHtmlCheckBox.setDisable(applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS);

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

        activeteTrayCheckBox.setDisable(!uiLauncher.isTraySupported());
        autostartCheckBox.setDisable(!uiLauncher.isTraySupported());
        progressIndicator.setVisible(false);
        useUICheckBox.setDisable(true);
        preferredArgSourceComboBox.setDisable(true);
        projectPathLabel.setTooltip(buildProjectPathsTooltip(projectPathLabel.getText()));
        if (uiLauncher.isTrayActivated()) {
            deamonButton.setText(resources.getString("button.deamon"));
        } else {
            deamonButton.setText(resources.getString("button.job"));
        }
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
        projectPathButton.setOnAction(projectPathActionEventHandler());
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        uploadTypeComboBox.setOnAction(uploadTypeActionEventHandler());
        executeButton.setOnAction(executeActionEventHandler());
        deamonButton.setOnAction(deamonActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
        saveConfigurationButton.setOnAction(saveConfigurationActionEventHandler(resources));
        addConfigurationButton.setOnAction(addConfigurationEventHandler());
    }

    private EventHandler<ActionEvent> projectPathActionEventHandler() {
        return event -> {
            String[] argsFromUI = createArgsFromUI();
            propertiesHelper.addAndSaveApplicationProperties(createProperties(argsFromUI));
            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(argsFromUI));
            uiLauncher.hideMainWindow();
            uiLauncher.showProjectsWindow();
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

        argList.add(ArgName.toolkitUsername + "=" + toolkitUsernameTextField.getText());
        argList.add(ArgName.toolkitPassword + "=" + toolkitPasswordField.getText());
        if (!StringUtils.nullOrEmpty(toolkitProjectListNamesTextField.getText())) {
            argList.add(ArgName.toolkitProjectListNames + "=" + toolkitProjectListNamesTextField.getText());
        }
        argList.add(ArgName.deleteDownloadedFiles + "=" + deleteDownloadedFilesCheckBox.isSelected());
        argList.add(ArgName.uploadAsHtml + "=" + uploadAsHtmlCheckBox.isSelected());

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

        argList.add(ArgName.confirmationWindow + "=" + confirmationWindowCheckBox.isSelected());
        argList.add(ArgName.preferredArgSource + "=" + PreferredArgSource.UI);
        argList.add(ArgName.useUI + "=" + useUICheckBox.isSelected());
        argList.add(ArgName.activeTray + "=" + activeteTrayCheckBox.isSelected());
        argList.add(ArgName.enableOnStartup + "=" + autostartCheckBox.isSelected());
        argList.add(ArgName.configurationName + "=" + configurationName.getValue());

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
            deleteDownloadedFilesCheckBox.setDisable(uploadTypeComboBox.getValue() != UploadType.TOOLKIT_DOCS || uploadAsHtmlCheckBox.isSelected());
            uploadAsHtmlCheckBox.setDisable(uploadTypeComboBox.getValue() != UploadType.TOOLKIT_DOCS);
            endDatePicker.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            authorsTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            committerEmailTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            gitAuthorTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            svnAuthorTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            mercurialAuthorTextField.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
            skipRemoteCheckBox.setDisable(uploadTypeComboBox.getValue() == UploadType.TOOLKIT_DOCS);
        };
    }

    private void saveCurrentConfiguration(ResourceBundle resource, Properties properties) {
        boolean isOverride = new AlertWindowBuilder()
                .withHeaderText(resource.getString("popup.overrideProperties.message"))
                .withOverrideText(resource.getString("popup.overrideProperties.buttonOverride"))
                .withCreateText(resource.getString("popup.overrideProperties.buttonUIProperties"))
                .withAlertType(Alert.AlertType.CONFIRMATION)
                .withWindowType(WindowType.OVERRIDE_WINDOW)
                .withImage()
                .buildAndDisplayOverrideWindow();

        if (isOverride) {
            properties.replace(ArgName.preferredArgSource.name(), PreferredArgSource.FILE.name());
            propertiesHelper.addAndSaveApplicationProperties(properties);
        } else {
            properties.remove(ArgName.startDate.name());
            properties.remove(ArgName.endDate.name());
            propertiesHelper.addAndSaveApplicationProperties(properties);
        }
    }

    private Properties createProperties(String[] args) {
        Properties properties = new Properties();
        for (String arg : args) {
            String key = arg.substring(0, arg.indexOf("="));
            String value = arg.substring(arg.indexOf("=") + 1);
            properties.setProperty(key, value);
        }
        return properties;
    }

    private EventHandler<ActionEvent> deamonActionEventHandler() {
        return event -> {
            if (uiLauncher.isTraySupported()) {
                String[] argsFromUI = createArgsFromUI();
                propertiesHelper.addAndSaveApplicationProperties(createProperties(argsFromUI));
                ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(argsFromUI);
                if (uiAppProperties.isActiveTray()) {
                    uiLauncher.updateTray(uiAppProperties);
                    uiLauncher.hideToTray();
                } else {
                    uiLauncher.showJobWindow();
                }
            } else {
                uiLauncher.showJobWindow();
            }
        };
    }

    private EventHandler<ActionEvent> exitActionEventHandler() {
        return event -> UILauncher.platformExit();
    }

    private EventHandler<ActionEvent> saveConfigurationActionEventHandler(ResourceBundle resources) {
        return event -> {
            String[] args = createArgsFromUI();
            saveCurrentConfiguration(resources, createProperties(args));
            ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(args);
            uiLauncher.updateTray(uiAppProperties);
        };
    }

    private EventHandler<ActionEvent> addConfigurationEventHandler() {
        return event -> {
            String[] args = new String[ArgName.values().length];
            int idx = 0;
            for (ArgName argName : ArgName.values()) {
                String value = argName.defaultValue();
                if (argName == ArgName.preferredArgSource) {
                    value = PreferredArgSource.UI.name();
                }
                args[idx++] = String.format("%s=%s", argName.name(), value);
            }
            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(args));
            uiLauncher.hideMainWindow();
            uiLauncher.showNewConfigurationWindow();
        };
    }

    private void setListeners(final ResourceBundle resources) {
        languageComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                            currentLanguage = languageComboBox.getValue();
                            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(createArgsFromUI()));
                            uiLauncher.changeLanguage(languageComboBox.getValue());
                        }
                );

        toolkitUsernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String userFolder = toolkitUserFolderTextField.getText();
            userFolder = userFolder.substring(0, userFolder.lastIndexOf("/") + 1) + newValue;
            toolkitUserFolderTextField.setText(userFolder);
        });

        final StartupService startupService = new StartupService();
        activeteTrayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                deamonButton.setText(resources.getString("button.deamon"));
                ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(createArgsFromUI());
                uiLauncher.setApplicationProperties(uiAppProperties);
                uiLauncher.initTrayHandler();
                uiLauncher.currentWindow().setOnCloseRequest(uiLauncher.trayOnCloseEventHandler());
                autostartCheckBox.setDisable(false);
            } else {
                deamonButton.setText(resources.getString("button.job"));
                uiLauncher.currentWindow().setOnCloseRequest(AbstractController.regularOnCloseEventHandler());
                uiLauncher.removeTray();
                autostartCheckBox.setDisable(true);
                autostartCheckBox.setSelected(false);
                startupService.disableStartOnStartup();
            }
        });

        autostartCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startupService.startOnStartup();
            } else {
                startupService.disableStartOnStartup();
            }
        });

        uploadAsHtmlCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                deleteDownloadedFilesCheckBox.setDisable(newValue));
    }

}
