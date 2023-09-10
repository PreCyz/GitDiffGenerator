package pg.gipter.ui.main;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

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

    CsvDetailsSectionController(UILauncher uiLauncher,
                                ApplicationProperties applicationProperties,
                                MainController mainController) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.mainController = mainController;
        this.vcsService = VcsServiceFactory.getInstance();
    }

    @SuppressWarnings("unchecked")
    void initialize(URL location, ResourceBundle resources, Map<String, Object> controlsMap) {
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
        setProperties();
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

    private void setProperties() {
        toolkitProjectListNamesTextField.setDisable(ItemType.TOOLKIT_DOCS != applicationProperties.itemType());
        setTooltipOnProjectListNames();

        setDisable(applicationProperties.itemType());

        useDefaultAuthorCheckBox.setDisable(disableDefaultAuthor());
        setTooltipOnUseDefaultAuthor();
        useDefaultEmailCheckBox.setDisable(disableDefaultEmail());
        setTooltipOnUseDefaultEmail();
        if (ItemType.isCodeRelated(applicationProperties.itemType()) &&
                applicationProperties.projectPaths()
                        .stream()
                        .noneMatch(projectPath -> ArgName.projectPath.defaultValue().equals(projectPath))
        ) {
            gitAuthorTextField.setDisable(applicationProperties.projectPaths()
                    .stream()
                    .noneMatch(path -> VersionControlSystem.valueFrom(Paths.get(path)) == VersionControlSystem.GIT)
            );
            mercurialAuthorTextField.setDisable(applicationProperties.projectPaths()
                    .stream()
                    .noneMatch(path -> VersionControlSystem.valueFrom(Paths.get(path)) == VersionControlSystem.MERCURIAL)
            );
            svnAuthorTextField.setDisable(applicationProperties.projectPaths()
                    .stream()
                    .noneMatch(path -> VersionControlSystem.valueFrom(Paths.get(path)) == VersionControlSystem.SVN)
            );
        }
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

    private void setTooltipOnUseDefaultAuthor() {
        if (ItemType.isCodeRelated(applicationProperties.itemType())) {
            vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
            String userName = vcsService.getUserName().orElseGet(() -> "");
            Tooltip tooltip = new Tooltip(BundleUtils.getMsg("vcs.panel.useDefaultAuthor.tooltip", userName));
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.setFont(Font.font("Courier New", 16));
            useDefaultAuthorCheckBox.setTooltip(tooltip);
        }
    }

    private void setTooltipOnUseDefaultEmail() {
        if (ItemType.isCodeRelated(applicationProperties.itemType())) {
            vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
            String userEmail = vcsService.getUserEmail().orElseGet(() -> "");
            Tooltip tooltip = new Tooltip(BundleUtils.getMsg("vcs.panel.useDefaultEmail.tooltip", userEmail));
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.setFont(Font.font("Courier New", 16));
            useDefaultEmailCheckBox.setTooltip(tooltip);
        }
    }

    private boolean disableDefaultAuthor() {
        boolean disabled = true;
        if (ItemType.isCodeRelated(applicationProperties.itemType())) {
            Set<VersionControlSystem> vcsSet = applicationProperties.projectPaths()
                    .stream()
                    .map(versionControlSystemFunction())
                    .collect(toSet());

            if (vcsSet.contains(VersionControlSystem.GIT)) {
                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                Optional<String> userName = vcsService.getUserName();
                if (userName.isPresent() && !applicationProperties.authors().contains(ArgName.author.defaultValue())) {
                    disabled = applicationProperties.authors().contains(userName.get());
                    disabled |= StringUtils.notEmpty(applicationProperties.gitAuthor()) &&
                            userName.get().equals(applicationProperties.gitAuthor());
                }
            }
        }
        return disabled;
    }

    private Function<String, VersionControlSystem> versionControlSystemFunction() {
        return projectPath -> {
            try {
                return VersionControlSystem.valueFrom(Paths.get(projectPath));
            } catch (IllegalArgumentException ex) {
                return VersionControlSystem.NA;
            }
        };
    }

    private boolean disableDefaultEmail() {
        boolean disabled = true;
        if (ItemType.isCodeRelated(applicationProperties.itemType())) {
            Set<VersionControlSystem> vcsSet = applicationProperties.projectPaths()
                    .stream()
                    .map(versionControlSystemFunction())
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
            if (!newValue && !gitAuthorTextField.getText().isEmpty()) {
                vcsService.setProjectPath(new LinkedList<>(applicationProperties.projectPaths()).getFirst());
                vcsService.getUserName().ifPresent(userName ->
                        useDefaultAuthorCheckBox.setDisable(gitAuthorTextField.getText().equals(userName))
                );
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
