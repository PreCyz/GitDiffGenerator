package pg.gipter.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.launchers.Launcher;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.ResourceUtils;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;

public class WizardLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(WizardLauncher.class);
    private final Stage primaryStage;
    private final Properties wizardProperties;
    private final String lastChosenConfiguration;
    private UILauncher uiLauncher;
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
                saveConfiguration();
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

        gridPane.add(new Label(BundleUtils.getMsg("vcs.panel.itemType")), 0, row);
        ComboBox<ItemType> comboBox = createUploadTypeComboBox(ArgName.itemType.name());
        gridPane.add(comboBox, 1, row);

        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizardProperties.putIfAbsent(ArgName.itemType.name(), ItemType.SIMPLE.name());
            }

            @Override
            public void onExitingPage(Wizard wizard) {
                if (StringUtils.nullOrEmpty(getValue(wizard, ArgName.configurationName.name()))) {
                    wizard.getSettings().put(ArgName.configurationName.name(), "wizard-config");
                }
                if (!StringUtils.nullOrEmpty(lastChosenConfiguration)) {
                    ApplicationProperties applicationProperties = propertiesWithCredentials();
                    if (applicationProperties.isToolkitCredentialsSet()) {
                        wizard.getSettings().put(ArgName.toolkitUsername.name(), applicationProperties.toolkitUsername());
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
        WizardPane page = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
            }
        };

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label(BundleUtils.getMsg("toolkit.panel.username")), 0, 0);
        TextField username = createTextField(ArgName.toolkitUsername.name());
        username.setText(ArgName.toolkitUsername.defaultValue());
        gridPane.add(username, 1, 0);

        page.setHeaderText(BundleUtils.getMsg("wizard.toolkit.credentials") + " (" + step + "/6)");
        page.setContent(gridPane);
        return page;
    }

    private void updateProperties(Wizard wizard, Properties properties) {
        properties.put(ArgName.configurationName.name(), getValue(wizard, ArgName.configurationName.name()));
        properties.put(ArgName.itemType.name(), getValue(wizard, ArgName.itemType.name()));
        properties.put(ArgName.toolkitUsername.name(), getValue(wizard, ArgName.toolkitUsername.name()).toUpperCase());
        properties.put(ArgName.author.name(), getValue(wizard, ArgName.author.name()));
        properties.put(ArgName.committerEmail.name(), getValue(wizard, ArgName.committerEmail.name()));
        properties.put(ArgName.itemPath.name(), getValue(wizard, ArgName.itemPath.name()));
        @SuppressWarnings("unchecked")
        LinkedHashSet<SharePointConfig> sharePointConfigs =
                Optional.ofNullable(wizard.getSettings().get(SharePointConfig.SHARE_POINT_CONFIGS))
                        .map(value -> (LinkedHashSet<SharePointConfig>) value)
                        .orElseGet(LinkedHashSet::new);
        if (!sharePointConfigs.isEmpty()) {
            properties.put(SharePointConfig.SHARE_POINT_CONFIGS, sharePointConfigs);
        }
    }

    private String getValue(Wizard wizard, String argName) {
        return Optional.ofNullable(wizard.getSettings().get(argName)).map(String::valueOf).orElseGet(() -> "");
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
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                wizardProperties.put(ArgName.itemType.name(), newValue.name())
        );
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

        pageGrid.add(new Label(BundleUtils.getMsg("vcs.panel.authors")), 0, row);
        TextField author = createTextField(ArgName.author.name());
        pageGrid.add(author, 1, row++);

        pageGrid.add(new Label(BundleUtils.getMsg("vcs.panel.committerEmail")), 0, row);
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
        itemPathLabel.setOnMouseClicked(labelAddItemEventHandler(itemPathStringProperty));
        itemPathLabel.textProperty().unbind();
        itemPathLabel.textProperty().bind(itemPathStringProperty);

        Button itemButton = new Button(BundleUtils.getMsg("button.add"));
        itemButton.setOnAction(addItemEventHandler(itemPathStringProperty));
        pageGrid.add(itemButton, 0, row);
        pageGrid.add(itemPathLabel, 1, row++);

        Label projectLabel = new Label();
        projectLabel.setOnMouseClicked(labelAddProjectEventHandler());
        projectLabel.textProperty().unbind();
        StringProperty projectLabelStringProperty = new SimpleStringProperty();
        projectLabelStringProperty.setValue(BundleUtils.getMsg("wizard.project.choose"));
        projectLabel.textProperty().bind(projectLabelStringProperty);
        wizardProperties.put(projectLabelPropertyName, projectLabelStringProperty);

        Button projectButton = new Button(BundleUtils.getMsg("button.add"));
        projectButton.setOnAction(addProjectEventHandler());
        pageGrid.add(projectButton, 0, row);
        pageGrid.add(projectLabel, 1, row);

        wizardProperties.put(SharePointConfig.SHARE_POINT_CONFIGS, new LinkedHashSet<>());

        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
                itemPathStringProperty.setValue(BundleUtils.getMsg("wizard.item.location"));
                projectLabel.setVisible(true);
                projectButton.setVisible(true);
                if (ItemType.valueFor(getValue(wizard, ArgName.itemType.name())) == ItemType.STATEMENT) {
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
                wizard.getSettings().put(ArgName.projectPath.name(), projectLabelStringProperty.getValue());
                wizard.getSettings().put(SharePointConfig.SHARE_POINT_CONFIGS, wizardProperties.get(SharePointConfig.SHARE_POINT_CONFIGS));
            }
        };
        wizardPane.setHeaderText(BundleUtils.getMsg("paths.panel.title") + " (" + step + "/6)");
        wizardPane.setContent(pageGrid);

        return wizardPane;
    }

    private EventHandler<MouseEvent> labelAddProjectEventHandler() {
        return event -> addProjects();
    }

    private EventHandler<ActionEvent> addProjectEventHandler() {
        return event -> addProjects();
    }

    private void addProjects() {
        saveConfiguration();
        uiLauncher = new UILauncher(primaryStage, applicationProperties);
        uiLauncher.showProject(applicationProperties.itemType(), wizardProperties);
    }

    public void saveConfiguration() {
        String[] args = wizardProperties.entrySet().stream()
                .filter(entry -> !SharePointConfig.SHARE_POINT_CONFIGS.equals(entry.getKey()))
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .toArray(String[]::new);
        RunConfig runConfig = RunConfig.valueFrom(args);

        applicationProperties.updateCurrentRunConfig(runConfig);
        applicationProperties.updateToolkitConfig(ToolkitConfig.valueFrom(args));
        applicationProperties.updateApplicationConfig(ApplicationConfig.valueFrom(args));
        applicationProperties.save();
    }

    private EventHandler<MouseEvent> labelAddItemEventHandler(StringProperty itemPathStringProperty) {
        return event -> addItemPath(itemPathStringProperty);
    }

    private EventHandler<ActionEvent> addItemEventHandler(StringProperty itemPathStringProperty) {
        return event -> addItemPath(itemPathStringProperty);
    }

    private void addItemPath(StringProperty itemPathStringProperty) {
        ItemType uploadType = ItemType.valueFor(wizardProperties.getProperty(ArgName.itemType.name()));
        Path currentDirectory = Paths.get(".");
        if (uploadType == ItemType.STATEMENT) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(currentDirectory.toFile());
            fileChooser.setTitle(BundleUtils.getMsg("directory.item.statement.title"));
            final Optional<Path> statementPath = Optional.ofNullable(fileChooser.showOpenDialog(primaryStage))
                    .map(File::toPath);
            boolean isStatementFileSet = statementPath.map(path -> Files.exists(path) && Files.isRegularFile(path))
                    .orElseGet(() -> false);
            if (isStatementFileSet) {
                itemPathStringProperty.setValue(statementPath.get().toAbsolutePath().toString());
                wizardProperties.put(ArgName.itemPath.name(), statementPath.get().toAbsolutePath().toString());
            }
        } else {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(currentDirectory.toFile());
            directoryChooser.setTitle(BundleUtils.getMsg("directory.item.store"));
            final Optional<Path> directoryPath = Optional.ofNullable(directoryChooser.showDialog(primaryStage))
                    .map(File::toPath);
            boolean isItemPathDirectorySet = directoryPath.map(path -> Files.exists(path) && Files.isDirectory(path))
                    .orElseGet(() -> false);
            if (isItemPathDirectorySet) {
                itemPathStringProperty.setValue(directoryPath.get().toAbsolutePath().toString());
                wizardProperties.put(ArgName.itemPath.name(), directoryPath.get().toAbsolutePath().toString());
            }
        }
    }

    private WizardPane buildFinishPage(short step) {
        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
            }

            @Override
            public void onExitingPage(Wizard wizard) {
                saveConfiguration();
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
                    ApplicationProperties applicationProperties = propertiesWithCredentials();
                    if (applicationProperties.isToolkitCredentialsSet()) {
                        return flowAdvancedLogic();
                    }
                    return toolkitCredentialsPage;
                } else if (currentPage == toolkitCredentialsPage) {
                    return flowAdvancedLogic();
                } else if (currentPage == committerPage) {
                    return projectPage;
                } else {
                    return finishPage;
                }
            }

            private WizardPane flowAdvancedLogic() {
                String property = wizardProperties.getProperty(ArgName.itemType.name());
                if (StringUtils.nullOrEmpty(property)) {
                    return committerPage;
                }
                switch (ItemType.valueFor(property)) {
                    case TOOLKIT_DOCS:
                    case STATEMENT:
                    default:
                        return committerPage;
                }
            }
        };
    }
}
