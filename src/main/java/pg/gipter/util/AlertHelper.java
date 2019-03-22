package pg.gipter.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.ui.AbstractController;

import java.io.File;
import java.net.URISyntaxException;

/** Created by Pawel Gawedzki on 10-Mar-2019.*/
public final class AlertHelper {

    public static int LOG_WINDOW = 0;
    public static int BROWSER_WINDOW = 1;

    private static final String LOGS_FOLDER_NAME = "logs";

    private AlertHelper() { }

    public static String logsFolder() {
        try {
            File jarFile = new File(AlertHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getPath().replace(jarFile.getName(), LOGS_FOLDER_NAME);
        } catch (URISyntaxException ex) {
            return "";
        }
    }

    public static void displayWindow(String message, String htmlLink, int windowType, Alert.AlertType alertType) {
        if (Platform.isFxApplicationThread()) {
            buildAndDisplayWindow(message, htmlLink, windowType, alertType);
        } else {
            Platform.runLater(() -> buildAndDisplayWindow(message, htmlLink, windowType, alertType));
        }
    }

    private static void buildAndDisplayWindow(String message, String link, int windowType, Alert.AlertType alertType) {
        Alert alert = buildDefaultAlert(alertType);
        Hyperlink hyperLink = buildHyperlink(link, windowType, alert);
        FlowPane fp = buildFlowPane(message, hyperLink);

        alert.getDialogPane().contentProperty().set(fp);
        AbstractController.setImageOnAlertWindow(alert);
        alert.showAndWait();
    }

    @NotNull
    private static Alert buildDefaultAlert(Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(BundleUtils.getMsg("popup.title"));
        alert.setHeaderText(BundleUtils.getMsg("popup.header.error"));
        return alert;
    }

    @NotNull
    private static Hyperlink buildHyperlink(String link, int windowType, Alert alert) {
        Hyperlink hyperLink = new Hyperlink(link);
        if (windowType == LOG_WINDOW) {
            hyperLink.setOnAction((evt) -> {
                alert.close();
                AppManagerFactory.getInstance().launchFileManagerForLogs();
            });
        } else if (windowType == BROWSER_WINDOW) {
            hyperLink.setOnAction((evt) -> {
                alert.close();
                AppManagerFactory.getInstance().launchDefaultBrowser(link);
            });
        }
        return hyperLink;
    }

    @NotNull
    private static FlowPane buildFlowPane(String message, Hyperlink hyperLink) {
        FlowPane fp = new FlowPane();
        Label lbl = new Label(message);
        fp.getChildren().addAll(lbl, hyperLink);
        return fp;
    }
}
