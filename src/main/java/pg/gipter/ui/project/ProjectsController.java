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
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.services.vcs.VcsServiceFactory;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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

    public ProjectsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setUpColumns();
        initValues();
        setActions(resources);
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
        TableColumn<ProjectDetails, String> vcsTypeColumn = new TableColumn<>();
        vcsTypeColumn.setText(column.getText());
        vcsTypeColumn.setPrefWidth(column.getPrefWidth());
        vcsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("vcsType"));
        vcsTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        vcsTypeColumn.setEditable(false);

        column = projectsTableView.getColumns().get(2);
        TableColumn<ProjectDetails, String> path = new TableColumn<>();
        path.setText(column.getText());
        path.setPrefWidth(column.getPrefWidth());
        path.setCellValueFactory(new PropertyValueFactory<>("path"));
        path.setCellFactory(TextFieldTableCell.forTableColumn());
        path.setEditable(false);

        projectsTableView.getColumns().clear();
        projectsTableView.getColumns().addAll(nameColumn, vcsTypeColumn, path);

        projectsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initValues() {
        Set<String> projects = applicationProperties.projectPaths();
        if (projects.size() == 1 && projects.contains(ProjectDetails.DEFAULT.getName())) {
            projectsTableView.setItems(FXCollections.observableArrayList(ProjectDetails.DEFAULT));
        } else {
            ObservableList<ProjectDetails> projectsPaths = FXCollections.observableArrayList();
            for (String path : projects) {
                Path project = Paths.get(path);
                Optional<String> supportedVcs = getSupportedVcs(Paths.get(path));
                if (supportedVcs.isEmpty() && applicationProperties.itemType() == ItemType.TOOLKIT_DOCS) {
                    ProjectDetails pd = new ProjectDetails(
                            project.getFileName().toString(),
                            applicationProperties.itemType().name(),
                            path
                    );
                    projectsPaths.add(pd);
                } else if (supportedVcs.isPresent() && applicationProperties.itemType() != ItemType.TOOLKIT_DOCS) {
                    ProjectDetails pd = new ProjectDetails(
                            project.getFileName().toString(),
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

    private Optional<String> getSupportedVcs(Path project) {
        try {
            return Optional.of(VersionControlSystem.valueFrom(project)).map(Enum::name);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private void setActions(ResourceBundle resources) {
        searchProjectsButton.setOnAction(searchButtonActionEventHandler(resources));
        saveButton.setOnAction(saveButtonActionEventHandler());
        addProjectButton.setOnAction(addButtonActionEventHandler(resources));
        removeProjectButton.setOnAction(removeButtonActionEventHandler());
    }

    private void setProperties(ResourceBundle resources) {
        if (applicationProperties.itemType() == ItemType.TOOLKIT_DOCS) {
            searchProjectsButton.setDisable(true);
            Tooltip tooltip = new Tooltip();
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.setFont(Font.font("Courier New", 14));
            tooltip.setText(resources.getString("projects.button.search.tooltip"));
        }
        saveButton.setDisable(projectsTableView.getItems().contains(ProjectDetails.DEFAULT));
    }

    private EventHandler<ActionEvent> searchButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(Paths.get(".").toFile());
            directoryChooser.setTitle(resources.getString("directory.search.title"));
            final Optional<File> directory = Optional.ofNullable(directoryChooser.showDialog(uiLauncher.currentWindow()));
            if (directory.isPresent() && Files.exists(directory.get().toPath()) &&
                    Files.isDirectory(directory.get().toPath())) {
                CompletableFuture.supplyAsync(() ->
                        FXCollections.observableList(searchForProjects(directory.get().toPath())), uiLauncher.nonUIExecutor()
                ).thenAcceptAsync(this::refreshProjectTableView, Platform::runLater);
            }
        };
    }

    private void refreshProjectTableView(ObservableList<ProjectDetails> observableList) {
        if (observableList.isEmpty()) {
            projectsTableView.setItems(FXCollections.observableArrayList(ProjectDetails.DEFAULT));
        } else {
            projectsTableView.setItems(observableList);
        }
        projectsTableView.refresh();
        saveButton.setDisable(false);
    }

    List<ProjectDetails> searchForProjects(Path directory) {
        List<ProjectDetails> result = new ArrayList<>();
        try {
            VersionControlSystem vcs = VersionControlSystem.valueFrom(directory);
            result.add(new ProjectDetails(
                    directory.getFileName().toString(), vcs.name(), directory.toAbsolutePath().toString()
            ));
        } catch (IllegalArgumentException ex) {
            try {
                List<Path> files = Files.list(directory).collect(toList());
                for (Path file : files) {
                    if (file != null && Files.isDirectory(file)) {
                        result.addAll(searchForProjects(file));
                    }
                }
            } catch (IOException e) {
                logger.warn("Problem with searching repos.", e);
            }
        }
        return result;
    }

    private EventHandler<ActionEvent> saveButtonActionEventHandler() {
        return event -> {
            final String projects = projectsTableView.getItems().stream().map(ProjectDetails::getPath).collect(Collectors.joining(","));
            applicationProperties.addProjectPath(projects);
            applicationProperties.save();

            validateVcsAvailability();

            uiLauncher.hideProjectsWindow();
            if (uiLauncher.hasWizardProperties()) {
                uiLauncher.addPropertyToWizard(ArgName.projectPath.name(), projects);
            } else {
                uiLauncher.setApplicationProperties(applicationProperties);
                uiLauncher.buildAndShowMainWindow();
            }
        };
    }

    private void validateVcsAvailability() {
        final VcsService vcsService = VcsServiceFactory.getInstance();
        final Optional<ProjectDetails> gitRepo = projectsTableView.getItems()
                .stream()
                .filter(pd -> VersionControlSystem.GIT.name().equals(pd.getVcsType()))
                .findAny();
        gitRepo.ifPresent(pd -> vcsService.setProjectPath(pd.getPath()));
        if (gitRepo.isPresent() && !vcsService.isVcsAvailableInCommandLine()) {
            String lineSeparator = SystemUtils.lineSeparator();
            new AlertWindowBuilder().withAlertType(Alert.AlertType.ERROR)
                    .withHeaderText(BundleUtils.getMsg("projects.alert.git.unavailable.header"))
                    .withMessage(BundleUtils.getMsg("projects.alert.git.unavailable.msg",
                            lineSeparator, lineSeparator, lineSeparator))
                    .withWebViewDetails(WebViewService.getInstance().pullFailWebView())
                    .buildAndDisplayWindow();
        }

    }

    private EventHandler<ActionEvent> addButtonActionEventHandler(ResourceBundle resources) {
        return event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(Paths.get(".").toFile());
            directoryChooser.setTitle(resources.getString("directory.item.title"));
            Path itemPathDirectory = directoryChooser.showDialog(uiLauncher.currentWindow()).toPath();
            if (Files.exists(itemPathDirectory) && Files.isDirectory(itemPathDirectory)) {
                try {
                    String vcsType = VersionControlSystem.valueFrom(itemPathDirectory).name();
                    ProjectDetails project = new ProjectDetails(
                            itemPathDirectory.getFileName().toString(),
                            vcsType,
                            itemPathDirectory.toAbsolutePath().toString()
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
                            .withLinkAction(new LogLinkAction())
                            .withAlertType(Alert.AlertType.ERROR)
                            .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
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
