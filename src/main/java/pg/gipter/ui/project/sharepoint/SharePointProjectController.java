package pg.gipter.ui.project.sharepoint;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public class SharePointProjectController extends AbstractController {

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField domainTextField;
    @FXML
    private TextField urlTextField;//https://netcompany.sharepoint.com/sites/TrueBlue/Delte%20dokumenter/Forms/AllItems.aspx
    @FXML
    private TextField projectTextField;
    @FXML
    private TextField listNameTextField;
    @FXML
    private Hyperlink sharePointLink;
    @FXML
    private ComboBox<SharePointConfig> sharePointProjectsComboBox;
    @FXML
    private Button addConfigButton;
    @FXML
    private Button removeConfigButton;
    @FXML
    private Label numberOfConfigsLabel;

    private final RunConfig currentRunConfig;
    private final LinkedHashSet<SharePointConfig> sharePointConfigSet;

    public SharePointProjectController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.currentRunConfig = applicationProperties.getRunConfig(applicationProperties.configurationName())
                .orElseGet(RunConfig::new);
        this.sharePointConfigSet = Optional.ofNullable(this.currentRunConfig.getSharePointConfigs())
                .map(LinkedHashSet::new)
                .orElseGet(LinkedHashSet::new);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setProperties();
        setActions(resources);
        setListeners();
        //setAccelerators();
    }

    private void setInitValues() {
        SharePointConfig sharePointConfig;
        if (!sharePointConfigSet.isEmpty()) {
            sharePointConfig = Optional.of(sharePointConfigSet)
                    .map(set -> new LinkedList<>(set).getFirst())
                    .orElseGet(SharePointConfig::new);
            updateUIControls(sharePointConfig);
            sharePointProjectsComboBox.setItems(FXCollections.observableArrayList(sharePointConfigSet));
        } else {
            sharePointConfig = new SharePointConfig();
            sharePointProjectsComboBox.setItems(FXCollections.observableArrayList(sharePointConfig));
        }
        sharePointProjectsComboBox.setValue(sharePointConfig);
        numberOfConfigsLabel.setText(
                BundleUtils.getMsg("sharepoint.numberOfConfigs", String.valueOf(sharePointConfigSet.size()))
        );
    }

    private void updateUIControls(SharePointConfig valueToSelect) {
        nameTextField.setText(valueToSelect.getName());
        urlTextField.setText(valueToSelect.getUrl());
        projectTextField.setText(valueToSelect.getProject());
        listNameTextField.setText(
                Optional.ofNullable(valueToSelect)
                        .map(array -> String.join(",", array.getListNames()))
                        .orElseGet(() -> "")
        );
        sharePointLink.setText(calculateFullLink());
    }

    private String calculateFullLink() {
        Supplier<String> emptyStringSupplier = () -> "";
        Predicate<String> notEmptyStringPredicate = str -> !"".equals(str.trim());
        Function<String, String> addSlashFunction = value -> value.startsWith("/") ? value : "/" + value;

        String fullLink =
                Optional.ofNullable(urlTextField.getText()).orElseGet(emptyStringSupplier) +
                        Optional.ofNullable(projectTextField.getText())
                                .filter(notEmptyStringPredicate)
                                .map(addSlashFunction)
                                .orElseGet(emptyStringSupplier);

        if (StringUtils.notEmpty(fullLink)) {
            fullLink += SharePointConfig.URL_SUFFIX;
        } else {
            fullLink = BundleUtils.getMsg("sharepoint.fullLink.default");
        }
        return fullLink;
    }

    private void setProperties() {
        sharePointProjectsComboBox.setConverter(comboBoxStringConverter());
        sharePointProjectsComboBox.setDisable(sharePointConfigSet.isEmpty());
        removeConfigButton.setDisable(sharePointConfigSet.isEmpty());
    }

    private StringConverter<SharePointConfig> comboBoxStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SharePointConfig sharePointConfig) {
                SharePointConfig sc = Optional.ofNullable(sharePointConfig).orElseGet(SharePointConfig::new);
                if (StringUtils.nullOrEmpty(sc.getName())) {
                    return "";
                }
                return Optional.ofNullable(sc.getName()).orElseGet(() -> "").trim();
            }

            @Override
            public SharePointConfig fromString(final String string) {
                return sharePointConfigSet.stream()
                        .filter(sc -> sc.getName().trim().equals(string))
                        .collect(toCollection(LinkedList::new))
                        .getFirst();
            }
        };
    }

    private void setActions(ResourceBundle resources) {
        addConfigButton.setOnAction(addConfigActionEventHandler());
        removeConfigButton.setOnAction(removeConfigActionEventHandler(resources));
        sharePointLink.setOnMouseClicked(sharePointLinkMouseClickEventHandler());
        sharePointProjectsComboBox.setOnAction(sharePointProjectsComboBoxActionEventHandler());
    }

    private EventHandler<ActionEvent> addConfigActionEventHandler() {
        return actionEvent -> {
            SharePointConfig sharePointConfig = new SharePointConfig();
            sharePointConfig.setName(nameTextField.getText());
            sharePointConfig.setUrl(urlTextField.getText());
            sharePointConfig.setProject(projectTextField.getText());
            sharePointConfig.setListNames(
                    Optional.ofNullable(listNameTextField.getText())
                            .map(s -> Stream.of(listNameTextField.getText().split(",")).collect(toCollection(LinkedHashSet::new)))
                            .orElseGet(LinkedHashSet::new)
            );

            sharePointConfigSet.add(sharePointConfig);

            sharePointProjectsComboBox.setItems(FXCollections.observableArrayList(sharePointConfigSet));
            sharePointProjectsComboBox.setValue(sharePointConfig);
            sharePointProjectsComboBox.setDisable(sharePointConfigSet.isEmpty());

            String projects = Optional.ofNullable(currentRunConfig.getProjectPath()).orElseGet(() -> "");
            if (projects.isEmpty()) {
                projects = sharePointConfig.getProject();
            } else {
                projects += "," + sharePointConfig.getProject();
            }

            currentRunConfig.setProjectPath(projects);
            currentRunConfig.addSharePointConfig(sharePointConfig);
            applicationProperties.updateCurrentRunConfig(currentRunConfig);
            applicationProperties.save();

            removeConfigButton.setDisable(sharePointConfigSet.isEmpty());

            numberOfConfigsLabel.setText(
                    BundleUtils.getMsg("sharepoint.numberOfConfigs", String.valueOf(sharePointConfigSet.size()))
            );

            if (uiLauncher.hasWizardProperties()) {
                uiLauncher.addPropertyToWizard(ArgName.projectPath.name(), projects);
                uiLauncher.addPropertyToWizard(SharePointConfig.SHARE_POINT_CONFIGS, sharePointConfigSet);
            }
        };
    }

    private EventHandler<ActionEvent> removeConfigActionEventHandler(ResourceBundle resources) {
        return actionEvent -> {
            SharePointConfig valueToRemove = sharePointProjectsComboBox.getValue();
            sharePointConfigSet.remove(valueToRemove);

            String projects = "";
            if (sharePointConfigSet.isEmpty()) {
                sharePointProjectsComboBox.setItems(FXCollections.emptyObservableList());
                nameTextField.clear();
                usernameTextField.clear();
                passwordField.clear();
                domainTextField.clear();
                urlTextField.clear();
                projectTextField.clear();
                listNameTextField.clear();
                sharePointLink.setText(resources.getString("sharepoint.fullLink.default"));
                sharePointProjectsComboBox.setDisable(sharePointConfigSet.isEmpty());
                removeConfigButton.setDisable(sharePointConfigSet.isEmpty());
            } else {
                sharePointProjectsComboBox.setItems(FXCollections.observableArrayList(sharePointConfigSet));
                SharePointConfig valueToSelect = new LinkedList<>(sharePointConfigSet).getFirst();
                sharePointProjectsComboBox.setValue(valueToSelect);
                updateUIControls(valueToSelect);
                projects = sharePointConfigSet.stream().map(SharePointConfig::getProject).collect(joining(","));
            }
            numberOfConfigsLabel.setText(
                    BundleUtils.getMsg("sharepoint.numberOfConfigs", String.valueOf(sharePointConfigSet.size()))
            );

            currentRunConfig.setProjectPath(projects);
            currentRunConfig.setSharePointConfigs(sharePointConfigSet);
            applicationProperties.updateCurrentRunConfig(currentRunConfig);
            applicationProperties.save();
            if (uiLauncher.hasWizardProperties()) {
                uiLauncher.addPropertyToWizard(ArgName.projectPath.name(), projects);
            }
        };
    }

    private EventHandler<? super MouseEvent> sharePointLinkMouseClickEventHandler() {
        return event -> {
            if (!BundleUtils.getMsg("sharepoint.fullLink.default").equals(sharePointLink.getText())) {
                AppManager instance = AppManagerFactory.getInstance();
                instance.launchDefaultBrowser(calculateFullLink());
                sharePointLink.setVisited(false);
            }
        };
    }

    private EventHandler<ActionEvent> sharePointProjectsComboBoxActionEventHandler() {
        return actionEvent -> {
            SharePointConfig selectedValue = sharePointProjectsComboBox.getValue();
            if (selectedValue != null) {
                updateUIControls(selectedValue);
            }
        };
    }

    private void setListeners() {
        urlTextField.textProperty().addListener(textFieldChangeListener());
        projectTextField.textProperty().addListener(textFieldChangeListener());
        listNameTextField.textProperty().addListener(textFieldChangeListener());
    }

    private ChangeListener<String> textFieldChangeListener() {
        return (observableValue, oldValue, newValue) -> sharePointLink.setText(calculateFullLink());
    }
}
