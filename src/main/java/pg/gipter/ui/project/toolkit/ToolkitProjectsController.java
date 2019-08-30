package pg.gipter.ui.project.toolkit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManager;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.service.ToolkitService;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.ui.project.ProjectDetails;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ToolkitProjectsController extends AbstractController {

    @FXML
    private Hyperlink showMyProjectsHyperlink;
    @FXML
    private Hyperlink checkProjectHyperlink;
    @FXML
    private TextField projectIdTextField;
    @FXML
    private TextField projectNameTextField;
    @FXML
    private Button saveButton;
    @FXML
    private Button addProjectButton;
    @FXML
    private Button removeProjectButton;
    @FXML
    private TableView<ProjectDetails> projectsTableView;
    @FXML
    private ProgressIndicator downloadProgressIndicator;
    @FXML
    private Label downloadLabel;

    private ApplicationProperties applicationProperties;
    private Set<ProjectDetails> projectsToDelete;
    private final String CASES = "/cases/";

    public ToolkitProjectsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setUpColumns();
        initValues();
        setupActions();
        setProperties();
    }

    private void setUpColumns() {
        TableColumn<ProjectDetails, ?> column = projectsTableView.getColumns().get(0);
        TableColumn<ProjectDetails, String> nameColumn = new TableColumn<>();
        nameColumn.setText(column.getText());
        nameColumn.setPrefWidth(column.getPrefWidth());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setEditable(false);

        column = projectsTableView.getColumns().get(1);
        TableColumn<ProjectDetails, String> cvsTypeColumn = new TableColumn<>();
        cvsTypeColumn.setText(column.getText());
        cvsTypeColumn.setPrefWidth(column.getPrefWidth());
        cvsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("cvsType"));
        cvsTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        cvsTypeColumn.setEditable(false);

        column = projectsTableView.getColumns().get(2);
        TableColumn<ProjectDetails, String> baseWordsColumn = new TableColumn<>();
        baseWordsColumn.setText(column.getText());
        baseWordsColumn.setPrefWidth(column.getPrefWidth());
        baseWordsColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        baseWordsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        baseWordsColumn.setEditable(false);

        projectsTableView.getColumns().clear();
        projectsTableView.getColumns().addAll(nameColumn, cvsTypeColumn, baseWordsColumn);

        projectsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        projectsTableView.getSelectionModel().selectedItemProperty().addListener(toDeleteChangeListener());
    }

    private ChangeListener<ProjectDetails> toDeleteChangeListener() {
        return (observable, oldValue, newValue) ->
                projectsToDelete = new LinkedHashSet<>(projectsTableView.getSelectionModel().getSelectedItems());
    }

    private void initValues() {
        downloadAvailableProjectNames();
        Set<String> projects = applicationProperties.projectPaths();
        String[] args = propertiesDao.loadArgumentArray(applicationProperties.configurationName());
        if (args.length == 0) {
            projects.clear();
            projects.add(ProjectDetails.DEFAULT.getName());
        }
        if (projects.contains(ProjectDetails.DEFAULT.getName())) {
            projectsTableView.setItems(FXCollections.observableArrayList(ProjectDetails.DEFAULT));
        } else {
            ObservableList<ProjectDetails> projectsPaths = FXCollections.observableArrayList();
            for (String path : projects) {
                if (path.startsWith(CASES)) {
                    String name = path.replace(CASES, "");
                    projectsPaths.add(new ProjectDetails(name, UploadType.TOOLKIT_DOCS.name(), path));
                }
            }
            if (projectsPaths.isEmpty()) {
                projectsPaths.add(ProjectDetails.DEFAULT);
            }
            projectsTableView.setItems(projectsPaths);
        }
    }

    private void downloadAvailableProjectNames() {
        if (applicationProperties.isToolkitCredentialsSet()) {
            final ToolkitService toolkitService = new ToolkitService(applicationProperties);
            resetIndicatorProperties(toolkitService);
            uiLauncher.executeOutsideUIThread(() -> {
                Set<String> links = toolkitService.downloadUserProjects();
                if (!links.isEmpty()) {
                    Set<ProjectDetails> projects = new LinkedHashSet<>();
                    if (!projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                        projects.addAll(projectsTableView.getItems());
                    }
                    projects.addAll(links.stream()
                            .map(link -> link.substring(link.indexOf(CASES)))
                            .map(p -> new ProjectDetails(p.replace(CASES, ""), UploadType.TOOLKIT_DOCS.name(), p))
                            .collect(toList()));
                    projectsTableView.setItems(FXCollections.observableArrayList(projects));
                    saveButton.setDisable(false);
                } else {
                    AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                            .withHeaderText(BundleUtils.getMsg("toolkit.projects.canNotDownload"))
                            .withLink(AlertHelper.logsFolder())
                            .withAlertType(Alert.AlertType.WARNING)
                            .withWindowType(WindowType.LOG_WINDOW)
                            .withImage(ImageFile.ERROR_CHICKEN_PNG);
                    Platform.runLater(() -> {
                        alertWindowBuilder.buildAndDisplayWindow();
                        downloadProgressIndicator.setVisible(false);
                        downloadLabel.setVisible(false);
                    });
                }
            });

        } else {
            downloadProgressIndicator.setVisible(false);
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("toolkit.projects.credentialsNotSet"))
                    .withAlertType(Alert.AlertType.WARNING)
                    .withWindowType(WindowType.OVERRIDE_WINDOW)
                    .withImage(ImageFile.OVERRIDE_PNG);
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
    }

    private void setupActions() {
        showMyProjectsHyperlink.setOnMouseClicked(showMyProjectsMouseClickEventHandler());
        checkProjectHyperlink.setOnMouseClicked(checkProjectsMouseClickEventHandler());
        addProjectButton.setOnAction(addActionEventHandler());
        removeProjectButton.setOnAction(removeButtonActionEventHandler());
        saveButton.setOnAction(saveButtonActionEventHandler());
    }

    private EventHandler<? super MouseEvent> showMyProjectsMouseClickEventHandler() {
        return event -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(applicationProperties.toolkitUrl() + "/toolkit/default.aspx");
            showMyProjectsHyperlink.setVisited(false);
        };
    }

    @NotNull
    private EventHandler<MouseEvent> checkProjectsMouseClickEventHandler() {
        return event -> Platform.runLater(() -> {
            String projectUrl = String.format("%s%s%s/%s/default.aspx",
                    applicationProperties.toolkitUrl(),
                    CASES,
                    projectIdTextField.getText(),
                    projectNameTextField.getText()
            );
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(projectUrl);
            checkProjectHyperlink.setVisited(false);
        });
    }

    @NotNull
    private EventHandler<ActionEvent> addActionEventHandler() {
        return event -> {
            String name = projectIdTextField.getText() + "/" + projectNameTextField.getText();
            String path = CASES + name;
            ProjectDetails pd = new ProjectDetails(name, UploadType.TOOLKIT_DOCS.name(), path);
            if (projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                projectsTableView.setItems(FXCollections.observableArrayList(pd));
            } else {
                Set<ProjectDetails> projectDetails = new LinkedHashSet<>(projectsTableView.getItems());
                projectDetails.add(pd);
                projectsTableView.setItems(FXCollections.observableArrayList(projectDetails));
            }
            projectsTableView.refresh();
            saveButton.setDisable(false);
            projectIdTextField.clear();
            projectNameTextField.clear();
        };
    }

    @NotNull
    private EventHandler<ActionEvent> removeButtonActionEventHandler() {
        return event -> {
            projectsToDelete = new LinkedHashSet<>(projectsTableView.getSelectionModel().getSelectedItems());
            projectsTableView.getItems().removeAll(projectsToDelete);
            if (projectsTableView.getItems().isEmpty()) {
                projectsTableView.getItems().add(ProjectDetails.DEFAULT);
                saveButton.setDisable(true);
            }
            projectsTableView.refresh();
        };
    }

    @NotNull
    private EventHandler<ActionEvent> saveButtonActionEventHandler() {
        return event -> {
            String configurationName = applicationProperties.configurationName();
            Properties properties = propertiesDao.createProperties(applicationProperties.getArgs());
            String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            properties.setProperty(ArgName.projectPath.name(), projects);
            propertiesDao.saveRunConfig(properties);

            uiLauncher.hideToolkitProjectsWindow();
            if (uiLauncher.isInvokeExecute()) {
                applicationProperties = ApplicationPropertiesFactory.getInstance(propertiesDao.loadArgumentArray(configurationName));
                uiLauncher.setApplicationProperties(applicationProperties);
                uiLauncher.buildAndShowMainWindow();
            }
        };
    }

    private void setProperties() {
        saveButton.setDisable(projectsTableView.getItems().contains(ProjectDetails.DEFAULT));
    }

    private void resetIndicatorProperties(Task task) {
        downloadProgressIndicator.setVisible(true);
        downloadProgressIndicator.progressProperty().unbind();
        downloadProgressIndicator.progressProperty().bind(task.progressProperty());
        downloadLabel.setVisible(true);
        downloadLabel.textProperty().unbind();
        downloadLabel.textProperty().bind(task.messageProperty());
    }
}
