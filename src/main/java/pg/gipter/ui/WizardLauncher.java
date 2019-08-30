package pg.gipter.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

public class WizardLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(WizardLauncher.class);
    private Stage primaryStage;
    private UILauncher uiLauncher;
    private PropertiesDao propertiesDao;

    public WizardLauncher(Stage stage) {
        this.primaryStage = stage;
        propertiesDao = DaoFactory.getPropertiesDao();
    }

    @Override
    public void execute() {
        showLinearWizard();
    }

    private void showLinearWizard() {
        Properties properties = new Properties();

        Wizard wizard = new Wizard();
        wizard.setTitle(BundleUtils.getMsg("wizard.title"));
        WizardPane configurationPage = buildConfigurationPage();
        WizardPane toolkitCredentialsPage = buildToolkitCredentialsPage(properties);
        WizardPane committerPage = buildCommitterPage(properties);
        WizardPane projectPage = buildProjectPage(properties);
        WizardPane finishPage = buildFinishPage(properties);

        wizard.setFlow(buildFlow(configurationPage, toolkitCredentialsPage, committerPage, projectPage, finishPage, properties));

        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                logger.info("Wizard finished.");
                String[] args = propertiesDao.loadArgumentArray(properties.getProperty(ArgName.configurationName.name()));
                uiLauncher = new UILauncher(primaryStage, ApplicationPropertiesFactory.getInstance(args));
                uiLauncher.execute();
            }
        });
    }

    private Wizard.Flow buildFlow(WizardPane configurationPage, WizardPane toolkitCredentialsPage, WizardPane committerPage,
                                  WizardPane projectPage, WizardPane finishPage, Properties properties) {
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
                    return configurationPage;
                } else if (currentPage == configurationPage) {
                    return toolkitCredentialsPage;
                } else if (currentPage == toolkitCredentialsPage) {
                    String property = properties.getProperty(ArgName.uploadType.name());
                    if (StringUtils.nullOrEmpty(property) || UploadType.valueFor(property) == UploadType.TOOLKIT_DOCS) {
                        return projectPage;
                    }
                    return committerPage;
                } else if (currentPage == committerPage) {
                    return projectPage;
                } else {
                    return finishPage;
                }
            }
        };
    }

    private WizardPane buildToolkitCredentialsPage(Properties properties) {
        int row = 0;

        WizardPane page = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, properties);
                propertiesDao.saveRunConfig(properties);
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

        page.setHeaderText(BundleUtils.getMsg("wizard.toolkit.credentials") + " (2/5)");
        page.setContent(gridPane);
        return page;
    }

    private void updateProperties(Wizard wizard, Properties properties) {
        properties.put(ArgName.configurationName.name(), String.valueOf(wizard.getSettings().get(ArgName.configurationName.name())));
        properties.put(ArgName.uploadType.name(), String.valueOf(wizard.getSettings().get(ArgName.uploadType.name())));
        properties.put(ArgName.toolkitUsername.name(), String.valueOf(wizard.getSettings().get(ArgName.toolkitUsername.name())));
        properties.put(ArgName.toolkitPassword.name(), String.valueOf(wizard.getSettings().get(ArgName.toolkitPassword.name())));
        properties.put(ArgName.author.name(), String.valueOf(wizard.getSettings().get(ArgName.author.name())));
        properties.put(ArgName.committerEmail.name(), String.valueOf(wizard.getSettings().get(ArgName.committerEmail.name())));
        properties.put(ArgName.itemPath.name(), String.valueOf(wizard.getSettings().get(ArgName.itemPath.name())));
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

    private WizardPane buildConfigurationPage() {
        int row = 0;

        WizardPane wizardPane = new WizardPane();
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label(BundleUtils.getMsg("main.configurationName")), 0, row);
        TextField configurationName = createTextField(ArgName.configurationName.name());
        gridPane.add(configurationName, 1, row++);

        gridPane.add(new Label(BundleUtils.getMsg("csv.panel.uploadType")), 0, row);
        ComboBox<UploadType> comboBox = createComboBox(ArgName.uploadType.name());
        gridPane.add(comboBox, 1, row);

        wizardPane.setHeaderText(BundleUtils.getMsg("wizard.configuration.details") + " (1/5)");
        wizardPane.setContent(gridPane);
        return wizardPane;
    }

    private ComboBox<UploadType> createComboBox(String id) {
        ComboBox<UploadType> comboBox = new ComboBox<>();
        comboBox.setId(id);
        comboBox.setValue(UploadType.SIMPLE);
        comboBox.setItems(FXCollections.observableArrayList(UploadType.values()));
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        return comboBox;
    }

    private WizardPane buildCommitterPage(Properties properties) {
        int row = 0;

        WizardPane page2 = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, properties);
                propertiesDao.saveRunConfig(properties);
                propertiesDao.saveToolkitSettings(properties);
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

        page2.setHeaderText(BundleUtils.getMsg("wizard.scv.details") + " (3/5)");
        page2.setContent(pageGrid);
        return page2;
    }

    private WizardPane buildProjectPage(Properties properties) {
        int row = 0;
        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, properties);
                propertiesDao.saveRunConfig(properties);
                propertiesDao.saveToolkitSettings(properties);
            }
        };
        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        Label projectLabel = new Label(BundleUtils.getMsg("paths.panel.projectPath"));
        Button projectButton = new Button(BundleUtils.getMsg("button.add"));
        projectButton.setOnAction(addProjectEventHandler(properties, projectLabel));
        pageGrid.add(projectButton, 0, row);
        pageGrid.add(projectLabel, 1, row++);

        TextField itemPathField = createTextField(ArgName.itemPath.name());
        itemPathField.setDisable(true);
        itemPathField.setText(BundleUtils.getMsg("paths.panel.itemPath"));
        Button itemButton = new Button(BundleUtils.getMsg("button.add"));
        itemButton.setOnAction(addItemEventHandler(properties, itemPathField));
        pageGrid.add(itemButton, 0, row);
        pageGrid.add(itemPathField, 1, row);

        wizardPane.setHeaderText(BundleUtils.getMsg("paths.panel.title") + " (4/5)");
        wizardPane.setContent(pageGrid);

        return wizardPane;
    }

    private EventHandler<ActionEvent> addProjectEventHandler(Properties properties, Label projectLabel) {
        return event -> {
            propertiesDao.saveRunConfig(properties);
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
            projectLabel.setText("Set");
        };
    }

    private EventHandler<ActionEvent> addItemEventHandler(Properties properties, TextField itemPathField) {
        return event -> {
            propertiesDao.saveRunConfig(properties);
            UploadType uploadType = UploadType.valueFor(properties.getProperty(ArgName.uploadType.name()));
            if (uploadType == UploadType.STATEMENT) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle(BundleUtils.getMsg("directory.item.statement.title"));
                File statementFile = fileChooser.showOpenDialog(uiLauncher.currentWindow());
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

    private WizardPane buildFinishPage(Properties properties) {
        WizardPane wizardPane = new WizardPane() {
            @Override
            public void onEnteringPage(Wizard wizard) {
                updateProperties(wizard, properties);
                Properties savedProperties = propertiesDao.loadApplicationProperties(properties.getProperty(ArgName.configurationName.name()))
                        .orElseGet(() -> properties);
                properties.setProperty(ArgName.projectPath.name(), savedProperties.getProperty(ArgName.projectPath.name()));
                propertiesDao.saveRunConfig(properties);
                propertiesDao.saveToolkitSettings(properties);
            }
        };
        wizardPane.setHeaderText("Huuzzaaaa!! Konfiguracja ukonczona. (5/5)");

        int row = 0;
        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        GridPane gridPane = new GridPane();
        gridPane.add(new Label(BundleUtils.getMsg("toolkit.panel.username")), 0, row);
        TextField username = createTextField(ArgName.toolkitUsername.name());
        username.setDisable(true);
        username.setText(properties.getProperty(ArgName.toolkitUsername.name()));
        gridPane.add(username, 1, row++);

        gridPane.add(new Label(BundleUtils.getMsg("toolkit.panel.password")), 0, row);
        TextField password = createTextField(ArgName.toolkitPassword.name());
        password.setDisable(true);
        password.setText(properties.getProperty(ArgName.toolkitPassword.name()));
        gridPane.add(password, 1, row++);

        gridPane.add(new Label(BundleUtils.getMsg("main.configurationName")), 0, row);
        TextField configurationName = createTextField(ArgName.configurationName.name());
        configurationName.setDisable(true);
        configurationName.setText(properties.getProperty(ArgName.configurationName.name()));
        gridPane.add(configurationName, 1, row++);

        gridPane.add(new Label(BundleUtils.getMsg("csv.panel.uploadType")), 0, row);
        TextField uploadType = createTextField(ArgName.uploadType.name());
        uploadType.setDisable(true);
        uploadType.setText(properties.getProperty(ArgName.uploadType.name()));
        gridPane.add(uploadType, 1, row++);

        pageGrid.add(new Label(BundleUtils.getMsg("csv.panel.authors")), 0, row);
        TextField author = createTextField(ArgName.author.name());
        author.setDisable(true);
        author.setText(properties.getProperty(ArgName.author.name()));
        pageGrid.add(author, 1, row++);

        pageGrid.add(new Label(BundleUtils.getMsg("csv.panel.committerEmail")), 0, row);
        TextField committerEmail = createTextField(ArgName.committerEmail.name());
        committerEmail.setDisable(true);
        committerEmail.setText(properties.getProperty(ArgName.committerEmail.name()));
        pageGrid.add(committerEmail, 1, row++);

        pageGrid.add(new Label(BundleUtils.getMsg("paths.panel.projectPath")), 0, row);
        TextField projectPath = createTextField(ArgName.projectPath.name());
        projectPath.setDisable(true);
        projectPath.setText(properties.getProperty(ArgName.projectPath.name()));
        pageGrid.add(projectPath, 1, row++);

        pageGrid.add(new Label(BundleUtils.getMsg("paths.panel.itemPath")), 0, row);
        TextField itemPath = createTextField(ArgName.itemPath.name());
        itemPath.setDisable(true);
        itemPath.setText(properties.getProperty(ArgName.itemPath.name()));
        pageGrid.add(itemPath, 1, row);

        wizardPane.setContent(pageGrid);

        return wizardPane;
    }
}
