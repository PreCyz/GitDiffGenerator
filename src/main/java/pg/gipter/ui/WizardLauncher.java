package pg.gipter.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Launcher;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;

import java.util.Properties;

public class WizardLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(WizardLauncher.class);
    private Stage primaryStage;
    private UILauncher uiLauncher;

    public WizardLauncher(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public void execute() {
        showLinearWizard();
    }

    private void showLinearWizard() {
        Properties properties = new Properties();
        // define pages to show

        Wizard wizard = new Wizard();
        wizard.setTitle("Gipter Wizard");
        WizardPane page1 = buildPage1(properties);
        WizardPane page2 = buildPage2(properties);
        WizardPane page3 = buildPage3(properties);

        // create wizard
        wizard.setFlow(new Wizard.LinearFlow(page1, page2, page3));

        System.out.println("page1: " + page1);
        System.out.println("page2: " + page2);
        System.out.println("page3: " + page3);

        // show wizard and wait for response
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                System.out.println("Wizard finished, settings: " + wizard.getSettings());
            }
        });
    }

    @NotNull
    private WizardPane buildPage1(Properties properties) {
        // --- page 1
        int row = 0;

        GridPane page1Grid = new GridPane();
        page1Grid.setVgap(10);
        page1Grid.setHgap(10);

        page1Grid.add(new Label("Configuration Name:"), 0, row);
        TextField txConfigurationName = createTextField(ArgName.configurationName.name());
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.registerValidator(txConfigurationName, Validator.createEmptyValidator("Configuration name is required"));
        validationSupport.setErrorDecorationEnabled(true);
//        validationSupport.redecorate();

        page1Grid.add(txConfigurationName, 1, row++);

        page1Grid.add(new Label("Upload type:"), 0, row);
        ComboBox<UploadType> comboBox = createComboBox(ArgName.uploadType.name());
        page1Grid.add(comboBox, 1, row);

        WizardPane page1 = new WizardPane();
        page1.setHeaderText("Please enter configuration details");
        page1.setContent(page1Grid);
        return page1;
    }

    @NotNull
    private WizardPane buildPage2(Properties properties) {
        // --- page 2
        int row = 0;

        WizardPane page2 = new WizardPane() {
            @Override public void onEnteringPage(Wizard wizard) {
                properties.put(ArgName.configurationName.name(), String.valueOf(wizard.getSettings().get(ArgName.configurationName.name())));
                properties.put(ArgName.uploadType.name(), String.valueOf(wizard.getSettings().get(ArgName.uploadType.name())));
            }
        };

        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        pageGrid.add(new Label("Author:"), 0, row);
        TextField author = createTextField(ArgName.author.name());
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.registerValidator(author, Validator.createEmptyValidator("Author is required"));
        validationSupport.setErrorDecorationEnabled(true);
//        validationSupport.redecorate();
        pageGrid.add(author, 1, row++);

        pageGrid.add(new Label("Committer email:"), 0, row);
        TextField committerEmail = createTextField(ArgName.author.name());
        pageGrid.add(committerEmail, 1, row++);

        page2.setHeaderText("Enter the scv details");
        page2.setContent(pageGrid);
        return page2;
    }

    @NotNull
    private WizardPane buildPage3(Properties properties) {
        // --- page 3
        int row = 0;
        WizardPane page3 = new WizardPane() {
            @Override public void onEnteringPage(Wizard wizard) {
                properties.put(ArgName.author.name(), String.valueOf(wizard.getSettings().get(ArgName.author.name())));
                properties.put(ArgName.committerEmail.name(), String.valueOf(wizard.getSettings().get(ArgName.committerEmail.name())));
            }
        };
        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        Label projectLabel = new Label("Project paths");
        Button projectButton = new Button();
        projectButton.setText("Add");
        projectButton.setOnAction(addProjectEventHandler(properties, projectLabel));

        pageGrid.add(projectButton, 0, row);
        pageGrid.add(projectLabel, 1, row++);

        page3.setHeaderText("Project details");
        page3.setContent(pageGrid);

        return page3;
    }

    @NotNull
    private EventHandler<ActionEvent> addProjectEventHandler(Properties properties, Label projectLabel) {
        return event -> {
            String[] args = properties.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .toArray(String[]::new);
            ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            uiLauncher = new UILauncher(primaryStage, applicationProperties);
            if (applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
                uiLauncher.showToolkitProjectsWindow(false);
            } else {
                uiLauncher.showProjectsWindow(false);
            }
            applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            projectLabel.setText(String.join(",", applicationProperties.projectPaths()));
        };
    }

    private TextField createTextField(String id) {
        TextField textField = new TextField();
        textField.setId(id);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }

    private ComboBox<UploadType> createComboBox(String id) {
        ComboBox<UploadType> comboBox = new ComboBox<>();
        comboBox.setId(id);
        comboBox.setValue(UploadType.SIMPLE);
        comboBox.setItems(FXCollections.observableArrayList(UploadType.values()));
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        return comboBox;
    }
}
