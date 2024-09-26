package pg.gipter.ui.menu;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.ResourceBundle;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ToolkitSettingsController extends AbstractController {

    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private TextField toolkitUsernameTextField;
    @FXML
    private TextField toolkitFolderNameTextField;
    @FXML
    private TextField toolkitListNameTextField;
    @FXML
    private TextField toolkitUrlTextField;
    @FXML
    private TextField toolkitWSTextField;
    @FXML
    private TextField toolkitUserFolderTextField;
    @FXML
    private Hyperlink toolkitUserFolderHyperlink;

    public ToolkitSettingsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setActions();
        setAccelerators();
    }

    private void setInitValues() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitFolderNameTextField.setText(applicationProperties.toolkitFolderName());
        toolkitListNameTextField.setText(applicationProperties.toolkitCopyListName());
        toolkitUrlTextField.setText(applicationProperties.toolkitHostUrl());
        toolkitWSTextField.setText(applicationProperties.toolkitWSUrl());
        toolkitUserFolderHyperlink.setText(applicationProperties.toolkitUserFolderUrl());
    }

    private void setActions() {
        toolkitUserFolderHyperlink.setOnMouseClicked(mouseClickEventHandler());
    }

    private EventHandler<MouseEvent> mouseClickEventHandler() {
        return event -> Platform.runLater(() -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(applicationProperties.toolkitUserFolderUrl());
            toolkitUserFolderHyperlink.setVisited(false);
        });
    }

    private void setAccelerators() {
        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (KeyCode.ESCAPE == e.getCode()) {
                saveToolkitSettings();
                uiLauncher.closeToolkitWindow();
            } else if (e.isControlDown() && e.getCode() == KeyCode.S) {
                saveToolkitSettings();
                toolkitUserFolderHyperlink.setText(applicationProperties.toolkitUserFolderUrl());
                new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("main.config.changed"))
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImageFile(ImageFile.FINGER_UP_PNG)
                        .buildAndDisplayWindow();
            }
        });
    }

    @Override
    public void executeBeforeClose() {
        saveToolkitSettings();
    }

    private void saveToolkitSettings() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(toolkitUsernameTextField.getText());
        toolkitConfig.setToolkitFolderName(toolkitFolderNameTextField.getText());
        applicationProperties.updateToolkitConfig(toolkitConfig);
        applicationProperties.save();
    }
}
