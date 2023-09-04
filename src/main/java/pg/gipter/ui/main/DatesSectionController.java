package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.ToolkitService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static pg.gipter.core.ApplicationProperties.yyyy_MM_dd;

public class DatesSectionController extends AbstractController {

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private CheckBox useLastItemDateCheckbox;


    public DatesSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Control> controlsMap) {
        super.initialize(location, resources);
        startDatePicker = (DatePicker) controlsMap.get("startDatePicker");
        endDatePicker = (DatePicker) controlsMap.get("endDatePicker");
        useLastItemDateCheckbox = (CheckBox) controlsMap.get("useLastItemDateCheckbox");

        setInitValues();
        setProperties();
        setListeners();
    }

    private void setInitValues() {
        LocalDate now = LocalDate.now();
        LocalDate initStartDate = now.minusDays(applicationProperties.periodInDays());
        startDatePicker.setValue(initStartDate);
        if (!initStartDate.isEqual(applicationProperties.startDate())) {
            startDatePicker.setValue(applicationProperties.startDate());
        }
        endDatePicker.setValue(now);
        if (!now.isEqual(applicationProperties.endDate())) {
            endDatePicker.setValue(applicationProperties.endDate());
        }

        setLastItemSubmissionDate();
    }

    void setLastItemSubmissionDate() {
        uiLauncher.executeOutsideUIThread(() -> {
            if (uiLauncher.getLastItemSubmissionDate() == null) {
                Optional<String> userId = new ToolkitService(applicationProperties).getUserId();
                Optional<String> submissionDate = new ToolkitService(applicationProperties).lastItemModifiedDate(userId.orElseGet(() -> ""));
                if (submissionDate.isPresent()) {
                    uiLauncher.setLastItemSubmissionDate(LocalDateTime.parse(submissionDate.get(), DateTimeFormatter.ISO_DATE_TIME));
                    Platform.runLater(() -> {
                        useLastItemDateCheckbox.setDisable(uiLauncher.getLastItemSubmissionDate() == null);
                        useLastItemDateCheckbox.setText(BundleUtils.getMsg(
                                "main.lastUploadDate",
                                uiLauncher.getLastItemSubmissionDate().format(DateTimeFormatter.ISO_DATE)
                        ));
                    });
                } else {
                    uiLauncher.setLastItemSubmissionDate(null);
                    Platform.runLater(() -> {
                        useLastItemDateCheckbox.setDisable(uiLauncher.getLastItemSubmissionDate() == null);
                        useLastItemDateCheckbox.setText(BundleUtils.getMsg("main.lastUploadDate.unavailable"));
                    });
                }
            } else {
                Platform.runLater(() -> {
                    useLastItemDateCheckbox.setDisable(uiLauncher.getLastItemSubmissionDate() == null);
                    useLastItemDateCheckbox.setText(BundleUtils.getMsg(
                            "main.lastUploadDate",
                            uiLauncher.getLastItemSubmissionDate().format(DateTimeFormatter.ISO_DATE)
                    ));
                });
            }
        });
    }

    private void setProperties() {
        startDatePicker.setConverter(dateConverter());
        endDatePicker.setConverter(dateConverter());
    }

    private StringConverter<LocalDate> dateConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate object) {
                return object.format(yyyy_MM_dd);
            }

            @Override
            public LocalDate fromString(String string) {
                return LocalDate.parse(string, yyyy_MM_dd);
            }
        };
    }

    void deselectUseLastItemDate() {
        if (useLastItemDateCheckbox.isSelected()) {
            useLastItemDateCheckbox.setSelected(false);
        }
    }

    private void setListeners() {
        useLastItemDateCheckbox.selectedProperty().addListener(useListItemCheckBoxListener());
    }

    private ChangeListener<Boolean> useListItemCheckBoxListener() {
        return (observable, oldValue, newValue) -> {
            startDatePicker.setDisable(newValue);
            if (newValue) {
                startDatePicker.setValue(uiLauncher.getLastItemSubmissionDate().toLocalDate());
            } else {
                startDatePicker.setValue(LocalDate.now().minusDays(applicationProperties.periodInDays()));
            }
        };
    }

    void disableEndDatePicker(ItemType itemType) {
        endDatePicker.setDisable(ItemType.isDocsRelated(itemType));
    }

    void setEndDatePicker(LocalDate localDate) {
        endDatePicker.setValue(localDate);
    }

    LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    LocalDate getEndDate() {
        return endDatePicker.getValue();
    }
}
