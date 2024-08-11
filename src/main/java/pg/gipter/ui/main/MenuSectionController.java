package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.services.GithubService;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.ui.*;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.ResourceBundle;

public class MenuSectionController extends AbstractController {

    private MenuItem applicationMenuItem;
    private MenuItem toolkitMenuItem;
    private MenuItem instructionMenuItem;
    private MenuItem upgradeMenuItem;
    private MenuItem wizardMenuItem;
    private MenuItem wikiMenuItem;

    private final MainController mainController;

    protected MenuSectionController(UILauncher uiLauncher,
                                    ApplicationProperties applicationProperties,
                                    MainController mainController) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        this.mainController = mainController;
    }

    public void initialize(URL location, ResourceBundle resources, Map<String, MenuItem> controlsMap) {
        super.initialize(location, resources);

        applicationMenuItem = controlsMap.get("applicationMenuItem");
        toolkitMenuItem = controlsMap.get("toolkitMenuItem");
        instructionMenuItem = controlsMap.get("instructionMenuItem");
        upgradeMenuItem = controlsMap.get("upgradeMenuItem");
        wizardMenuItem = controlsMap.get("wizardMenuItem");
        wikiMenuItem = controlsMap.get("wikiMenuItem");

        setProperties();
        setAccelerators();
        setActions();
    }

    private void setProperties() {
        instructionMenuItem.setDisable(!(Paths.get("Gipter-ui-description.pdf").toFile().exists() && Desktop.isDesktopSupported()));

        setUpgradeMenuItemDisabled();
    }

    private void setUpgradeMenuItemDisabled() {
        uiLauncher.executeOutsideUIThread(() -> {
            logger.info("Checking new version.");
            GithubService service = new GithubService(applicationProperties.version(), applicationProperties.githubToken());
            final boolean newVersion = service.isNewVersion();
            if (newVersion) {
                logger.info("New version [{}] available.", service.getServerVersion());
            } else {
                logger.info("This version is up to date.");
            }
            Platform.runLater(() -> upgradeMenuItem.setDisable(!newVersion));
        });
    }

    private void setAccelerators() {
        applicationMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
        toolkitMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
        upgradeMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
        instructionMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
        wizardMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
        wikiMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
    }

    private void setActions() {
        applicationMenuItem.setOnAction(applicationActionEventHandler());
        toolkitMenuItem.setOnAction(toolkitActionEventHandler());
        instructionMenuItem.setOnAction(instructionActionEventHandler());
        upgradeMenuItem.setOnAction(upgradeActionEventHandler());
        wizardMenuItem.setOnAction(launchWizardActionEventHandler());
        wikiMenuItem.setOnAction(wikiActionEventHandler());
    }

    private EventHandler<ActionEvent> applicationActionEventHandler() {
        return event -> uiLauncher.showApplicationSettingsWindow();
    }

    private EventHandler<ActionEvent> toolkitActionEventHandler() {
        return event -> {
            uiLauncher.setApplicationProperties(applicationProperties);
            uiLauncher.showToolkitSettingsWindow();
        };
    }

    private EventHandler<ActionEvent> instructionActionEventHandler() {
        return event -> {
            String pdfFileName = "Gipter-ui-description.pdf";
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withMessage(BundleUtils.getMsg("popup.warning.desktopNotSupported"))
                    .withLinkAction(new BrowserLinkAction(applicationProperties.toolkitUserFolderUrl()))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
            try {
                Path pdfFile = Paths.get(pdfFileName);
                if (Files.exists(pdfFile)) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile.toFile());
                    } else {
                        logger.error("AWT Desktop is not supported by the platform.");
                        Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
                    }
                }
            } catch (IOException e) {
                logger.error("Could not find [{}] file with instructions.", pdfFileName, e);
                Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
            }
        };
    }

    private EventHandler<ActionEvent> upgradeActionEventHandler() {
        return event -> {
            uiLauncher.hideMainWindow();
            uiLauncher.showUpgradeWindow();
        };
    }

    private EventHandler<ActionEvent> launchWizardActionEventHandler() {
        return event -> {
            uiLauncher.hideMainWindow();
            new WizardLauncher(uiLauncher.currentWindow(), mainController.getConfigurationNameComboBoxValue()).execute();
        };
    }

    private EventHandler<ActionEvent> wikiActionEventHandler() {
        return event -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(GithubService.GITHUB_URL + "/wiki");
        };
    }
}
