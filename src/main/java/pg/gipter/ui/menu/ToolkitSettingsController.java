package pg.gipter.ui.menu;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManager;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.ResourceBundle;

/** Created by Pawel Gawedzki on 23-Jul-2019. */
public class ToolkitSettingsController extends AbstractController {

    @FXML
    private TextField toolkitUsernameTextField;
    @FXML
    private PasswordField toolkitPasswordField;
    @FXML
    private TextField toolkitDomainTextField;
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

    private ApplicationProperties applicationProperties;

    public ToolkitSettingsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setInitValues();
        setActions();
    }

    private void setInitValues() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
        toolkitDomainTextField.setText(applicationProperties.toolkitDomain());
        toolkitListNameTextField.setText(applicationProperties.toolkitCopyListName());
        toolkitUrlTextField.setText(applicationProperties.toolkitUrl());
        toolkitWSTextField.setText(applicationProperties.toolkitWSUrl());
        toolkitUserFolderHyperlink.setText(applicationProperties.toolkitUserFolder());
    }

    private void setActions() {
        toolkitUserFolderHyperlink.setOnMouseClicked(mouseClickEventHandler());
    }

    @NotNull
    private EventHandler<MouseEvent> mouseClickEventHandler() {
        return event -> Platform.runLater(() -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(applicationProperties.toolkitUserFolder());
            toolkitUserFolderHyperlink.setVisited(false);
        });
    }
}
