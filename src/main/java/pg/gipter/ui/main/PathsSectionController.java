package pg.gipter.ui.main;

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
import pg.gipter.core.model.NamePatternValue;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

class PathsSectionController extends AbstractController {

    private Label projectPathLabel;
    private Label itemPathLabel;
    private TextField itemFileNamePrefixTextField;
    private Button projectPathButton;
    private Button itemPathButton;

    private final MainController mainController;

    private final Set<String> definedPatterns;
    private String currentItemName = "";
    private String inteliSense = "";
    private boolean useInteliSense = false;

    PathsSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties, MainController mainController) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.mainController = mainController;
        this.definedPatterns = EnumSet.allOf(NamePatternValue.class)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Object> controlsMap) {
        super.initialize(location, resources);
        projectPathLabel = (Label)controlsMap.get("projectPathLabel");
        itemPathLabel = (Label)controlsMap.get("itemPathLabel");
        itemFileNamePrefixTextField = (TextField)controlsMap.get("itemFileNamePrefixTextField");
        projectPathButton = (Button)controlsMap.get("projectPathButton");
        itemPathButton = (Button)controlsMap.get("itemPathButton");
        setInitValues();
        setProperties(resources);
        setActions(resources);
        setListeners();
    }

    private void setInitValues() {
        projectPathLabel.setText(String.join(",", applicationProperties.projectPaths()));
        String itemFileName = Paths.get(applicationProperties.itemPath()).getFileName().toString();
        String itemPath = applicationProperties.itemPath()
                .substring(0, applicationProperties.itemPath().indexOf(itemFileName) - 1);
        itemPathLabel.setText(itemPath);
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
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle(resources.getString("directory.item.statement.title"));
                File statementFile = fileChooser.showOpenDialog(uiLauncher.currentWindow());
                if (statementFile != null && statementFile.exists() && statementFile.isFile()) {
                    itemPathLabel.setText(statementFile.getAbsolutePath());
                    itemPathButton.setText(resources.getString("button.open"));
                    itemPathLabel.getTooltip().setText(statementFile.getAbsolutePath());
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                directoryChooser.setTitle(resources.getString("directory.item.store"));
                File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
                if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                    itemPathLabel.setText(itemPathDirectory.getAbsolutePath());
                    itemPathButton.setText(resources.getString("button.change"));
                    itemPathLabel.getTooltip().setText(itemPathDirectory.getAbsolutePath());
                }
            }
        };
    }

    private EventHandler<KeyEvent> itemNameKeyReleasedEventHandler() {
        return event -> {
            if (event.getCode() == KeyCode.ENTER) {
                itemFileNamePrefixTextField.setText(currentItemName);
                itemFileNamePrefixTextField.positionCaret(currentItemName.length());
            }
        };
    }

    private void setListeners() {
        itemFileNamePrefixTextField.textProperty().addListener(itemFileNameChangeListener());
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

    void setDisableProjectPathButton(boolean value) {
        projectPathButton.setDisable(value);
    }

    String getProjectPathLabelValue() {
        return projectPathLabel.getText();
    }

    String getItemPathLabelValue() {
        return itemPathLabel.getText();
    }

    String getItemFileNamePrefix() {
        return itemFileNamePrefixTextField.getText();
    }
}
