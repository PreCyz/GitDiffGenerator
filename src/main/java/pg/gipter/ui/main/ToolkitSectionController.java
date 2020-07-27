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
import pg.gipter.utils.JarHelper;

import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class ToolkitSectionController extends AbstractController {

    private final TextField toolkitUsernameTextField;
    private final PasswordField toolkitPasswordField;
    private final TextField toolkitDomainTextField;
    private final Hyperlink verifyCredentialsHyperlink;
    private final ProgressIndicator verifyProgressIndicator;

    ToolkitSectionController(UILauncher uiLauncher,
                             ApplicationProperties applicationProperties,
                             Map<String, Object> controlsMap) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.toolkitUsernameTextField = (TextField)controlsMap.get("toolkitUsernameTextField");
        this.toolkitPasswordField = (PasswordField)controlsMap.get("toolkitPasswordField");
        this.toolkitDomainTextField = (TextField)controlsMap.get("toolkitDomainTextField");
        this.verifyCredentialsHyperlink = (Hyperlink) controlsMap.get("verifyCredentialsHyperlink");
        this.verifyProgressIndicator = (ProgressIndicator) controlsMap.get("verifyProgressIndicator");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
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
                        alertWindowBuilder.withHeaderText(BundleUtils.getMsg("toolkit.panel.credentialsVerified"))
                                .withWindowType(WindowType.CONFIRMATION_WINDOW)
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImage(ImageFile.FINGER_UP_PNG);
                    } else {
                        alertWindowBuilder.withHeaderText(BundleUtils.getMsg("toolkit.panel.credentialsWrong"))
                                .withLink(JarHelper.logsFolder())
                                .withWindowType(WindowType.LOG_WINDOW)
                                .withAlertType(Alert.AlertType.ERROR)
                                .withImage(ImageFile.MINION_IOIO_GIF);
                    }
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

    void setToolkitCredentialsIfAvailable() {
        toolkitUsernameTextField.setText(applicationProperties.toolkitUsername());
        toolkitPasswordField.setText(applicationProperties.toolkitPassword());
    }
}
