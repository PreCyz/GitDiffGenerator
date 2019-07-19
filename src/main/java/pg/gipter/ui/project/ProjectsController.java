package pg.gipter.ui.project;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import pg.gipter.producer.command.UploadType;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.PropertiesHelper;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

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

    private Set<ProjectDetails> projectsToDelete;
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
        setupButtons(resources);
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

    private void setupButtons(ResourceBundle resources) {
        if (applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
            searchProjectsButton.setDisable(true);
            Tooltip tooltip = new Tooltip();
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.setFont(Font.font("Courier New", 14));
            tooltip.setText(resources.getString("projects.button.search.tooltip"));
        }
        searchProjectsButton.setOnAction(searchButtonActionEventHandler(resources));
        saveButton.setOnAction(saveButtonActionEventHandler());
        addProjectButton.setOnAction(addButtonActionEventHandler(resources));
        removeProjectButton.setOnAction(removeButtonActionEventHandler());
    }

    @NotNull
    private EventHandler<ActionEvent> searchButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle(resources.getString("directory.search.title"));
            File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
            if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                ObservableList<ProjectDetails> projects = FXCollections.observableList(searchForProjects(itemPathDirectory));
                if (!projects.isEmpty()) {
                    if (projectsTableView.getItems().size() == 1 && projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                        projectsTableView.setItems(projects);
                    } else {
                        projectsTableView.getItems().addAll(projects);
                    }
                }
            }
        };
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
            PropertiesHelper helper = new PropertiesHelper();
            Properties uiApplications = helper.loadApplicationProperties(uiLauncher.getConfigurationName()).orElseGet(Properties::new);
            String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            uiApplications.setProperty(ArgName.projectPath.name(), projects);
            helper.addAndSaveApplicationProperties(uiApplications);
            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(
                    new String[]{ArgName.configurationName + "=" + uiLauncher.getConfigurationName()}
            ));
            uiLauncher.hideProjectsWindow();
            uiLauncher.buildAndShowMainWindow();
        };
    }

    @NotNull
    private EventHandler<ActionEvent> addButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            if (applicationProperties.uploadType() != UploadType.TOOLKIT_DOCS) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                directoryChooser.setTitle(resources.getString("directory.item.title"));
                File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
                if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                    try {
                        String vcsType = vcsType = VersionControlSystem.valueFrom(itemPathDirectory).name();;
                        ProjectDetails project = new ProjectDetails(
                                itemPathDirectory.getName(),
                                vcsType,
                                itemPathDirectory.getAbsolutePath()
                        );
                        if (projectsTableView.getItems().size() == 1 && projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                            projectsTableView.setItems(FXCollections.observableArrayList(project));
                        } else {
                            projectsTableView.getItems().add(project);
                        }
                        projectsTableView.refresh();
                    } catch (IllegalArgumentException ex) {
                        Platform.runLater(() -> new AlertWindowBuilder()
                                .withHeaderText(ex.getMessage())
                                .withLink(AlertHelper.logsFolder())
                                .withWindowType(WindowType.LOG_WINDOW)
                                .withAlertType(Alert.AlertType.ERROR)
                                .withImage()
                                .buildAndDisplayWindow()
                        );
                    }
                }
            } else {
                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle(resources.getString("projects.tooltip.title"));
                dialog.setHeaderText(resources.getString("projects.tooltip.header"));

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && !StringUtils.nullOrEmpty(result.get().trim())) {
                    Set<String> projects = Stream.of(result.get().split(",")).collect(toSet());
                    for (String project : projects) {
                        ProjectDetails pd = new ProjectDetails(
                                project.replace("/cases/", ""),
                                UploadType.TOOLKIT_DOCS.name(),
                                project
                        );
                        if (projectsTableView.getItems().size() == 1 && projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                            projectsTableView.setItems(FXCollections.observableArrayList(pd));
                        } else {
                            projectsTableView.getItems().add(pd);
                        }
                    }
                    projectsTableView.refresh();
                }
            }
        };
    }

    @NotNull
    private EventHandler<ActionEvent> removeButtonActionEventHandler() {
        return event -> {
            projectsToDelete = new LinkedHashSet<>(projectsTableView.getSelectionModel().getSelectedItems());
            projectsTableView.getItems().removeAll(projectsToDelete);
            if (projectsTableView.getItems().isEmpty()) {
                projectsTableView.getItems().add(ProjectDetails.DEFAULT);
            }
            projectsTableView.refresh();
        };
    }
}
