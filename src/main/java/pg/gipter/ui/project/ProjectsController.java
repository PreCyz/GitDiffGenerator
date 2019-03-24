package pg.gipter.ui.project;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import org.jetbrains.annotations.NotNull;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.PropertiesHelper;

import java.io.File;
import java.net.URL;
import java.util.*;
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
        setUpButtons(resources);
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
                ProjectDetails pd = new ProjectDetails(
                        project.getName(),
                        VersionControlSystem.valueFrom(project).name(),
                        path
                );
                projectsPaths.add(pd);
            }
            projectsTableView.setItems(projectsPaths);
        }
    }

    private void setUpButtons(ResourceBundle resources) {
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
            directoryChooser.setTitle(resources.getString("directory.item.title"));
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
            Properties uiApplications = helper.loadUIApplicationProperties().orElseGet(Properties::new);
            String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            uiApplications.setProperty(ArgName.projectPath.name(), projects);
            helper.saveToUIApplicationProperties(uiApplications);
            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(
                    new String[]{ArgName.preferredArgSource + "=" + PreferredArgSource.UI}
            ));
            uiLauncher.hideProjectsWindow();
            uiLauncher.buildAndShowMainWindow();
        };
    }

    @NotNull
    private EventHandler<ActionEvent> addButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle(resources.getString("directory.item.title"));
            File itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow());
            if (itemPathDirectory != null && itemPathDirectory.exists() && itemPathDirectory.isDirectory()) {
                try {
                    ProjectDetails project = new ProjectDetails(
                            itemPathDirectory.getName(),
                            VersionControlSystem.valueFrom(itemPathDirectory).name(),
                            itemPathDirectory.getAbsolutePath()
                    );
                    if (projectsTableView.getItems().size() == 1 && projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                        projectsTableView.setItems(FXCollections.observableArrayList(project));
                    } else {
                        projectsTableView.getItems().add(project);
                    }
                    projectsTableView.refresh();
                } catch (IllegalArgumentException ex) {
                    AlertHelper.displayWindow(ex.getMessage(), AlertHelper.logsFolder(), AlertHelper.LOG_WINDOW, Alert.AlertType.ERROR);
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
