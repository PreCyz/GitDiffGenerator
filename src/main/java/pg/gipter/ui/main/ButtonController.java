package pg.gipter.ui.main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.configuration.CacheManager;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.ui.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ButtonController extends AbstractController {

    private Button executeButton;
    private Button executeAllButton;
    private Button jobButton;
    private Button exitButton;

    private final MainController mainController;

    protected ButtonController(UILauncher uiLauncher, ApplicationProperties applicationProperties,
                               MainController mainController) {
        super(uiLauncher);
        this.mainController = mainController;
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Button> initButtonMap) {
        executeButton = initButtonMap.get("exitButton");
        executeAllButton = initButtonMap.get("executeAllButton");
        jobButton = initButtonMap.get("jobButton");
        exitButton = initButtonMap.get("exitButton");

        setActions();
    }

    private void setActions() {
        executeButton.setOnAction(executeActionEventHandler());
        executeAllButton.setOnAction(executeAllActionEventHandler());
        jobButton.setOnAction(jobActionEventHandler());
        exitButton.setOnAction(exitActionEventHandler());
    }

    private EventHandler<ActionEvent> executeActionEventHandler() {
        return event -> execute();
    }

    void execute() {
        RunConfig runConfig = mainController.createRunConfigFromUI();
        ToolkitConfig toolkitConfig = mainController.createToolkitConfigFromUI();
        ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(Stream.concat(
                Arrays.stream(runConfig.toArgumentArray()),
                Arrays.stream(toolkitConfig.toArgumentArray())
        ).toArray(String[]::new));

        FXMultiRunner runner = new FXMultiRunner(
                Stream.of(uiAppProperties).collect(toList()),
                uiLauncher.nonUIExecutor(),
                RunType.EXECUTE
        );
        mainController.resetIndicatorProperties(runner);
        uiLauncher.executeOutsideUIThread(() -> {
            runner.start();
            if (uiAppProperties.isActiveTray()) {
                uiLauncher.updateTray(uiAppProperties);
            }
            mainController.updateLastItemUploadDate();
        });
    }

    private EventHandler<ActionEvent> executeAllActionEventHandler() {
        return event -> executeAll();
    }

    void executeAll() {
        RunConfig runConfig = mainController.createRunConfigFromUI();
        ApplicationProperties uiAppProperties = ApplicationPropertiesFactory.getInstance(runConfig.toArgumentArray());
        Map<String, ApplicationProperties> map = CacheManager.getAllApplicationProperties();
        map.put(uiAppProperties.configurationName(), uiAppProperties);

        FXMultiRunner runner = new FXMultiRunner(map.values(), uiLauncher.nonUIExecutor(), RunType.EXECUTE_ALL);
        mainController.resetIndicatorProperties(runner);
        uiLauncher.executeOutsideUIThread(() -> {
            runner.call();
            ApplicationProperties instance = new LinkedList<>(map.values()).getFirst();
            if (instance.isActiveTray()) {
                uiLauncher.updateTray(instance);
            }
        });
    }

    private EventHandler<ActionEvent> jobActionEventHandler() {
        return event -> uiLauncher.showJobWindow();
    }

    private EventHandler<ActionEvent> exitActionEventHandler() {
        return event -> UILauncher.platformExit();
    }

    public void setExecuteDisabled(boolean value) {
        executeButton.setDisable(value);
    }

    public void setExecuteAllDisabled(boolean value) {
        executeAllButton.setDisable(value);
    }

    public void setJobDisabled(boolean value) {
        jobButton.setDisable(value);
    }
}
