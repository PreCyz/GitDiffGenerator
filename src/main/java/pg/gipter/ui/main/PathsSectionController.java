package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.NamePatternValue;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.IntelliSenseService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import static java.util.stream.Collectors.joining;

class PathsSectionController extends AbstractController {

    private Label projectPathLabel;
    private Label itemPathLabel;
    private TextField itemFileNamePrefixTextField;
    private Button projectPathButton;
    private Button itemPathButton;

    private final MainController mainController;

    private final Set<String> definedPatterns;
    private String currentItemFileNamePrefixValue = "";
    private final IntelliSenseService<NamePatternValue> intelliSenseService;
    boolean ignoreItemPrefixNameListener = false;

    PathsSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties, MainController mainController) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.mainController = mainController;
        intelliSenseService = new IntelliSenseService<>(NamePatternValue.class);
        this.definedPatterns = intelliSenseService.getDefinedPatterns();
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Control> controlsMap) {
        super.initialize(location, resources);
        projectPathLabel = (Label) controlsMap.get("projectPathLabel");
        itemPathLabel = (Label) controlsMap.get("itemPathLabel");
        itemFileNamePrefixTextField = (TextField) controlsMap.get("itemFileNamePrefixTextField");
        projectPathButton = (Button) controlsMap.get("projectPathButton");
        itemPathButton = (Button) controlsMap.get("itemPathButton");
        setInitValues();
        setProperties(resources);
        setActions(resources);
        setListeners();
    }

    private void setInitValues() {
        projectPathLabel.setText(applicationProperties.projectPaths()
                .stream()
                .map(path -> path.substring(path.lastIndexOf(File.separator) + 1))
                .collect(joining(","))
        );
        String itemFileName = Paths.get(applicationProperties.itemPath()).getFileName().toString();
        if (applicationProperties.itemPath().contains(ArgName.itemPath.defaultValue())) {
            itemPathLabel.setText(ArgName.itemPath.defaultValue());
        } else {
            String itemPath = applicationProperties.itemPath()
                    .substring(0, applicationProperties.itemPath().indexOf(itemFileName) - 1);
            itemPathLabel.setText(itemPath);
            Platform.runLater(() -> {
                if (Files.notExists(Paths.get(itemPath))) {
                    new AlertWindowBuilder()
                            .withAlertType(Alert.AlertType.ERROR)
                            .withMessage(BundleUtils.getMsg(
                                    "paths.panel.itemPath.nonExists",
                                    itemPath,
                                    System.getProperty("line.separator")
                            ))
                            .withWebViewDetails(WebViewService.getInstance().pullFailWebView())
                            .buildAndDisplayWindow();
                }
            });
        }
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            itemPathLabel.setText(applicationProperties.itemPath());
        }
        projectPathLabel.setTooltip(buildPathTooltip(projectPathLabel.getText()));
        itemPathLabel.setTooltip(buildPathTooltip(itemPathLabel.getText()));
        itemFileNamePrefixTextField.setText(applicationProperties.itemFileNamePrefix());
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

    private void setProperties(ResourceBundle resources) {
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

        TextFields.bindAutoCompletion(itemFileNamePrefixTextField, itemNameSuggestionsCallback());
    }

    private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> itemNameSuggestionsCallback() {
        return param -> {
            if (itemFileNamePrefixTextField.getCaretPosition() < param.getUserText().length()) {
                itemFileNamePrefixTextField.positionCaret(param.getUserText().length());
            }
            return intelliSenseService.getFilteredValues(param.getUserText());
        };
    }

    private void setActions(ResourceBundle resources) {
        projectPathButton.setOnAction(projectPathActionEventHandler());
        itemPathButton.setOnAction(itemPathActionEventHandler(resources));
        itemFileNamePrefixTextField.setOnKeyReleased(itemNameKeyReleasedEventHandler());
    }

    private EventHandler<ActionEvent> projectPathActionEventHandler() {
        return event -> {
            RunConfig runConfig = mainController.createRunConfigFromUI();
            applicationProperties.updateCurrentRunConfig(runConfig);
            uiLauncher.setApplicationProperties(applicationProperties);
            String configurationName = mainController.getConfigurationName();
            mainController.updateConfigurationNameComboBox(configurationName, configurationName);
            uiLauncher.showProject(mainController.getItemType());
        };
    }

    private EventHandler<ActionEvent> itemPathActionEventHandler(final ResourceBundle resources) {
        return event -> {
            if (mainController.getItemType() == ItemType.STATEMENT) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(Paths.get(".").toFile());
                fileChooser.setTitle(resources.getString("directory.item.statement.title"));
                final Optional<Path> statementFile = Optional.ofNullable(fileChooser.showOpenDialog(uiLauncher.currentWindow()))
                        .map(File::toPath);
                boolean isStatementFileSet = statementFile.map(path -> Files.exists(path) && Files.isRegularFile(path))
                        .orElseGet(() -> false);
                if (isStatementFileSet) {
                    itemPathLabel.setText(statementFile.get().toAbsolutePath().toString());
                    itemPathButton.setText(resources.getString("button.open"));
                    itemPathLabel.getTooltip().setText(statementFile.get().toAbsolutePath().toString());
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(Paths.get(".").toFile());
                directoryChooser.setTitle(resources.getString("directory.item.store"));
                final Optional<Path> itemPathDirectory = Optional.ofNullable(directoryChooser.showDialog(uiLauncher.currentWindow()))
                        .map(File::toPath);
                boolean isDirectorySet = itemPathDirectory.map(path -> Files.exists(path) && Files.isDirectory(path))
                        .orElseGet(() -> false);
                if (isDirectorySet) {
                    itemPathLabel.setText(itemPathDirectory.get().toAbsolutePath().toString());
                    itemPathButton.setText(resources.getString("button.change"));
                    itemPathLabel.getTooltip().setText(itemPathDirectory.get().toAbsolutePath().toString());
                }
            }
        };
    }

    private EventHandler<KeyEvent> itemNameKeyReleasedEventHandler() {
        return event -> {
            if (EnumSet.of(KeyCode.ENTER, KeyCode.TAB).contains(event.getCode())) {
                ignoreItemPrefixNameListener = true;
                itemFileNamePrefixTextField.setText(currentItemFileNamePrefixValue);
                itemFileNamePrefixTextField.positionCaret(currentItemFileNamePrefixValue.length());
                ignoreItemPrefixNameListener = false;
            } else if (KeyCode.BACK_SPACE == event.getCode()) {
                final Optional<Integer> startPosition = intelliSenseService.getSelectedStartPosition(currentItemFileNamePrefixValue);
                if (startPosition.isPresent()) {
                    final int endSelectionPosition = itemFileNamePrefixTextField.getCaretPosition();
                    itemFileNamePrefixTextField.selectRange(startPosition.get(), endSelectionPosition);
                }
            }
        };
    }

    private void setListeners() {
        itemFileNamePrefixTextField.textProperty().addListener(itemFileNameChangeListener());
    }

    private ChangeListener<String> itemFileNameChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (ignoreItemPrefixNameListener) return;

            currentItemFileNamePrefixValue = newValue;
            if (definedPatterns.contains(newValue)) {
                currentItemFileNamePrefixValue = intelliSenseService.getValue(oldValue, newValue);
                ignoreItemPrefixNameListener = true;
                itemFileNamePrefixTextField.setText(currentItemFileNamePrefixValue);
                ignoreItemPrefixNameListener = false;
            }
            itemFileNamePrefixTextField.positionCaret(currentItemFileNamePrefixValue.length());
        };
    }

    void setDisableProjectPathButton(boolean value) {
        projectPathButton.setDisable(value);
    }

    String getProjectPaths() {
        return String.join(",", applicationProperties.projectPaths());
    }

    String getItemPathLabelValue() {
        return itemPathLabel.getText();
    }

    String getItemFileNamePrefix() {
        return itemFileNamePrefixTextField.getText();
    }
}
