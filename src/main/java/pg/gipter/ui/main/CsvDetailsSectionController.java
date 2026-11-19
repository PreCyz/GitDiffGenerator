package pg.gipter.ui.main;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.services.vcs.VcsServiceFactory;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

class CsvDetailsSectionController extends AbstractController {

    private TextField authorsTextField;
    private TextField committerEmailTextField;
    private TextField gitAuthorTextField;
    private TextField mercurialAuthorTextField;
    private TextField svnAuthorTextField;
    private ComboBox<ItemType> itemTypeComboBox;
    private TextField toolkitProjectListNamesTextField;
    private CheckBox useDefaultAuthorCheckBox;
    private CheckBox useDefaultEmailCheckBox;

    private final MainController mainController;
    private VcsService vcsService;
    private Optional<String> defaultAuthor = Optional.empty();
    private Optional<String> defaultEmail = Optional.empty();

    CsvDetailsSectionController(UILauncher uiLauncher,
                                ApplicationProperties applicationProperties,
                                MainController mainController) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.mainController = mainController;
        this.vcsService = VcsServiceFactory.getInstance();
    }

    @SuppressWarnings("unchecked")
    void initialize(URL location, ResourceBundle resources, Map<String, Object> controlsMap,
                    Map<String, VersionControlSystem> repositories) {
        super.initialize(location, resources);

        authorsTextField = (TextField) controlsMap.get("authorsTextField");
        committerEmailTextField = (TextField) controlsMap.get("committerEmailTextField");
        gitAuthorTextField = (TextField) controlsMap.get("gitAuthorTextField");
        mercurialAuthorTextField = (TextField) controlsMap.get("mercurialAuthorTextField");
        svnAuthorTextField = (TextField) controlsMap.get("svnAuthorTextField");
        itemTypeComboBox = (ComboBox<ItemType>) controlsMap.get("itemTypeComboBox");
        toolkitProjectListNamesTextField = (TextField) controlsMap.get("toolkitProjectListNamesTextField");
        useDefaultAuthorCheckBox = (CheckBox) controlsMap.get("useDefaultAuthorCheckBox");
        useDefaultEmailCheckBox = (CheckBox) controlsMap.get("useDefaultEmailCheckBox");

        setInitValues();
        setProperties(repositories);
        setActions();
        setListeners();
    }

    private void setInitValues() {
        itemTypeComboBox.setItems(FXCollections.observableArrayList(ItemType.values()));
        itemTypeComboBox.setValue(applicationProperties.itemType());
        toolkitProjectListNamesTextField.setText(String.join(",", applicationProperties.toolkitProjectListNames()));
        if (ItemType.isCodeRelated(applicationProperties.itemType())) {
            authorsTextField.setText(String.join(",", applicationProperties.authors()));
            committerEmailTextField.setText(applicationProperties.committerEmail());
            gitAuthorTextField.setText(applicationProperties.gitAuthor());
            mercurialAuthorTextField.setText(applicationProperties.mercurialAuthor());
            svnAuthorTextField.setText(applicationProperties.svnAuthor());
        } else {
            authorsTextField.setText(null);
            committerEmailTextField.setText(null);
            gitAuthorTextField.setText(null);
            mercurialAuthorTextField.setText(null);
            svnAuthorTextField.setText(null);
        }
    }

    private void setProperties(Map<String, VersionControlSystem> repositories) {
        toolkitProjectListNamesTextField.setDisable(ItemType.TOOLKIT_DOCS != applicationProperties.itemType());
        setTooltipOnProjectListNames();

        setDisable(applicationProperties.itemType());

        setRepositoryFields(repositories);
    }

    private void setRepositoryFields(Map<String, VersionControlSystem> repositories) {
        if (!repositories.isEmpty()) {
            gitAuthorTextField.setDisable(!repositories.containsValue(VersionControlSystem.GIT));
            mercurialAuthorTextField.setDisable(!repositories.containsValue(VersionControlSystem.MERCURIAL));
            svnAuthorTextField.setDisable(!repositories.containsValue(VersionControlSystem.SVN));
        }

        defaultAuthor = Optional.empty();
        defaultEmail = Optional.empty();
        repositories.entrySet().stream()
                .filter(entry -> entry.getValue() == VersionControlSystem.GIT)
                .findFirst()
                .ifPresent(entry -> {
                    vcsService.setProjectPath(entry.getKey());
                    defaultAuthor = vcsService.getUserName();
                    defaultEmail = vcsService.getUserEmail();
                });

        useDefaultAuthorCheckBox.setDisable(defaultAuthor.map(name ->
                applicationProperties.authors().contains(ArgName.author.defaultValue())
                        || applicationProperties.authors().contains(name)
                        || name.equals(applicationProperties.gitAuthor())).orElse(true));
        useDefaultEmailCheckBox.setDisable(defaultEmail.map(email ->
                StringUtils.nullOrEmpty(applicationProperties.committerEmail())
                        || email.equals(applicationProperties.committerEmail())).orElse(true));
        useDefaultAuthorCheckBox.setTooltip(defaultAuthor
                .map(name -> gitTooltip("vcs.panel.useDefaultAuthor.tooltip", name)).orElse(null));
        useDefaultEmailCheckBox.setTooltip(defaultEmail
                .map(email -> gitTooltip("vcs.panel.useDefaultEmail.tooltip", email)).orElse(null));
    }

    private Tooltip gitTooltip(String key, String value) {
        Tooltip tooltip = new Tooltip(BundleUtils.getMsg(key, value));
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.setFont(Font.font("Courier New", 16));
        return tooltip;
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

    private void setActions() {
        itemTypeComboBox.setOnAction(uploadTypeActionEventHandler());
    }

    private EventHandler<ActionEvent> uploadTypeActionEventHandler() {
        return event -> {
            boolean disableProjectButton = itemTypeComboBox.getValue() == ItemType.STATEMENT;
            disableProjectButton |= applicationProperties.getRunConfigMap().isEmpty() &&
                    mainController.getConfigurationName().isEmpty();
            mainController.setDisableProjectPathButton(disableProjectButton);
            if (itemTypeComboBox.getValue() == ItemType.TOOLKIT_DOCS) {
                mainController.setEndDatePicker(LocalDate.now());
            }
            toolkitProjectListNamesTextField.setDisable(itemTypeComboBox.getValue() != ItemType.TOOLKIT_DOCS);
            boolean disable = !ItemType.isDocsRelated(itemTypeComboBox.getValue());
            mainController.disableDeleteDownloadedFiles(disable);
            setDisable(itemTypeComboBox.getValue());
            setRepositoryFields(ItemType.isCodeRelated(itemTypeComboBox.getValue())
                    ? VersionControlSystem.inspectProjectPaths(applicationProperties.projectPaths()) : Map.of());
            setTooltipOnProjectListNames();
        };
    }

    private void setListeners() {
        useDefaultAuthorCheckBox.selectedProperty().addListener(useDefaultAuthorChangeListener());
        useDefaultEmailCheckBox.selectedProperty().addListener(useDefaultEmailChangeListener());
        gitAuthorTextField.focusedProperty().addListener(gitAuthorFocusChangeListener());
        committerEmailTextField.focusedProperty().addListener(committerEmailFocusChangeListener());
    }

    private ChangeListener<? super Boolean> useDefaultAuthorChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                defaultAuthor.ifPresent(gitAuthorTextField::setText);
            } else {
                gitAuthorTextField.setText(applicationProperties.gitAuthor());
            }
        };
    }

    private ChangeListener<? super Boolean> useDefaultEmailChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                defaultEmail.ifPresent(committerEmailTextField::setText);
            } else {
                committerEmailTextField.setText(applicationProperties.committerEmail());
            }
        };
    }

    private ChangeListener<? super Boolean> gitAuthorFocusChangeListener() {
        return (ChangeListener<Boolean>) (observableValue, oldValue, newValue) -> {
            if (!newValue && gitAuthorTextField.getLength() > 0) {
                defaultAuthor.ifPresent(userName ->
                        useDefaultAuthorCheckBox.setDisable(gitAuthorTextField.getText().equals(userName))
                );
            }
        };
    }

    private ChangeListener<? super Boolean> committerEmailFocusChangeListener() {
        return (ChangeListener<Boolean>) (observableValue, oldValue, newValue) -> {
            if (!newValue && committerEmailTextField.getLength() > 0) {
                defaultEmail.ifPresent(userEmail ->
                        useDefaultEmailCheckBox.setDisable(committerEmailTextField.getText().equals(userEmail))
                );
            }
        };
    }

    void setDisable(ItemType itemType) {
        boolean disable = !ItemType.isCodeRelated(itemType);
        authorsTextField.setDisable(disable);
        committerEmailTextField.setDisable(disable);
        gitAuthorTextField.setDisable(disable);
        svnAuthorTextField.setDisable(disable);
        mercurialAuthorTextField.setDisable(disable);

        mainController.setDisable(itemType);
    }

    ItemType getItemType() {
        return itemTypeComboBox.getValue();
    }

    String getAuthors() {
        return authorsTextField.getText();
    }

    String getCommitterEmail() {
        return committerEmailTextField.getText();
    }

    String getGitAuthor() {
        return gitAuthorTextField.getText();
    }

    String getMercurialAuthor() {
        return mercurialAuthorTextField.getText();
    }

    String getSvnAuthor() {
        return svnAuthorTextField.getText();
    }

    String getToolkitProjectListNames() {
        return toolkitProjectListNamesTextField.getText();
    }

    public void setVcsService(VcsService vcsService) {
        this.vcsService = vcsService;
    }
}
