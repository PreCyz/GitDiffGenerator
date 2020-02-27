package pg.gipter.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producer.command.ItemType;
import pg.gipter.launcher.Launcher;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.ResourceUtils;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

public class WizardLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(WizardLauncher.class);
    private Stage primaryStage;
    private UILauncher uiLauncher;
    private Properties wizardProperties;
    private String lastChosenConfiguration;
    private ApplicationProperties applicationProperties;

    static final String projectLabelPropertyName = "projectLabelProperty";

    public WizardLauncher(Stage stage) {
        this(stage, ArgName.configurationName.defaultValue());
    }

    public WizardLauncher(Stage stage, String lastChosenConfiguration) {
        this.primaryStage = stage;
        this.lastChosenConfiguration = lastChosenConfiguration;
        wizardProperties = new Properties();
        applicationProperties = ApplicationPropertiesFactory.getInstance(new String[]{});
    }

    @Override
    public void execute() {
        short step = 1;
        Wizard wizard = new Wizard();
        wizard.setTitle(BundleUtils.getMsg("wizard.title"));
        WizardPane welcomePage = buildWelcomePage(step++);
        WizardPane configurationPage = buildConfigurationPage(step++);
        WizardPane toolkitCredentialsPage = buildToolkitCredentialsPage(step++);
        WizardPane committerPage = buildCommitterPage(step++);
        WizardPane projectPage = buildProjectPage(step++);
        WizardPane finishPage = buildFinishPage(step);

        wizard.setFlow(buildFlow(welcomePage, configurationPage, toolkitCredentialsPage, committerPage, projectPage, finishPage));

        wizard.showAndWait().ifPresent(result -> {
            ApplicationProperties instance = ApplicationPropertiesFactory.getInstance(new String[]{});
            if (result == ButtonType.FINISH) {
                logger.info("Wizard finished.");
                String[] args = wizardProperties.entrySet().stream()
                        .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                        .toArray(String[]::new);
                applicationProperties.updateCurrentRunConfig(RunConfig.valueFrom(args));
                applicationProperties.updateToolkitConfig(ToolkitConfig.valueFrom(args));
                applicationProperties.updateApplicationConfig(ApplicationConfig.valueFrom(args));
                applicationProperties.save();
            } else if (result == ButtonType.CANCEL) {
                final String configurationName = wizardProperties.getProperty(ArgName.configurationName.name());
                applicationProperties.getRunConfig(configurationName)
                        .ifPresent(runConfig -> applicationProperties.removeConfig(configurationName));
                logger.info("Wizard canceled.");
                applicationProperties = ApplicationPropertiesFactory.getInstance(new String[]{});
                if (!StringUtils.nullOrEmpty(lastChosenConfiguration)) {
                    Optional<RunConfig> lastConfiguration = applicationProperties.getRunConfig(lastChosenConfiguration);
                    if (lastConfiguration.isPresent()) {
                        instance = ApplicationPropertiesFactory.getInstance(lastConfiguration.get().toArgumentArray());
                    }
                }
            }
            uiLauncher = new UILauncher(primaryStage, instance);
            uiLauncher.execute();
        });
    }

    private WizardPane buildWelcomePage(int step) {
        WizardPane wizardPane = new WizardPane();
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        ImageView imageView = ResourceUtils.getImgResource(ImageFile.MINION_FART_GIF.fileUrl())
                .map(url -> new ImageView(new Image(url.toString())))
                .orElseGet(ImageView::new);
        imageView.setId("imageView");
        imageView.setX(60);
        imageView.setY(10);
        anchorPane.getChildren().addAll(imageView);

        wizardPane.setContent(anchorPane);
        wizardPane.setHeaderText(BundleUtils.getMsg("wizard.welcome.text", String.valueOf(step)));
        return wizardPane;
    }

    private WizardPane buildConfigurationPage(short step) {
        int row = 0;

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label(BundleUtils.getMsg("main.configurationName")), 0, row);
        TextField configurationName = createTextField(ArgName.configurationName.name());
        gridPane.add(configurationName, 1, row++);

        gridPane.add(new Label(BundleUtils.getMsg("csv.panel.itemType")), 0, row);
        ComboBox<ItemType> comboBox = createUploadTypeComboBox(ArgName.itemType.name());
        gridPane.add(comboBox, 1, row);

        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizardProperties.putIfAbsent(ArgName.itemType.name(), ItemType.SIMPLE.name());
            }

            @Override
            public void onExitingPage(Wizard wizard) {
                if (StringUtils.nullOrEmpty(getValue(wizard, ArgName.configurationName))) {
                    wizard.getSettings().put(ArgName.configurationName.name(), "wizard-config");
                }
                if (!StringUtils.nullOrEmpty(lastChosenConfiguration)) {
                    ApplicationProperties applicationProperties = propertiesWithCredentials();
                    if (applicationProperties.isToolkitCredentialsSet()) {
                        wizard.getSettings().put(ArgName.toolkitUsername.name(), applicationProperties.toolkitUsername());
                        wizard.getSettings().put(ArgName.toolkitPassword.name(), applicationProperties.toolkitPassword());
                    }
                }
                String uploadType = wizardProperties.getProperty(ArgName.itemType.name());
                if (StringUtils.nullOrEmpty(uploadType)) {
                    wizardProperties.put(ArgName.itemType.name(), comboBox.getValue().name());
                }
            }
        };
        wizardPane.setHeaderText(BundleUtils.getMsg("wizard.configuration.details") + " (" + step + "/6)");
        wizardPane.setContent(gridPane);
        return wizardPane;
    }

    private ApplicationProperties propertiesWithCredentials() {
        if (!applicationProperties.isToolkitConfigExists()) {
            applicationProperties.updateToolkitConfig(new ToolkitConfig());
        }
        return applicationProperties;
    }

    private WizardPane buildToolkitCredentialsPage(short step) {
        int row = 0;

        WizardPane page = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
            }
        };

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label(BundleUtils.getMsg("toolkit.panel.username")), 0, row);
        TextField username = createTextField(ArgName.toolkitUsername.name());
        gridPane.add(username, 1, row++);

        gridPane.add(new Label(BundleUtils.getMsg("toolkit.panel.password")), 0, row);
        PasswordField password = createPasswordField(ArgName.toolkitPassword.name());
        gridPane.add(password, 1, row);

        page.setHeaderText(BundleUtils.getMsg("wizard.toolkit.credentials") + " (" + step + "/6)");
        page.setContent(gridPane);
        return page;
    }

    private void updateProperties(Wizard wizard, Properties properties) {
        properties.put(ArgName.configurationName.name(), getValue(wizard, ArgName.configurationName));
        properties.put(ArgName.itemType.name(), getValue(wizard, ArgName.itemType));
        properties.put(ArgName.toolkitUsername.name(), getValue(wizard, ArgName.toolkitUsername).toUpperCase());
        properties.put(ArgName.toolkitPassword.name(), getValue(wizard, ArgName.toolkitPassword));
        properties.put(ArgName.author.name(), getValue(wizard, ArgName.author));
        properties.put(ArgName.committerEmail.name(), getValue(wizard, ArgName.committerEmail));
        properties.put(ArgName.itemPath.name(), getValue(wizard, ArgName.itemPath));
    }

    private String getValue(Wizard wizard, ArgName argName) {
        return Optional.ofNullable(wizard.getSettings().get(argName.name())).map(String::valueOf).orElseGet(() -> "");
    }

    private TextField createTextField(String id) {
        TextField textField = new TextField();
        textField.setId(id);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }

    private PasswordField createPasswordField(String id) {
        PasswordField passwordField = new PasswordField();
        passwordField.setId(id);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        return passwordField;
    }

    private ComboBox<ItemType> createUploadTypeComboBox(String id) {
        ComboBox<ItemType> comboBox = new ComboBox<>();
        comboBox.setId(id);
        comboBox.setValue(ItemType.SIMPLE);
        comboBox.setItems(FXCollections.observableArrayList(ItemType.values()));
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            wizardProperties.put(ArgName.itemType.name(), newValue.name());
        });
        Tooltip tooltip = new Tooltip(BundleUtils.getMsg("wizard.uploadType.description"));
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.setFont(Font.font("Courier New", 14));
        comboBox.setTooltip(tooltip);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        return comboBox;
    }

    private WizardPane buildCommitterPage(short step) {
        int row = 0;

        WizardPane page2 = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
            }
        };

        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        pageGrid.add(new Label(BundleUtils.getMsg("csv.panel.authors")), 0, row);
        TextField author = createTextField(ArgName.author.name());
        pageGrid.add(author, 1, row++);

        pageGrid.add(new Label(BundleUtils.getMsg("csv.panel.committerEmail")), 0, row);
        TextField committerEmail = createTextField(ArgName.committerEmail.name());
        pageGrid.add(committerEmail, 1, row);

        page2.setHeaderText(BundleUtils.getMsg("wizard.scv.details") + " (" + step + "/6)");
        page2.setContent(pageGrid);
        return page2;
    }

    private WizardPane buildProjectPage(short step) {
        int row = 0;
        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        StringProperty itemPathStringProperty = new SimpleStringProperty();
        itemPathStringProperty.setValue(BundleUtils.getMsg("wizard.item.location"));
        Label itemPathLabel = new Label();
        itemPathLabel.textProperty().unbind();
        itemPathLabel.textProperty().bind(itemPathStringProperty);

        Button itemButton = new Button(BundleUtils.getMsg("button.add"));
        itemButton.setOnAction(addItemEventHandler(itemPathStringProperty));
        pageGrid.add(itemButton, 0, row);
        pageGrid.add(itemPathLabel, 1, row++);

        Label projectLabel = new Label();
        projectLabel.textProperty().unbind();
        StringProperty projectLabelStringProperty = new SimpleStringProperty();
        projectLabelStringProperty.setValue(BundleUtils.getMsg("wizard.project.choose"));
        projectLabel.textProperty().bind(projectLabelStringProperty);
        wizardProperties.put(projectLabelPropertyName, projectLabelStringProperty);

        Button projectButton = new Button(BundleUtils.getMsg("button.add"));
        projectButton.setOnAction(addProjectEventHandler());
        pageGrid.add(projectButton, 0, row);
        pageGrid.add(projectLabel, 1, row);

        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
                itemPathStringProperty.setValue(BundleUtils.getMsg("wizard.item.location"));
                projectLabel.setVisible(true);
                projectButton.setVisible(true);
                if (ItemType.valueFor(getValue(wizard, ArgName.itemType)) == ItemType.STATEMENT) {
                    itemPathStringProperty.setValue(BundleUtils.getMsg("wizard.item.statement.location"));
                    projectLabel.setVisible(false);
                    projectButton.setVisible(false);
                }

                String projectPath = wizardProperties.getProperty(ArgName.projectPath.name());
                if (!StringUtils.nullOrEmpty(projectPath)) {
                    StringProperty textProperty = (StringProperty) wizardProperties.get(projectLabelPropertyName);
                    textProperty.setValue(StringUtils.trimTo50(projectPath));
                }

                String itemPath = wizardProperties.getProperty(ArgName.itemPath.name());
                if (!StringUtils.nullOrEmpty(itemPath)) {
                    itemPathStringProperty.setValue(StringUtils.trimTo50(itemPath));
                }
            }

            @Override
            public void onExitingPage(Wizard wizard) {
                wizard.getSettings().put(ArgName.itemPath.name(), itemPathStringProperty.getValue());
            }
        };
        wizardPane.setHeaderText(BundleUtils.getMsg("paths.panel.title") + " (" + step + "/6)");
        wizardPane.setContent(pageGrid);

        return wizardPane;
    }

    private EventHandler<ActionEvent> addProjectEventHandler() {
        return event -> {
            String[] args = wizardProperties.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .toArray(String[]::new);
            RunConfig runConfig = RunConfig.valueFrom(args);
            ToolkitConfig toolkitConfig = ToolkitConfig.valueFrom(args);
            applicationProperties.updateCurrentRunConfig(runConfig);
            applicationProperties.updateToolkitConfig(toolkitConfig);
            applicationProperties.save();

            uiLauncher = new UILauncher(primaryStage, applicationProperties);
            if (applicationProperties.itemType() == ItemType.TOOLKIT_DOCS) {
                uiLauncher.showToolkitProjectsWindow(wizardProperties);
            } else {
                uiLauncher.showProjectsWindow(wizardProperties);
            }
        };
    }

    private EventHandler<ActionEvent> addItemEventHandler(StringProperty itemPathStringProperty) {
        return event -> {
            ItemType uploadType = ItemType.valueFor(wizardProperties.getProperty(ArgName.itemType.name()));
            if (uploadType == ItemType.STATEMENT) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle(BundleUtils.getMsg("directory.item.statement.title"));
                File statementFile = fileChooser.showOpenDialog(primaryStage);
                if (statementFile != null && statementFile.exists() && statementFile.isFile()) {
                    itemPathStringProperty.setValue(statementFile.getAbsolutePath());
                    wizardProperties.put(ArgName.itemPath.name(), statementFile.getAbsolutePath());
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                directoryChooser.setTitle(BundleUtils.getMsg("directory.item.store"));
                File itemPathDirectory = directoryChooser.showDialog(primaryStage);
                if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                    itemPathStringProperty.setValue(itemPathDirectory.getAbsolutePath());
                    wizardProperties.put(ArgName.itemPath.name(), itemPathDirectory.getAbsolutePath());
                }
            }
        };
    }

    private WizardPane buildFinishPage(short step) {
        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
            }

            @Override
            public void onExitingPage(Wizard wizard) {
                String[] args = wizardProperties.entrySet().stream()
                        .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                        .toArray(String[]::new);
                RunConfig runConfig = RunConfig.valueFrom(args);
                ToolkitConfig toolkitConfig = ToolkitConfig.valueFrom(args);
                applicationProperties.updateCurrentRunConfig(runConfig);
                applicationProperties.updateToolkitConfig(toolkitConfig);
                applicationProperties.save();
            }
        };
        wizardPane.setHeaderText(BundleUtils.getMsg("wizard.finish.text", String.valueOf(step)));

        AnchorPane anchorPane = new AnchorPane();
        ImageView imageView = ResourceUtils.getImgResource(ImageFile.MINION_APPLAUSE_GIF.fileUrl())
                .map(url -> new ImageView(new Image(url.toString())))
                .orElseGet(ImageView::new);
        imageView.setId("imageView");
        imageView.setX(20);
        imageView.setY(10);
        anchorPane.getChildren().addAll(imageView);
        wizardPane.setContent(anchorPane);
        return wizardPane;
    }

    private Wizard.Flow buildFlow(WizardPane welcomePage, WizardPane configurationPage, WizardPane toolkitCredentialsPage,
                                  WizardPane committerPage, WizardPane projectPage, WizardPane finishPage) {
        return new Wizard.Flow() {

            @Override
            public Optional<WizardPane> advance(WizardPane currentPage) {
                return Optional.of(getNext(currentPage));
            }

            @Override
            public boolean canAdvance(WizardPane currentPage) {
                return currentPage != finishPage;
            }

            private WizardPane getNext(WizardPane currentPage) {
                if (currentPage == null) {
                    return welcomePage;
                } else if (currentPage == welcomePage) {
                    return configurationPage;
                } else if (currentPage == configurationPage) {
                    if (StringUtils.nullOrEmpty(lastChosenConfiguration)) {
                        return toolkitCredentialsPage;
                    }
                    ApplicationProperties applicationProperties = propertiesWithCredentials();
                    if (applicationProperties.isToolkitCredentialsSet()) {
                        return flowAdvanceLogic();
                    }
                    return toolkitCredentialsPage;
                } else if (currentPage == toolkitCredentialsPage) {
                    return flowAdvanceLogic();
                } else if (currentPage == committerPage) {
                    return projectPage;
                } else {
                    return finishPage;
                }
            }

            private WizardPane flowAdvanceLogic() {
                String property = wizardProperties.getProperty(ArgName.itemType.name());
                if (StringUtils.nullOrEmpty(property)) {
                    return committerPage;
                }
                switch (ItemType.valueFor(property)) {
                    case TOOLKIT_DOCS:
                    case STATEMENT:
                        return projectPage;
                    default:
                        return committerPage;
                }
            }
        };
    }
}
