package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.services.FXWebService;
import pg.gipter.services.ToolkitService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

class ToolkitSectionController extends AbstractController {

    private TextField toolkitUsernameTextField;
    private Hyperlink verifyCredentialsHyperlink;
    private ProgressIndicator verifyProgressIndicator;

    private static AtomicBoolean connectionCheckInProgress;

    ToolkitSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Control> controlsMap) {
        super.initialize(location, resources);
        toolkitUsernameTextField = (TextField)controlsMap.get("toolkitUsernameTextField");
        verifyCredentialsHyperlink = (Hyperlink) controlsMap.get("verifyCredentialsHyperlink");
        verifyProgressIndicator = (ProgressIndicator) controlsMap.get("verifyProgressIndicator");
        setInitValues();
        setProperties();
        setActions();
    }

    private void setInitValues() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
    }

    private void setProperties() {
        verifyProgressIndicator.setVisible(false);
        if (connectionCheckInProgress == null) {
            connectionCheckInProgress = new AtomicBoolean(true);
        }
        if (!connectionCheckInProgress.get()) {
            connectionCheckInProgress.set(true);
            uiLauncher.executeOutsideUIThread(() -> {
                final boolean hasConnection = applicationProperties.hasConnectionToToolkit();
                Platform.runLater(() -> verifyCredentialsHyperlink.setVisible(!hasConnection));
                connectionCheckInProgress.set(false);
            });
        }
        boolean showVerifyHyperLink = !(CookiesService.hasValidFedAuth() &&
                new ToolkitService(applicationProperties).isCookieWorking(CookiesService.getFedAuthString()));
        verifyCredentialsHyperlink.setVisible(showVerifyHyperLink);
    }

    private void setActions() {
        verifyCredentialsHyperlink.setOnMouseClicked(verifyCredentialsHyperlinkOnMouseClickEventHandler());
    }

    ToolkitConfig createToolkitConfigFromUI() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(toolkitUsernameTextField.getText());
        return toolkitConfig;
    }

    private EventHandler<MouseEvent> verifyCredentialsHyperlinkOnMouseClickEventHandler() {
        return event -> {
            verifyCredentialsHyperlink.setVisited(false);
            verifyProgressIndicator.setVisible(false);
            verifyCredentialsHyperlink.setVisible(false);
            new FXWebService().initSSO();
        };
    }

    void setToolkitCredentialsIfAvailable() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
    }
}
