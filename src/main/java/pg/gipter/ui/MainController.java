package pg.gipter.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import pg.gipter.launcher.Runner;
import pg.gipter.producer.command.CodeProtection;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.util.PropertiesHelper;
import pg.gipter.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

class MainController extends AbstractController {

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
    private ComboBox<CodeProtection> codeProtectionComboBox;
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
    private CheckBox confirmationWindowCheckBox;
    @FXML
    private ComboBox<PreferredArgSource> preferredArgSourceComboBox;
    @FXML
    private CheckBox useUICheckBox;
    @FXML
    private CheckBox saveConfigurationCheckBox;

    @FXML
    private Button executeButton;
    @FXML
    private ComboBox<String> languageComboBox;

    private ApplicationProperties applicationProperties;

    private final String[] supportedLanguages = {"en", "pl"};
    private static String currentLanguage;
    private static boolean saveCurrentSettings = false;

    MainController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues(resources);
        setProperties(resources);
        setActions(resources);
        setListeners();
    }

    private void setInitValues(ResourceBundle resources) {
        authorsTextField.setText(String.join(",", applicationProperties.authors()));
        committerEmailTextField.setText(applicationProperties.committerEmail());
        gitAuthorTextField.setText(applicationProperties.gitAuthor());
        mercurialAuthorTextField.setText(applicationProperties.mercurialAuthor());
        svnAuthorTextField.setText(applicationProperties.svnAuthor());
        codeProtectionComboBox.setItems(FXCollections.observableArrayList(CodeProtection.values()));
        codeProtectionComboBox.setValue(applicationProperties.codeProtection());
        skipRemoteCheckBox.setSelected(applicationProperties.isSkipRemote());

        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
        toolkitDomainTextField.setText(applicationProperties.toolkitDomain());
        toolkitListNameTextField.setText(applicationProperties.toolkitListName());
        toolkitUrlTextField.setText(applicationProperties.toolkitUrl());
        toolkitWSTextField.setText(applicationProperties.toolkitWSUrl());
        toolkitUserFolderTextField.setText(applicationProperties.toolkitUserFolder());

        projectPathLabel.setText(String.join(",", applicationProperties.projectPaths()));
        String itemFileName = Paths.get(applicationProperties.itemPath()).getFileName().toString();
        String itemPath = applicationProperties.itemPath().substring(0, applicationProperties.itemPath().indexOf(itemFileName) - 1);
        itemPathLabel.setText(itemPath);
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());

        startDatePicker.setValue(applicationProperties.startDate());
        endDatePicker.setValue(applicationProperties.endDate());
        periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));

        confirmationWindowCheckBox.setSelected(applicationProperties.isConfirmationWindow());
        preferredArgSourceComboBox.setItems(FXCollections.observableArrayList(PreferredArgSource.values()));
        preferredArgSourceComboBox.setValue(PreferredArgSource.UI);
        useUICheckBox.setSelected(applicationProperties.isUseUI());
        saveConfigurationCheckBox.setSelected(saveCurrentSettings);

        languageComboBox.setItems(FXCollections.observableList(Arrays.asList(supportedLanguages)));
        if (StringUtils.nullOrEmpty(currentLanguage)) {
            if (StringUtils.nullOrEmpty(resources.getLocale().getLanguage())
                    || supportedLanguages[0].equals(resources.getLocale().getLanguage())) {
                currentLanguage = supportedLanguages[0];

            } else if (supportedLanguages[1].equals(resources.getLocale().getLanguage())) {
                currentLanguage = supportedLanguages[1];
            }
        }
        languageComboBox.setValue(currentLanguage);
    }

    private void setProperties(ResourceBundle resources) {
        toolkitDomainTextField.setEditable(false);
        toolkitListNameTextField.setEditable(false);
        toolkitUrlTextField.setEditable(false);
        toolkitWSTextField.setEditable(false);
        toolkitUserFolderTextField.setEditable(false);

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
    }

    private void setActions(ResourceBundle resources) {
        projectPathButton.setOnAction(projectPathActionEventHandler(resources));
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        executeButton.setOnAction(runActionEventHandler(resources));
    }

    private EventHandler<ActionEvent> projectPathActionEventHandler(final ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle(resources.getString("directory.item.title"));
            File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
            if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                String result = itemPathDirectory.getAbsolutePath();
                if (isAddPath(resources)) {
                    result = result + (StringUtils.nullOrEmpty(projectPathLabel.getText()) ? "" : ",") + projectPathLabel.getText();
                }
                projectPathLabel.setText(result);
                projectPathButton.setText(resources.getString("button.change"));
            }
        };
    }

    private boolean isAddPath(ResourceBundle resource) {
        String add = resource.getString("popup.addOrReplace.add");
        String replace = resource.getString("popup.addOrReplace.replace");
        ButtonType addButton = new ButtonType(add, ButtonBar.ButtonData.OK_DONE);
        ButtonType replaceButton = new ButtonType(replace, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                resource.getString("popup.addOrReplace.message"),
                addButton,
                replaceButton
        );
        setAlertCommonAttributes(resource, alert);

        return alert.showAndWait().orElse(replaceButton) == addButton;
    }

    private void setAlertCommonAttributes(ResourceBundle resource, Alert alert) {
        alert.setTitle(resource.getString("popup.title"));
        alert.setHeaderText(resource.getString("popup.header"));

        setImageOnAlertWindow(alert);
    }

    private EventHandler<ActionEvent> itemPathActionEventHandler(final ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle(resources.getString("directory.project.title"));
            File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
            if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                itemPathLabel.setText(itemPathDirectory.getAbsolutePath());
                itemPathButton.setText(resources.getString("button.change"));
            }
        };
    }

    private EventHandler<ActionEvent> runActionEventHandler(final ResourceBundle resources) {
        return event -> {
            String[] args = {
                    ArgName.author + "=" + authorsTextField.getText(),
                    ArgName.committerEmail + "=" + committerEmailTextField.getText(),
                    ArgName.gitAuthor + "=" + gitAuthorTextField.getText(),
                    ArgName.mercurialAuthor + "=" + mercurialAuthorTextField.getText(),
                    ArgName.svnAuthor + "=" + svnAuthorTextField.getText(),
                    ArgName.codeProtection + "=" + codeProtectionComboBox.getValue(),
                    ArgName.skipRemote + "=" + skipRemoteCheckBox.isSelected(),

                    ArgName.toolkitUsername + "=" + toolkitUsernameTextField.getText(),
                    ArgName.toolkitPassword + "=" + toolkitPasswordField.getText(),

                    ArgName.projectPath + "=" + projectPathLabel.getText(),
                    ArgName.itemPath + "=" + itemPathLabel.getText(),
                    ArgName.itemFileNamePrefix + "=" + itemFileNamePrefixTextField.getText(),

                    ArgName.startDate + "=" + startDatePicker.getValue().format(ApplicationProperties.yyyy_MM_dd),
                    ArgName.endDate + "=" + endDatePicker.getValue(),
                    ArgName.periodInDays + "=" + periodInDaysTextField.getText(),

                    ArgName.confirmationWindow + "=" + confirmationWindowCheckBox.isSelected(),
                    ArgName.preferredArgSource + "=" + PreferredArgSource.UI,
                    ArgName.useUI + "=" + useUICheckBox.isSelected()
            };
            if (saveConfigurationCheckBox.isSelected()) {
                saveCurrentConfiguration(resources, createProperties(args));
            }
            ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(args);
            Runner runner = new Runner(uiAppProperties);
            runner.run();
        };
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

    private void saveCurrentConfiguration(ResourceBundle resource, Properties properties) {
        String override = resource.getString("popup.overrideProperties.buttonOverride");
        String create = resource.getString("popup.overrideProperties.buttonUIProperties");
        ButtonType createButton = new ButtonType(create, ButtonBar.ButtonData.OK_DONE);
        ButtonType overrideButton = new ButtonType(override, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                resource.getString("popup.overrideProperties.message"),
                overrideButton,
                createButton
        );
        setAlertCommonAttributes(resource, alert);

        Optional<ButtonType> result = alert.showAndWait();
        PropertiesHelper helper = new PropertiesHelper();
        if (result.orElse(createButton) == overrideButton) {
            helper.saveToApplicationProperties(properties);
        } else {
            helper.saveToUIApplicationProperties(properties);
        }
    }

    private void setListeners() {
        languageComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    currentLanguage = languageComboBox.getValue();
                    saveCurrentSettings = saveConfigurationCheckBox.isSelected();
                    uiLauncher.changeLanguage(languageComboBox.getValue());
                }
        );
    }

}
