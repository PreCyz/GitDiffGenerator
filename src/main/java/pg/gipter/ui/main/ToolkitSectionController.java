package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.services.FXWebService;
import pg.gipter.services.ToolkitService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class ToolkitSectionController extends AbstractController {

    private TextField toolkitUsernameTextField;
    private PasswordField toolkitPasswordField;
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
        toolkitPasswordField = (PasswordField)controlsMap.get("toolkitPasswordField");
        verifyCredentialsHyperlink = (Hyperlink) controlsMap.get("verifyCredentialsHyperlink");
        verifyProgressIndicator = (ProgressIndicator) controlsMap.get("verifyProgressIndicator");
        setInitValues();
        setProperties();
        setActions();
    }

    private void setInitValues() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitSSOPassword());
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
    }

    private void setActions() {
        verifyCredentialsHyperlink.setOnMouseClicked(verifyCredentialsHyperlinkOnMouseClickEventHandler());
    }

    ToolkitConfig createToolkitConfigFromUI() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(toolkitUsernameTextField.getText());
        toolkitConfig.setToolkitSSOPassword(toolkitPasswordField.getText());
        return toolkitConfig;
    }

    private EventHandler<MouseEvent> verifyCredentialsHyperlinkOnMouseClickEventHandler() {
        return event -> {
            verifyProgressIndicator.setVisible(true);
            verifyCredentialsHyperlink.setVisited(false);
            final Task<Void> task = new Task<>() {
                @Override
                public Void call() {
                    final List<String> arguments = Stream.of(createToolkitConfigFromUI().toArgumentArray()).collect(toList());
                    arguments.add(ArgName.preferredArgSource.name() + "=" + ArgName.preferredArgSource.defaultValue());
                    arguments.add(ArgName.useUI.name() + "=N");
                    final ApplicationProperties appProps = ApplicationPropertiesFactory.getInstance(arguments.toArray(String[]::new));
                    Platform.runLater(() -> {
                        boolean hasConnection = false;
                        CookiesService cookiesService = new CookiesService(appProps);
                        if (cookiesService.hasValidFedAuth()) {
                            ToolkitService toolkitService = new ToolkitService(appProps);
                            hasConnection = toolkitService.isCookieWorking(cookiesService.getFedAuthString());
                        }
                        if (!hasConnection) {
                            new FXWebService(appProps).initSSO();
                        }
                        verifyProgressIndicator.setVisible(false);
                    });
                    return null;
                }
            };
            uiLauncher.executeOutsideUIThread(task);
        };
    }

    void setToolkitCredentialsIfAvailable() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitSSOPassword());
    }
}
