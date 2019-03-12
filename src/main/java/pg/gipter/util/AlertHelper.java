package pg.gipter.util;

import javafx.scene.control.Alert;
import pg.gipter.ui.AbstractController;

import java.io.File;
import java.net.URISyntaxException;

/** Created by Pawel Gawedzki on 10-Mar-2019. */
public final class AlertHelper {

    private AlertHelper() {}

    public static String createLogsErrorMessage() {
        String errorMsg;
        try {
            File jarFile = new File(AlertHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String logsDirectory = jarFile.getPath().replace(jarFile.getName(), "logs");
            errorMsg = BundleUtils.getMsg("popup.error.messageWithLog", logsDirectory);
        } catch (URISyntaxException ex) {
            errorMsg = BundleUtils.getMsg("popup.error.messageWithoutLog");
        }
        return errorMsg;
    }

    public static void displayWindow(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(BundleUtils.getMsg("popup.title"));
        alert.setHeaderText(BundleUtils.getMsg("popup.header.error"));
        alert.setContentText(message);

        AbstractController.setImageOnAlertWindow(alert);

        alert.showAndWait();
    }
}
