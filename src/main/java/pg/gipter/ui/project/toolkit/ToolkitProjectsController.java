package pg.gipter.ui.project.toolkit;

import javafx.application.Platform;
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
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.ToolkitService;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.*;
import pg.gipter.ui.project.ProjectDetails;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.*;
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

    private final static String CASES = "/cases/";

    public ToolkitProjectsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setUpColumns();
        initValues();
        setActions();
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
        TableColumn<ProjectDetails, String> vcsTypeColumn = new TableColumn<>();
        vcsTypeColumn.setText(column.getText());
        vcsTypeColumn.setPrefWidth(column.getPrefWidth());
        vcsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("vcsType"));
        vcsTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        vcsTypeColumn.setEditable(false);

        column = projectsTableView.getColumns().get(2);
        TableColumn<ProjectDetails, String> baseWordsColumn = new TableColumn<>();
        baseWordsColumn.setText(column.getText());
        baseWordsColumn.setPrefWidth(column.getPrefWidth());
        baseWordsColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        baseWordsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        baseWordsColumn.setEditable(false);

        projectsTableView.getColumns().clear();
        projectsTableView.getColumns().addAll(nameColumn, vcsTypeColumn, baseWordsColumn);

        projectsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initValues() {
        downloadAvailableProjectNames();
        Set<String> projects = applicationProperties.projectPaths();
        String[] args = applicationProperties.getCurrentRunConfigArray();
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
                    projectsPaths.add(new ProjectDetails(name, ItemType.TOOLKIT_DOCS.name(), path));
                }
            }
            if (projectsPaths.isEmpty()) {
                projectsPaths.add(ProjectDetails.DEFAULT);
            }
            projectsTableView.setItems(projectsPaths);
        }
    }

    private void downloadAvailableProjectNames() {
        WebViewService webViewService = WebViewService.getInstance();
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
                            .map(p -> new ProjectDetails(p, ItemType.TOOLKIT_DOCS.name(), CASES + p))
                            .collect(toList()));
                    projectsTableView.setItems(FXCollections.observableArrayList(projects));
                    saveButton.setDisable(false);
                } else {
                    AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                            .withHeaderText(BundleUtils.getMsg("toolkit.projects.canNotDownload"))
                            .withLinkAction(new LogLinkAction())
                            .withAlertType(Alert.AlertType.WARNING)
                            .withWebViewDetails(new WebViewDetails(webViewService.createImageView(ImageFile.ERROR_CHICKEN_PNG)));
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
                    .withWebViewDetails(new WebViewDetails(webViewService.createImageView(ImageFile.OVERRIDE_PNG)));
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
    }

    private void setActions() {
        showMyProjectsHyperlink.setOnMouseClicked(showMyProjectsMouseClickEventHandler());
        checkProjectHyperlink.setOnMouseClicked(checkProjectsMouseClickEventHandler());
        addProjectButton.setOnAction(addActionEventHandler());
        removeProjectButton.setOnAction(removeButtonActionEventHandler());
        saveButton.setOnAction(saveButtonActionEventHandler());
    }

    private EventHandler<? super MouseEvent> showMyProjectsMouseClickEventHandler() {
        return event -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(applicationProperties.toolkitHostUrl() + "/toolkit/default.aspx");
            showMyProjectsHyperlink.setVisited(false);
        };
    }

    private EventHandler<MouseEvent> checkProjectsMouseClickEventHandler() {
        return event -> Platform.runLater(() -> {
            String projectUrl = String.format("%s%s%s/%s/default.aspx",
                    applicationProperties.toolkitHostUrl(),
                    CASES,
                    projectIdTextField.getText(),
                    projectNameTextField.getText()
            );
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(projectUrl);
            checkProjectHyperlink.setVisited(false);
        });
    }

    private EventHandler<ActionEvent> addActionEventHandler() {
        return event -> {
            String name = projectIdTextField.getText() + "/" + projectNameTextField.getText();
            String path = CASES + name;
            ProjectDetails pd = new ProjectDetails(name, ItemType.TOOLKIT_DOCS.name(), path);
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

    private EventHandler<ActionEvent> removeButtonActionEventHandler() {
        return event -> {
            LinkedHashSet<ProjectDetails> projectsToDelete = new LinkedHashSet<>(
                    projectsTableView.getSelectionModel().getSelectedItems()
            );
            projectsTableView.getItems().removeAll(projectsToDelete);
            if (projectsTableView.getItems().isEmpty()) {
                projectsTableView.getItems().add(ProjectDetails.DEFAULT);
                saveButton.setDisable(true);
            }
            projectsTableView.refresh();
        };
    }

    private EventHandler<ActionEvent> saveButtonActionEventHandler() {
        return event -> {
            String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            applicationProperties.addProjectPath(projects);
            applicationProperties.save();

            uiLauncher.hideToolkitProjectsWindow();
            if (uiLauncher.hasWizardProperties()) {
                uiLauncher.addPropertyToWizard(ArgName.projectPath.name(), projects);
            } else {
                uiLauncher.setApplicationProperties(applicationProperties);
                uiLauncher.buildAndShowMainWindow();
            }
        };
    }

    private void setProperties() {
        saveButton.setDisable(projectsTableView.getItems().contains(ProjectDetails.DEFAULT));
    }

    private void resetIndicatorProperties(Task<?> task) {
        downloadProgressIndicator.setVisible(true);
        downloadProgressIndicator.progressProperty().unbind();
        downloadProgressIndicator.progressProperty().bind(task.progressProperty());
        downloadLabel.setVisible(true);
        downloadLabel.textProperty().unbind();
        downloadLabel.textProperty().bind(task.messageProperty());
    }
}
