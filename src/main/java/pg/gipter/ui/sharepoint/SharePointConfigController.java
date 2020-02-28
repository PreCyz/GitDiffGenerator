package pg.gipter.ui.sharepoint;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.service.platform.AppManager;
import pg.gipter.service.platform.AppManagerFactory;
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

import static java.util.stream.Collectors.toCollection;

public class SharePointConfigController extends AbstractController {

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
    private ComboBox<SharePointConfig> sharePointUrlsComboBox;
    @FXML
    private Button addConfigButton;
    @FXML
    private Button removeConfigButton;
    @FXML
    private Label numberOfConfigsLabel;

    private ApplicationProperties applicationProperties;
    private RunConfig currentRunConfig;
    private LinkedHashSet<SharePointConfig> sharePointConfigSet;

    public SharePointConfigController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
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
            usernameTextField.setText(sharePointConfig.getUsername());
            passwordField.setText(sharePointConfig.getPassword());
            domainTextField.setText(sharePointConfig.getDomain());
            urlTextField.setText(sharePointConfig.getUrl());
            projectTextField.setText(sharePointConfig.getProject());
            listNameTextField.setText(sharePointConfig.getListName());
            sharePointLink.setText(calculateFullLink());
            sharePointUrlsComboBox.setItems(FXCollections.observableArrayList(sharePointConfigSet));
            sharePointUrlsComboBox.setValue(sharePointConfig);
        } else {
            sharePointConfig = new SharePointConfig();
            sharePointUrlsComboBox.setItems(FXCollections.observableArrayList(sharePointConfig));
            sharePointUrlsComboBox.setValue(sharePointConfig);
        }
        numberOfConfigsLabel.setText(
                BundleUtils.getMsg("sharepoint.numberOfConfigs", String.valueOf(sharePointConfigSet.size()))
        );
    }

    private String calculateFullLink() {
        Supplier<String> emptyStringSupplier = () -> "";
        Predicate<String> notEmptyStringPredicate = str -> !"".equals(str.trim());
        Function<String, String> addSlashFunction = value -> value.startsWith("/") ? value : "/" + value;

        String fullLink = Optional.ofNullable(urlTextField.getText()).orElseGet(emptyStringSupplier)
                +
                Optional.ofNullable(projectTextField.getText())
                        .filter(notEmptyStringPredicate)
                        .map(addSlashFunction)
                        .orElseGet(emptyStringSupplier)
                +
                Optional.ofNullable(listNameTextField.getText())
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
        sharePointUrlsComboBox.setConverter(comboBoxStringConverter());
        sharePointUrlsComboBox.setDisable(sharePointConfigSet.isEmpty());
        removeConfigButton.setDisable(sharePointConfigSet.isEmpty());
    }

    private StringConverter<SharePointConfig> comboBoxStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SharePointConfig sharePointConfig) {
                SharePointConfig sc = Optional.ofNullable(sharePointConfig).orElseGet(SharePointConfig::new);
                if (StringUtils.nullOrEmpty(sc.getProject()) && StringUtils.nullOrEmpty(sc.getListName())) {
                    return "";
                }
                return String.format("%s - %s", sc.getProject(), sc.getListName()).trim();
            }

            @Override
            public SharePointConfig fromString(final String string) {
                return sharePointConfigSet.stream()
                        .filter(sc -> String.format("%s - %s", sc.getProject(), sc.getListName()).trim().equals(string))
                        .collect(toCollection(LinkedList::new))
                        .getFirst();
            }
        };
    }

    private void setActions(ResourceBundle resources) {
        addConfigButton.setOnAction(addConfigActionEventHandler());
        removeConfigButton.setOnAction(removeConfigActionEventHandler(resources));
        sharePointLink.setOnMouseClicked(sharePointLinkMouseClickEventHandler());
    }

    private EventHandler<ActionEvent> addConfigActionEventHandler() {
        return actionEvent -> {
            SharePointConfig sharePointConfig = new SharePointConfig();
            sharePointConfig.setUsername(usernameTextField.getText());
            sharePointConfig.setPassword(passwordField.getText());
            sharePointConfig.setDomain(domainTextField.getText());
            sharePointConfig.setUrl(urlTextField.getText());
            sharePointConfig.setProject(projectTextField.getText());
            sharePointConfig.setListName(listNameTextField.getText());

            sharePointConfigSet.add(sharePointConfig);

            sharePointUrlsComboBox.setItems(FXCollections.observableArrayList(sharePointConfigSet));
            sharePointUrlsComboBox.setValue(sharePointConfig);
            sharePointUrlsComboBox.setDisable(sharePointConfigSet.isEmpty());

            currentRunConfig.addSharePointConfig(sharePointConfig);
            applicationProperties.updateCurrentRunConfig(currentRunConfig);
            applicationProperties.save();

            removeConfigButton.setDisable(sharePointConfigSet.isEmpty());

            numberOfConfigsLabel.setText(
                    BundleUtils.getMsg("sharepoint.numberOfConfigs", String.valueOf(sharePointConfigSet.size()))
            );
        };
    }

    private EventHandler<ActionEvent> removeConfigActionEventHandler(ResourceBundle resources) {
        return actionEvent -> {
            sharePointConfigSet.remove(sharePointUrlsComboBox.getValue());

            if (sharePointConfigSet.isEmpty()) {
                sharePointUrlsComboBox.setItems(FXCollections.emptyObservableList());
                usernameTextField.setText("");
                passwordField.setText("");
                domainTextField.setText("");
                urlTextField.setText("");
                projectTextField.setText("");
                listNameTextField.setText("");
                sharePointLink.setText(resources.getString("sharepoint.fullLink.default"));
                sharePointUrlsComboBox.setDisable(sharePointConfigSet.isEmpty());
                removeConfigButton.setDisable(sharePointConfigSet.isEmpty());
            } else {
                sharePointUrlsComboBox.setItems(FXCollections.observableArrayList(sharePointConfigSet));
                SharePointConfig valueToSelect = new LinkedList<>(sharePointConfigSet).getFirst();
                sharePointUrlsComboBox.setValue(valueToSelect);
                usernameTextField.setText(valueToSelect.getUsername());
                passwordField.setText(valueToSelect.getPassword());
                domainTextField.setText(valueToSelect.getDomain());
                urlTextField.setText(valueToSelect.getUrl());
                projectTextField.setText(valueToSelect.getProject());
                listNameTextField.setText(valueToSelect.getListName());
                sharePointLink.setText(calculateFullLink());
            }
            numberOfConfigsLabel.setText(
                    BundleUtils.getMsg("sharepoint.numberOfConfigs", String.valueOf(sharePointConfigSet.size()))
            );

            currentRunConfig.setSharePointConfigs(sharePointConfigSet);
            applicationProperties.updateCurrentRunConfig(currentRunConfig);
            applicationProperties.save();
        };
    }

    private EventHandler<? super MouseEvent> sharePointLinkMouseClickEventHandler() {
        return event -> {
            if (!sharePointLink.getText().equals(BundleUtils.getMsg("sharepoint.fullLink.default"))) {
                AppManager instance = AppManagerFactory.getInstance();
                instance.launchDefaultBrowser(calculateFullLink());
                sharePointLink.setVisited(false);
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
