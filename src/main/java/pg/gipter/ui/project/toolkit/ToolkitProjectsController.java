package pg.gipter.ui.project.toolkit;

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
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManager;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.ui.project.ProjectDetails;
import pg.gipter.utils.BundleUtils;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class ToolkitProjectsController extends AbstractController {

    @FXML
    private TextField projectIdTextField;
    @FXML
    private TextField projectNameTextField;
    @FXML
    private Hyperlink checkProjectHyperlink;
    @FXML
    private Button saveButton;
    @FXML
    private Button addProjectButton;
    @FXML
    private Button removeProjectButton;
    @FXML
    private TableView<ProjectDetails> projectsTableView;

    private ApplicationProperties applicationProperties;
    private Set<ProjectDetails> projectsToDelete;

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
        String[] args = propertiesHelper.loadArgumentArray(applicationProperties.configurationName());
        if (args.length == 0) {
            projects.clear();
            projects.add(ProjectDetails.DEFAULT.getName());
        }
        if (projects.size() == 1 && projects.contains(ProjectDetails.DEFAULT.getName())) {
            projectsTableView.setItems(FXCollections.observableArrayList(ProjectDetails.DEFAULT));
        } else {
            ObservableList<ProjectDetails> projectsPaths = FXCollections.observableArrayList();
            for (String path : projects) {
                File project = new File(path);
                if (applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
                    projectsPaths.add(new ProjectDetails(project.getName(), UploadType.TOOLKIT_DOCS.name(), path));
                }
            }
            if (projectsPaths.isEmpty()) {
                projectsPaths.add(ProjectDetails.DEFAULT);
            }
            projectsTableView.setItems(projectsPaths);
        }
    }

    private void setupActions() {
        checkProjectHyperlink.setOnMouseClicked(mouseClickEventHandler());
        addProjectButton.setOnAction(addActionEventHandler());
        removeProjectButton.setOnAction(removeButtonActionEventHandler());
        saveButton.setOnAction(saveButtonActionEventHandler());
    }

    @NotNull
    private EventHandler<MouseEvent> mouseClickEventHandler() {
        return event -> Platform.runLater(() -> {
            String projectUrl = String.format("%s/cases/%s/%s/default.aspx",
                    applicationProperties.toolkitUrl(),
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
            String path = "/cases/" + name;
            ProjectDetails pd = new ProjectDetails(name, UploadType.TOOLKIT_DOCS.name(), path);
            if (projectsTableView.getItems().size() == 1 && projectsTableView.getItems().contains(ProjectDetails.DEFAULT)) {
                projectsTableView.setItems(FXCollections.observableArrayList(pd));
            } else {
                Set<ProjectDetails> projectDetails = new LinkedHashSet<>(projectsTableView.getItems());
                projectDetails.add(pd);
                projectsTableView.setItems(FXCollections.observableArrayList(projectDetails));
            }
            projectsTableView.refresh();
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

    @NotNull
    private EventHandler<ActionEvent> saveButtonActionEventHandler() {
        return event -> {
            Properties uiApplications = propertiesHelper.loadApplicationProperties(uiLauncher.getConfigurationName()).orElseGet(Properties::new);
            String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            uiApplications.setProperty(ArgName.projectPath.name(), projects);
            propertiesHelper.saveRunConfig(uiApplications);
            uiLauncher.setApplicationProperties(ApplicationPropertiesFactory.getInstance(
                    propertiesHelper.loadArgumentArray(uiLauncher.getConfigurationName())
            ));
            uiLauncher.hideToolkitProjectsWindow();
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWindowType(WindowType.CONFIRMATION_WINDOW)
                    .withImage()
                    .buildAndDisplayWindow()
            );
            uiLauncher.buildAndShowMainWindow();
        };
    }
}
