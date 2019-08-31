package pg.gipter.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.launcher.Launcher;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
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
    private PropertiesDao propertiesDao;
    private Properties wizardProperties;
    private String lastChosenConfiguration;

    public WizardLauncher(Stage stage) {
        this(stage, "");
    }

    public WizardLauncher(Stage stage, String lastChosenConfiguration) {
        this.primaryStage = stage;
        this.lastChosenConfiguration = lastChosenConfiguration;
        propertiesDao = DaoFactory.getPropertiesDao();
        wizardProperties = new Properties();
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
                String[] args = propertiesDao.loadArgumentArray(wizardProperties.getProperty(ArgName.configurationName.name()));
                instance = ApplicationPropertiesFactory.getInstance(args);
            } else if (result == ButtonType.CANCEL) {
                propertiesDao.loadApplicationProperties(wizardProperties.getProperty(ArgName.configurationName.name()))
                        .ifPresent(props -> propertiesDao.removeConfig(wizardProperties.getProperty(ArgName.configurationName.name())));
                logger.info("Wizard canceled.");
                instance = ApplicationPropertiesFactory.getInstance(new String[]{});
                if (!StringUtils.nullOrEmpty(lastChosenConfiguration)) {
                    Optional<Properties> lastConfiguration = propertiesDao.loadApplicationProperties(lastChosenConfiguration);
                    if (lastConfiguration.isPresent()) {
                        instance = ApplicationPropertiesFactory.getInstance(propertiesDao.loadArgumentArray(lastChosenConfiguration));
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

        gridPane.add(new Label(BundleUtils.getMsg("csv.panel.uploadType")), 0, row);
        ComboBox<UploadType> comboBox = createUploadTypeComboBox(ArgName.uploadType.name());
        gridPane.add(comboBox, 1, row);

        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizardProperties.putIfAbsent(ArgName.uploadType.name(), UploadType.SIMPLE.name());
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
                String uploadType = wizardProperties.getProperty(ArgName.uploadType.name());
                if (StringUtils.nullOrEmpty(uploadType)) {
                    wizardProperties.put(ArgName.uploadType.name(), comboBox.getValue().name());
                }
            }
        };
        wizardPane.setHeaderText(BundleUtils.getMsg("wizard.configuration.details") + " (" + step + "/6)");
        wizardPane.setContent(gridPane);
        return wizardPane;
    }

    private ApplicationProperties propertiesWithCredentials() {
        Properties properties = propertiesDao.loadToolkitCredentials();
        String[] args = properties.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
        return ApplicationPropertiesFactory.getInstance(args);
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
        properties.put(ArgName.uploadType.name(), getValue(wizard, ArgName.uploadType));
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

    private ComboBox<UploadType> createUploadTypeComboBox(String id) {
        ComboBox<UploadType> comboBox = new ComboBox<>();
        comboBox.setId(id);
        comboBox.setValue(UploadType.SIMPLE);
        comboBox.setItems(FXCollections.observableArrayList(UploadType.values()));
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            wizardProperties.put(ArgName.uploadType.name(), newValue.name());
        });
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

        TextField itemPathField = createTextField(ArgName.itemPath.name());
        itemPathField.setDisable(true);
        itemPathField.setText(BundleUtils.getMsg("wizard.item.location"));
        Button itemButton = new Button(BundleUtils.getMsg("button.add"));
        itemButton.setOnAction(addItemEventHandler(itemPathField));
        pageGrid.add(itemButton, 0, row);
        pageGrid.add(itemPathField, 1, row++);

        Label projectLabel = new Label(BundleUtils.getMsg("wizard.project.choose"));
        Button projectButton = new Button(BundleUtils.getMsg("button.add"));
        projectButton.setOnAction(addProjectEventHandler(projectLabel));
        pageGrid.add(projectButton, 0, row);
        pageGrid.add(projectLabel, 1, row);

        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, wizardProperties);
                itemPathField.setText(BundleUtils.getMsg("wizard.item.location"));
                projectLabel.setVisible(true);
                projectButton.setVisible(true);
                if (UploadType.valueFor(getValue(wizard, ArgName.uploadType)) == UploadType.STATEMENT) {
                    itemPathField.setText(BundleUtils.getMsg("wizard.item.statement.location"));
                    projectLabel.setVisible(false);
                    projectButton.setVisible(false);
                }

                projectLabel.setText(BundleUtils.getMsg("wizard.project.choose"));
                String projectPath = wizardProperties.getProperty(ArgName.projectPath.name());
                if (!StringUtils.nullOrEmpty(projectPath)) {
                    projectLabel.setText(trimPath(projectPath));
                }

                String itemPath = wizardProperties.getProperty(ArgName.itemPath.name());
                if (!StringUtils.nullOrEmpty(itemPath)) {
                    itemPathField.setText(trimPath(itemPath));
                }
            }

            private String trimPath(String path) {
                int maxPathLength = 50;
                if (path.length() > 50) {
                    path = path.substring(0, maxPathLength) + "...";
                }
                return path;
            }

            @Override
            public void onExitingPage(Wizard wizard) {
                projectLabel.setText(getValue(wizard, ArgName.projectPath));
            }
        };
        wizardPane.setHeaderText(BundleUtils.getMsg("paths.panel.title") + " (" + step + "/6)");
        wizardPane.setContent(pageGrid);

        return wizardPane;
    }

    private EventHandler<ActionEvent> addProjectEventHandler(Label projectLabel) {
        return event -> {
            propertiesDao.saveRunConfig(wizardProperties);
            String[] args = wizardProperties.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .toArray(String[]::new);
            ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(args);
            uiLauncher = new UILauncher(primaryStage, applicationProperties);
            if (applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
                uiLauncher.showToolkitProjectsWindow(wizardProperties);
            } else {
                uiLauncher.showProjectsWindow(wizardProperties);
            }
            projectLabel.setText("Set");
        };
    }

    private EventHandler<ActionEvent> addItemEventHandler(TextField itemPathField) {
        return event -> {
            UploadType uploadType = UploadType.valueFor(wizardProperties.getProperty(ArgName.uploadType.name()));
            if (uploadType == UploadType.STATEMENT) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle(BundleUtils.getMsg("directory.item.statement.title"));
                File statementFile = fileChooser.showOpenDialog(primaryStage);
                if (statementFile != null && statementFile.exists() && statementFile.isFile()) {
                    itemPathField.setText(statementFile.getAbsolutePath());
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                directoryChooser.setTitle(BundleUtils.getMsg("directory.item.store"));
                File itemPathDirectory = directoryChooser.showDialog(primaryStage);
                if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                    itemPathField.setText(itemPathDirectory.getAbsolutePath());
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
                propertiesDao.saveToolkitSettings(wizardProperties);
                propertiesDao.saveRunConfig(wizardProperties);
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
                String property = wizardProperties.getProperty(ArgName.uploadType.name());
                if (StringUtils.nullOrEmpty(property)) {
                    return committerPage;
                }
                switch (UploadType.valueFor(property)) {
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
