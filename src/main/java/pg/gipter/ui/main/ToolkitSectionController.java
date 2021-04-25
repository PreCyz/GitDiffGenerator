package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import pg.gipter.core.*;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.ToolkitService;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;

import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class ToolkitSectionController extends AbstractController {

    private TextField toolkitUsernameTextField;
    private PasswordField toolkitPasswordField;
    private TextField toolkitDomainTextField;
    private Hyperlink verifyCredentialsHyperlink;
    private ProgressIndicator verifyProgressIndicator;

    ToolkitSectionController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, Object> controlsMap) {
        super.initialize(location, resources);
        toolkitUsernameTextField = (TextField)controlsMap.get("toolkitUsernameTextField");
        toolkitPasswordField = (PasswordField)controlsMap.get("toolkitPasswordField");
        toolkitDomainTextField = (TextField)controlsMap.get("toolkitDomainTextField");
        verifyCredentialsHyperlink = (Hyperlink) controlsMap.get("verifyCredentialsHyperlink");
        verifyProgressIndicator = (ProgressIndicator) controlsMap.get("verifyProgressIndicator");
        setInitValues();
        setProperties();
        setActions();
    }

    private void setInitValues() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
        toolkitDomainTextField.setText(applicationProperties.toolkitDomain());
    }

    private void setProperties() {
        toolkitDomainTextField.setEditable(false);
        verifyProgressIndicator.setVisible(false);
    }

    private void setActions() {
        verifyCredentialsHyperlink.setOnMouseClicked(verifyCredentialsHyperlinkOnMouseClickEventHandler());
    }

    ToolkitConfig createToolkitConfigFromUI() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(toolkitUsernameTextField.getText());
        toolkitConfig.setToolkitPassword(toolkitPasswordField.getText());
        return toolkitConfig;
    }

    private EventHandler<MouseEvent> verifyCredentialsHyperlinkOnMouseClickEventHandler() {
        return event -> {
            verifyProgressIndicator.setVisible(true);
            verifyCredentialsHyperlink.setVisited(false);
            Task<Void> task = new Task<>() {
                @Override
                public Void call() {
                    final List<String> arguments = Stream.of(createToolkitConfigFromUI().toArgumentArray()).collect(toList());
                    arguments.add(ArgName.preferredArgSource.name() + "=" + ArgName.preferredArgSource.defaultValue());
                    arguments.add(ArgName.useUI.name() + "=N");
                    final ApplicationProperties instance = ApplicationPropertiesFactory.getInstance(arguments.toArray(String[]::new));
                    boolean hasProperCredentials = new ToolkitService(instance).hasProperCredentials();
                    AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
                    if (hasProperCredentials) {
                        saveToolkitCredentials();
                        alertWindowBuilder.withHeaderText(BundleUtils.getMsg("toolkit.panel.credentialsVerified"))
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImage(ImageFile.FINGER_UP_PNG);
                    } else {
                        alertWindowBuilder.withHeaderText(BundleUtils.getMsg("toolkit.panel.credentialsWrong"))
                                .withLinkAction(new LogLinkAction())
                                .withAlertType(Alert.AlertType.ERROR)
                                .withImage(ImageFile.MINION_IOIO_GIF);
                    }
                    uiLauncher.updateTray(applicationProperties);
                    Platform.runLater(() -> {
                        verifyProgressIndicator.setVisible(false);
                        alertWindowBuilder.buildAndDisplayWindow();
                    });
                    return null;
                }
            };
            uiLauncher.executeOutsideUIThread(task);
        };
    }

    private void saveToolkitCredentials() {
        ToolkitConfig toolkitConfigFromUI = createToolkitConfigFromUI();
        applicationProperties.updateToolkitConfig(toolkitConfigFromUI);
        applicationProperties.save();
    }

    void setToolkitCredentialsIfAvailable() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
    }
}
