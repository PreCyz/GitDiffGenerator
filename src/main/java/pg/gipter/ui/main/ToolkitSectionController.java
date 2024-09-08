package pg.gipter.ui.main;

import javafx.scene.control.*;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

class ToolkitSectionController extends AbstractController {

    private TextField toolkitUsernameTextField;
    private TextField toolkitFolderNameTextField;
    private Label cookieExpiryLabel;
    private ProgressIndicator verifyProgressIndicator;

    ToolkitSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Control> controlsMap) {
        super.initialize(location, resources);
        toolkitUsernameTextField = (TextField) controlsMap.get("toolkitUsernameTextField");
        toolkitFolderNameTextField = (TextField) controlsMap.get("toolkitFolderNameTextField");
        cookieExpiryLabel = (Label) controlsMap.get("cookieExpiryLabel");
        verifyProgressIndicator = (ProgressIndicator) controlsMap.get("verifyProgressIndicator");
        setInitValues();
    }

    private void setInitValues() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitFolderNameTextField.setText(applicationProperties.toolkitFolderName());
        cookieExpiryLabel.setText(BundleUtils.getMsg("toolkit.panel.cookieExpires", CookiesService.expiryDate()));
    }

    ToolkitConfig createToolkitConfigFromUI() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(toolkitUsernameTextField.getText());
        toolkitConfig.setToolkitFolderName(toolkitFolderNameTextField.getText());
        return toolkitConfig;
    }

    void setToolkitCredentialsIfAvailable() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
    }
}
