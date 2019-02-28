package pg.gipter.launcher;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.util.StringUtils;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.PreferredArgSource;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TextField authorsTextField;
    @FXML private TextField committerEmailTextField;
    @FXML private TextField gitAuthorTextField;
    @FXML private TextField mercurialAuthorTextField;
    @FXML private TextField svnAuthorTextField;
    @FXML private ComboBox<CodeProtection> codeProtectionComboBox;
    @FXML private CheckBox skipRemoteCheckBox;

    @FXML private TextField toolkitUsernameTextField;
    @FXML private PasswordField toolkitPasswordField;
    @FXML private TextField toolkitDomainTextField;
    @FXML private TextField toolkitListNameTextField;
    @FXML private TextField toolkitUrlTextField;
    @FXML private TextField toolkitWSTextField;
    @FXML private TextField toolkitUserFolderTextField;

    @FXML private Label projectPathLabel;
    @FXML private Label itemPathLabel;
    @FXML private TextField itemFileNamePrefixTextField;
    @FXML private Button projectPathButton;
    @FXML private Button itemPathButton;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField periodInDaysTextField;

    @FXML private CheckBox confirmationWindowCheckBox;
    @FXML private ComboBox<PreferredArgSource> preferredArgSourceComboBox;
    @FXML private CheckBox useUICheckBox;
    @FXML private CheckBox saveConfigurationCheckBox;

    @FXML private Button runButton;

    private ApplicationProperties applicationProperties;
    private Runnable runner;

    MainController(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.runner = new Runner(applicationProperties);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setInitValues();
        setProperties();
        setActions();
    }

    private void setInitValues() {
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
        itemPathLabel.setText(applicationProperties.itemPath());
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());

        startDatePicker.setValue(applicationProperties.startDate());
        endDatePicker.setValue(applicationProperties.endDate());
        periodInDaysTextField.setText(String.valueOf(applicationProperties.periodInDays()));

        confirmationWindowCheckBox.setSelected(applicationProperties.isConfirmationWindow());
        preferredArgSourceComboBox.setItems(FXCollections.observableArrayList(PreferredArgSource.values()));
        preferredArgSourceComboBox.setValue(applicationProperties.preferredArgSource());
        useUICheckBox.setSelected(applicationProperties.isUseUI());
        saveConfigurationCheckBox.setSelected(false);
    }

    private void setProperties() {
        toolkitDomainTextField.setDisable(true);
        toolkitListNameTextField.setDisable(true);
        toolkitUrlTextField.setDisable(true);
        toolkitWSTextField.setDisable(true);
        toolkitUserFolderTextField.setDisable(true);

        projectPathButton.setVisible(applicationProperties.projectPaths().isEmpty());
        itemPathButton.setVisible(StringUtils.nullOrEmpty(applicationProperties.itemPath()));
    }

    private void setActions() {
        runButton.setOnAction(event -> runner.run());
        projectPathButton.setOnAction(event -> {});
        itemPathButton.setOnAction(event -> {});
    }

}
