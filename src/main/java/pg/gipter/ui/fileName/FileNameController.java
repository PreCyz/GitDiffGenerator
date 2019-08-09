package pg.gipter.ui.fileName;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.jetbrains.annotations.NotNull;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.dto.FileNameSetting;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class FileNameController extends AbstractController {

    @FXML
    private ComboBox<String> firstPercentComboBox;
    @FXML
    private ComboBox<String> secondPercentComboBox;
    @FXML
    private ComboBox<String> thirdPercentComboBox;
    @FXML
    private ComboBox<String> fourthPercentComboBox;
    @FXML
    private ComboBox<String> fifthPercentComboBox;
    @FXML
    private Label resultFileName;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;

    private ApplicationProperties applicationProperties;

    private final String SAMPLE = "how-%1-high-%2-can-%3-you-%4-go-%5-?";

    public FileNameController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setActions();
        setProperties();
    }

    private void setInitValues() {
        Set<String> comboValues = new LinkedHashSet<>();
        comboValues.add("");
        comboValues.addAll(Stream.of(NamePatternValue.values()).map(NamePatternValue::name).collect(toSet()));
        firstPercentComboBox.setItems(FXCollections.observableArrayList(comboValues));
        secondPercentComboBox.setItems(FXCollections.observableArrayList(comboValues));
        thirdPercentComboBox.setItems(FXCollections.observableArrayList(comboValues));
        fourthPercentComboBox.setItems(FXCollections.observableArrayList(comboValues));
        fifthPercentComboBox.setItems(FXCollections.observableArrayList(comboValues));

        Optional<FileNameSetting> fileNameSetting = propertiesHelper.loadFileNameSetting();
        if (fileNameSetting.isPresent()) {
            firstPercentComboBox.setValue(fileNameSetting.get().getFirstPercent().name());
            secondPercentComboBox.setValue(fileNameSetting.get().getSecondPercent().name());
            thirdPercentComboBox.setValue(fileNameSetting.get().getThirdPercent().name());
            fourthPercentComboBox.setValue(fileNameSetting.get().getFourthPercent().name());
            fifthPercentComboBox.setValue(fileNameSetting.get().getFifthPercent().name());
        } else {
            clearComboBoxes();
        }
        resultFileName.setText(calculateSampleName());
    }

    private void clearComboBoxes() {
        firstPercentComboBox.setValue("");
        secondPercentComboBox.setValue("");
        thirdPercentComboBox.setValue("");
        fourthPercentComboBox.setValue("");
        fifthPercentComboBox.setValue("");
    }

    private String calculateSampleName() {
        String result = replacePatternWithValue(SAMPLE, "%1", firstPercentComboBox.getValue());
        result = replacePatternWithValue(result, "%2", secondPercentComboBox.getValue());
        result = replacePatternWithValue(result, "%3", thirdPercentComboBox.getValue());
        result = replacePatternWithValue(result, "%4", fourthPercentComboBox.getValue());
        result = replacePatternWithValue(result, "%5", fifthPercentComboBox.getValue());
        return result;
    }

    private String replacePatternWithValue(String source, String pattern, String value) {
        if (value.isEmpty()) {
            return source.replaceAll(pattern, value);
        }
        return source.replaceAll(pattern, applicationProperties.valueFromPattern(NamePatternValue.valueOf(value)));
    }

    private void setActions() {
        clearButton.setOnAction(clearActionEventHandler());
        saveButton.setOnAction(saveActionEventHandler());
    }

    @NotNull
    private EventHandler<ActionEvent> saveActionEventHandler() {
        return event -> uiLauncher.executeOutsideUIThread(() -> {
            FileNameSetting fileNameSetting = new FileNameSetting();
            if (!firstPercentComboBox.getValue().isEmpty()) {
                fileNameSetting.setFirstPercent(NamePatternValue.valueOf(firstPercentComboBox.getValue()));
            }
            if (!secondPercentComboBox.getValue().isEmpty()) {
                fileNameSetting.setSecondPercent(NamePatternValue.valueOf(secondPercentComboBox.getValue()));
            }
            if (!thirdPercentComboBox.getValue().isEmpty()) {
                fileNameSetting.setThirdPercent(NamePatternValue.valueOf(thirdPercentComboBox.getValue()));
            }
            if (!fourthPercentComboBox.getValue().isEmpty()) {
                fileNameSetting.setFourthPercent(NamePatternValue.valueOf(fourthPercentComboBox.getValue()));
            }
            if (!fifthPercentComboBox.getValue().isEmpty()) {
                fileNameSetting.setFifthPercent(NamePatternValue.valueOf(fifthPercentComboBox.getValue()));
            }
            propertiesHelper.saveFileNameSetting(fileNameSetting);
        });
    }

    private EventHandler<ActionEvent> clearActionEventHandler() {
        return event -> clearComboBoxes();
    }

    private void setProperties() {
        firstPercentComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> resultFileName.setText(calculateSampleName()));
        secondPercentComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> resultFileName.setText(calculateSampleName()));
        thirdPercentComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> resultFileName.setText(calculateSampleName()));
        fourthPercentComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> resultFileName.setText(calculateSampleName()));
        fifthPercentComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> resultFileName.setText(calculateSampleName()));
    }

}
