package pg.gipter.ui.menu.fileName;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.settings.dto.NameSetting;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

public class NameSettingsController extends AbstractController {

    @FXML
    private TableView<NameTableData> nameSettingsTableView;
    @FXML
    private TextField wordToReplaceTextField;
    @FXML
    private ComboBox<String> replacementComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;

    private Set<NameTableData> patternsToDelete;
    private NameSetting fileNameSetting;

    private final String emptyString = "";

    public NameSettingsController(UILauncher uiLauncher) {
        super(uiLauncher);
        this.fileNameSetting = new NameSetting();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setUpColumns();
        setInitValues();
        setActions();
        setProperties();
    }

    private void setUpColumns() {
        TableColumn<NameTableData, ?> column = nameSettingsTableView.getColumns().get(0);
        TableColumn<NameTableData, String> wordToReplaceColumn = new TableColumn<>();
        wordToReplaceColumn.setText(column.getText());
        wordToReplaceColumn.setPrefWidth(column.getPrefWidth());
        wordToReplaceColumn.setCellValueFactory(new PropertyValueFactory<>("wordToReplace"));
        wordToReplaceColumn.setEditable(false);

        column = nameSettingsTableView.getColumns().get(1);
        TableColumn<NameTableData, String> replacementColumn = new TableColumn<>();
        replacementColumn.setText(column.getText());
        replacementColumn.setPrefWidth(column.getPrefWidth());
        replacementColumn.setCellValueFactory(new PropertyValueFactory<>("replacement"));
        replacementColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        replacementColumn.setEditable(false);

        nameSettingsTableView.getColumns().clear();
        nameSettingsTableView.getColumns().addAll(wordToReplaceColumn, replacementColumn);

        nameSettingsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        nameSettingsTableView.getSelectionModel().selectedItemProperty().addListener(toDeleteChangeListener());
    }

    private ChangeListener<NameTableData> toDeleteChangeListener() {
        return (observable, oldValue, newValue) ->
                patternsToDelete = new LinkedHashSet<>(nameSettingsTableView.getSelectionModel().getSelectedItems());
    }

    private void setInitValues() {
        Optional<NameSetting> fnsOpt = propertiesDao.loadFileNameSetting();
        if (fnsOpt.isPresent()) {
            fileNameSetting = fnsOpt.get();
            setTableViewData();
        }
        replacementComboBox.setValue(emptyString);
        Set<String> comboValues = new LinkedHashSet<>();
        comboValues.add(emptyString);
        comboValues.addAll(EnumSet.allOf(NamePatternValue.class).stream().map(NamePatternValue::name).collect(toCollection(LinkedHashSet::new)));
        replacementComboBox.setItems(FXCollections.observableArrayList(comboValues));
        saveButton.setDisable(nameSettingsTableView.getItems().isEmpty());
        clearButton.setDisable(nameSettingsTableView.getItems().isEmpty());
        removeButton.setDisable(nameSettingsTableView.getItems().isEmpty());
        addButton.setDisable(true);
    }

    private void setActions() {
        removeButton.setOnAction(removeButtonActionEventHandler());
        addButton.setOnAction(addButtonActionEventHandler());
        wordToReplaceTextField.setOnKeyReleased(wordToReplaceKeyReleasedEventHandler());
        clearButton.setOnAction(clearButtonActionEventHandler());
        saveButton.setOnAction(saveButtonActionEventHandler());
    }

    private EventHandler<ActionEvent> saveButtonActionEventHandler() {
        return event -> {
            uiLauncher.executeOutsideUIThread(() -> {
                propertiesDao.saveFileNameSetting(fileNameSetting);
                clearItemFileNamePrefix();
            });
            saveButton.setDisable(fileNameSetting.getNameSettings().isEmpty());
            new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("nameSettings.saved"))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWindowType(WindowType.CONFIRMATION_WINDOW)
                    .withImage(ImageFile.FINGER_UP_PNG)
                    .buildAndDisplayWindow();
        };
    }

    private void clearItemFileNamePrefix() {
        if (fileNameSetting.getNameSettings().isEmpty()) {
            Map<String, Properties> map = propertiesDao.loadAllApplicationProperties();
            if (!map.isEmpty()) {
                for (Properties config : map.values()) {
                    String property = config.getProperty(ArgName.itemFileNamePrefix.name());
                        if (!StringUtils.nullOrEmpty(property)) {
                            int openingBracket = property.indexOf("{", 0);
                            int closingBracket = property.indexOf("}", openingBracket);
                            if (openingBracket >= 0 && closingBracket > 0) {
                                config.remove(ArgName.itemFileNamePrefix.name());
                                propertiesDao.saveRunConfig(config);
                            }
                        }
                }
            }
        }
    }

    private EventHandler<ActionEvent> removeButtonActionEventHandler() {
        return event -> {
            patternsToDelete = new LinkedHashSet<>(nameSettingsTableView.getSelectionModel().getSelectedItems());
            nameSettingsTableView.getItems().removeAll(patternsToDelete);
            nameSettingsTableView.refresh();
            fileNameSetting.removeSettings(patternsToDelete.stream().map(NameTableData::getWordToReplace).collect(toSet()));
            saveButton.setDisable(nameSettingsTableView.getItems().isEmpty());
            clearButton.setDisable(nameSettingsTableView.getItems().isEmpty());
            removeButton.setDisable(nameSettingsTableView.getItems().isEmpty());
        };
    }

    private EventHandler<ActionEvent> addButtonActionEventHandler() {
        return event -> {
            String wordToReplace = wordToReplaceTextField.getText();
            wordToReplace = wordToReplace.startsWith("{") ? wordToReplace : "{" + wordToReplace;
            wordToReplace = wordToReplace.endsWith("}") ? wordToReplace : wordToReplace + "}";
            fileNameSetting.addSetting(wordToReplace, NamePatternValue.valueOf(replacementComboBox.getValue()));
            setTableViewData();
            wordToReplaceTextField.clear();
            replacementComboBox.setValue(emptyString);
            nameSettingsTableView.refresh();
            saveButton.setDisable(false);
            clearButton.setDisable(false);
            removeButton.setDisable(false);
            addButton.setDisable(true);
        };
    }

    private void setTableViewData() {
        List<NameTableData> tableData = this.fileNameSetting.getNameSettings()
                .entrySet()
                .stream()
                .map(entry -> new NameTableData(entry.getKey(), entry.getValue().name()))
                .collect(Collectors.toCollection(LinkedList::new));
        nameSettingsTableView.setItems(FXCollections.observableArrayList(tableData));
    }

    private EventHandler<KeyEvent> wordToReplaceKeyReleasedEventHandler() {
        return event -> addButton.setDisable(
                emptyString.equals(wordToReplaceTextField.getText()) || emptyString.equals(replacementComboBox.getValue())
        );
    }

    private EventHandler<ActionEvent> clearButtonActionEventHandler() {
        return event -> {
            fileNameSetting = new NameSetting();
            nameSettingsTableView.getItems().clear();
            clearButton.setDisable(nameSettingsTableView.getItems().isEmpty());
            removeButton.setDisable(nameSettingsTableView.getItems().isEmpty());
        };
    }

    private void setProperties() {
        replacementComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        addButton.setDisable(
                                emptyString.equals(newValue) || emptyString.equals(wordToReplaceTextField.getText())
                        )
                );


    }

}
