package pg.gipter.ui.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.services.GithubService;
import pg.gipter.services.keystore.*;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.ui.*;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.*;

import static java.util.stream.Collectors.joining;

public class MenuSectionController extends AbstractController {

    private MenuItem applicationMenuItem;
    private MenuItem toolkitMenuItem;
    private MenuItem readMeMenuItem;
    private MenuItem instructionMenuItem;
    private MenuItem upgradeMenuItem;
    private MenuItem wizardMenuItem;
    private MenuItem wikiMenuItem;
    private MenuItem importCertMenuItem;
    private MenuItem importCertProgrammaticMenuItem;

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
        readMeMenuItem = controlsMap.get("readMeMenuItem");
        instructionMenuItem = controlsMap.get("instructionMenuItem");
        upgradeMenuItem = controlsMap.get("upgradeMenuItem");
        wizardMenuItem = controlsMap.get("wizardMenuItem");
        wikiMenuItem = controlsMap.get("wikiMenuItem");
        importCertMenuItem = controlsMap.get("importCertMenuItem");
        importCertProgrammaticMenuItem = controlsMap.get("importCertProgrammaticMenuItem");

        setProperties();
        setAccelerators();
        setActions();
    }

    private void setProperties() {
        instructionMenuItem.setDisable(!(Paths.get("Gipter-ui-description.pdf").toFile().exists() && Desktop.isDesktopSupported()));

        setUpgradeMenuItemDisabled();

        final boolean enableImportCert = StringUtils.notEmpty(System.getProperty("java.home")) &&
                applicationProperties.isCertImportEnabled() &&
                CertificateServiceFactory.getInstance(true).hasCertToImport();
        importCertMenuItem.setDisable(!enableImportCert);
        importCertProgrammaticMenuItem.setDisable(!enableImportCert);
    }

    private void setUpgradeMenuItemDisabled() {
        uiLauncher.executeOutsideUIThread(() -> {
            logger.info("Checking new version.");
            GithubService service = new GithubService(applicationProperties.version());
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
        readMeMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN)
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
        importCertMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
        importCertProgrammaticMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN)
        );
    }

    private void setActions() {
        applicationMenuItem.setOnAction(applicationActionEventHandler());
        toolkitMenuItem.setOnAction(toolkitActionEventHandler());
        readMeMenuItem.setOnAction(readMeActionEventHandler());
        instructionMenuItem.setOnAction(instructionActionEventHandler());
        upgradeMenuItem.setOnAction(upgradeActionEventHandler());
        wizardMenuItem.setOnAction(launchWizardActionEventHandler());
        wikiMenuItem.setOnAction(wikiActionEventHandler());
        importCertMenuItem.setOnAction(importCertEventHandler());
        importCertProgrammaticMenuItem.setOnAction(importCertProgrammaticEventHandler());
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

    private EventHandler<ActionEvent> readMeActionEventHandler() {
        return event -> {
            AppManager instance = AppManagerFactory.getInstance();
            instance.launchDefaultBrowser(GithubService.GITHUB_URL + "#gitdiffgenerator");
        };
    }

    private EventHandler<ActionEvent> instructionActionEventHandler() {
        return event -> {
            String pdfFileName = "Gipter-ui-description.pdf";
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.warning.desktopNotSupported"))
                    .withLinkAction(new BrowserLinkAction(applicationProperties.toolkitUserFolder()))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withImageFile(ImageFile.ERROR_CHICKEN_PNG);
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

    private EventHandler<ActionEvent> importCertEventHandler() {
        return actionEvent -> {
            final java.util.List<CertImportResult> certs = CertificateServiceFactory.getInstance(true).automaticImport();
            autoImport(certs);
        };
    }

    private EventHandler<ActionEvent> importCertProgrammaticEventHandler() {
        return actionEvent -> {
            final java.util.List<CertImportResult> certs = CertificateServiceFactory.getInstance(false).automaticImport();
            autoImport(certs);
        };
    }

    private void autoImport(List<CertImportResult> certs) {
        String successMsg = certs.stream()
                .filter(r -> r.getStatus() == CertImportStatus.SUCCESS)
                .map(CertImportResult::getCertName)
                .collect(joining(","));
        String importedMsg = certs.stream()
                .filter(r -> r.getStatus() == CertImportStatus.ALREADY_IMPORTED)
                .map(CertImportResult::getCertName)
                .collect(joining(","));
        String failMsg = certs.stream()
                .filter(r -> r.getStatus() == CertImportStatus.FAILED)
                .map(CertImportResult::getCertName)
                .collect(joining(","));

        String finalMsg = "";
        EnumSet<CertImportStatus> statuses = EnumSet.noneOf(CertImportStatus.class);
        if (StringUtils.notEmpty(successMsg)) {
            finalMsg = BundleUtils.getMsg("certificate.add.success", successMsg) + "\n";
            statuses.add(CertImportStatus.SUCCESS);
        }
        if (StringUtils.notEmpty(successMsg)) {
            finalMsg += BundleUtils.getMsg("certificate.add.failed", failMsg) + "\n";
            statuses.add(CertImportStatus.FAILED);
        }
        if (StringUtils.notEmpty(importedMsg)) {
            finalMsg += BundleUtils.getMsg("certificate.add.exists", importedMsg);
            statuses.add(CertImportStatus.ALREADY_IMPORTED);
        }
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder();
        alertWindowBuilder.withHeaderText(finalMsg)
                .withAlertType(Alert.AlertType.INFORMATION)
                .withImageFile(statuses.containsAll(EnumSet.of(CertImportStatus.SUCCESS)) ?
                        ImageFile.randomSuccessImage() : ImageFile.randomFailImage())
                .buildAndDisplayWindow();
    }
}
