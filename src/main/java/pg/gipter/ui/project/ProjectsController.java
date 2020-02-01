package pg.gipter.ui.project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import org.jetbrains.annotations.NotNull;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dto.RunConfig;
import pg.gipter.core.producer.command.UploadType;
import pg.gipter.core.producer.command.VersionControlSystem;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProjectsController extends AbstractController {

    @FXML
    private TableView<ProjectDetails> projectsTableView;
    @FXML
    private Button searchProjectsButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button addProjectButton;
    @FXML
    private Button removeProjectButton;

    private ApplicationProperties applicationProperties;

    public ProjectsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setUpColumns();
        initValues();
        setupActions(resources);
        setProperties(resources);
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
        TableColumn<ProjectDetails, String> path = new TableColumn<>();
        path.setText(column.getText());
        path.setPrefWidth(column.getPrefWidth());
        path.setCellValueFactory(new PropertyValueFactory<>("path"));
        path.setCellFactory(TextFieldTableCell.forTableColumn());
        path.setEditable(false);

        projectsTableView.getColumns().clear();
        projectsTableView.getColumns().addAll(nameColumn, cvsTypeColumn, path);

        projectsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initValues() {
        Set<String> projects = applicationProperties.projectPaths();
        if (projects.size() == 1 && projects.contains(ProjectDetails.DEFAULT.getName())) {
            projectsTableView.setItems(FXCollections.observableArrayList(ProjectDetails.DEFAULT));
        } else {
            ObservableList<ProjectDetails> projectsPaths = FXCollections.observableArrayList();
            for (String path : projects) {
                File project = new File(path);
                Optional<String> supportedVcs = getSupportedVcs(project);
                if (!supportedVcs.isPresent() && applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
                    ProjectDetails pd = new ProjectDetails(
                            project.getName(),
                            applicationProperties.uploadType().name(),
                            path
                    );
                    projectsPaths.add(pd);
                } else if (supportedVcs.isPresent() && applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS) {
                    ProjectDetails pd = new ProjectDetails(
                            project.getName(),
                            supportedVcs.get(),
                            path
                    );
                    projectsPaths.add(pd);
                }
            }
            if (projectsPaths.isEmpty()) {
                projectsPaths.add(ProjectDetails.DEFAULT);
            }
            projectsTableView.setItems(projectsPaths);
        }
    }

    @NotNull
    private Optional<String> getSupportedVcs(File project) {
        try {
            return Optional.of(VersionControlSystem.valueFrom(project).name());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private void setupActions(ResourceBundle resources) {
        searchProjectsButton.setOnAction(searchButtonActionEventHandler(resources));
        saveButton.setOnAction(saveButtonActionEventHandler());
        addProjectButton.setOnAction(addButtonActionEventHandler(resources));
        removeProjectButton.setOnAction(removeButtonActionEventHandler());
    }

    private void setProperties(ResourceBundle resources) {
        if (applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
            searchProjectsButton.setDisable(true);
            Tooltip tooltip = new Tooltip();
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.setFont(Font.font("Courier New", 14));
            tooltip.setText(resources.getString("projects.button.search.tooltip"));
        }
        saveButton.setDisable(projectsTableView.getItems().contains(ProjectDetails.DEFAULT));
    }

    @NotNull
    private EventHandler<ActionEvent> searchButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle(resources.getString("directory.search.title"));
            File directory = directoryChooser.showDialog(uiLauncher.currentWindow());
            if (directory != null && directory.exists() && directory.isDirectory()) {
                CompletableFuture.supplyAsync(() -> FXCollections.observableList(searchForProjects(directory)), uiLauncher.nonUIExecutor())
                        .thenAcceptAsync(this::refreshProjectTableView, Platform::runLater);
            }
        };
    }

    private void refreshProjectTableView(@NotNull ObservableList<ProjectDetails> observableList) {
        if (!observableList.isEmpty()) {
            if (projectsTableView.getItems().size() == 1 && projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                projectsTableView.setItems(observableList);
            } else {
                projectsTableView.getItems().addAll(observableList);
            }
            projectsTableView.refresh();
            saveButton.setDisable(false);
        }
    }

    List<ProjectDetails> searchForProjects(File directory) {
        List<ProjectDetails> result = new ArrayList<>();
        try {
            VersionControlSystem vcs = VersionControlSystem.valueFrom(directory);
            result.add(new ProjectDetails(directory.getName(), vcs.name(), directory.getAbsolutePath()));
        } catch (IllegalArgumentException ex) {
            for (File file : directory.listFiles()) {
                if (file != null && file.isDirectory()) {
                    result.addAll(searchForProjects(file));
                }
            }
        }
        return result;
    }

    @NotNull
    private EventHandler<ActionEvent> saveButtonActionEventHandler() {
        return event -> {
            String configurationName = applicationProperties.configurationName();
            Optional<RunConfig> runConfig = configurationDao.loadRunConfig(configurationName);
            final String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            runConfig.ifPresent(rc -> {
                rc.setProjectPath(projects);
                configurationDao.saveRunConfig(rc);
            });

            uiLauncher.hideProjectsWindow();
            if (uiLauncher.isInvokeExecute()) {
                applicationProperties = ApplicationPropertiesFactory.getInstance(configurationDao.loadArgumentArray(configurationName));
                uiLauncher.setApplicationProperties(applicationProperties);
                uiLauncher.buildAndShowMainWindow();
            } else {
                uiLauncher.addPropertyToWizard(ArgName.projectPath.name(), projects);
            }
        };
    }

    private EventHandler<ActionEvent> addButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle(resources.getString("directory.item.title"));
            File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
            if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                try {
                    String vcsType = VersionControlSystem.valueFrom(itemPathDirectory).name();
                    ProjectDetails project = new ProjectDetails(
                            itemPathDirectory.getName(),
                            vcsType,
                            itemPathDirectory.getAbsolutePath()
                    );
                    if (projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                        projectsTableView.setItems(FXCollections.observableArrayList(project));
                        saveButton.setDisable(false);
                    } else {
                        projectsTableView.getItems().add(project);
                    }
                    projectsTableView.refresh();
                } catch (IllegalArgumentException ex) {
                    AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                            .withHeaderText(ex.getMessage())
                            .withLink(AlertHelper.logsFolder())
                            .withWindowType(WindowType.LOG_WINDOW)
                            .withAlertType(Alert.AlertType.ERROR)
                            .withImage(ImageFile.ERROR_CHICKEN_PNG);
                    Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
                }
            }
        };
    }

    private EventHandler<ActionEvent> removeButtonActionEventHandler() {
        return event -> {
            LinkedHashSet<ProjectDetails> projectsToDelete = new LinkedHashSet<>(projectsTableView.getSelectionModel().getSelectedItems());
            projectsTableView.getItems().removeAll(projectsToDelete);
            if (projectsTableView.getItems().isEmpty()) {
                projectsTableView.getItems().add(ProjectDetails.DEFAULT);
            }
            projectsTableView.refresh();
            saveButton.setDisable(projectsTableView.getItems().contains(ProjectDetails.DEFAULT));
        };
    }
}
