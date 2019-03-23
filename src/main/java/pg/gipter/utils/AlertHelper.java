package pg.gipter.utils;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.ui.AbstractController;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

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
            buildAndDisplayWindow(message, "https://github.com/precyz", BROWSER_WINDOW, alertType);
        } else {
            Platform.runLater(() -> {
                buildAndDisplayWindow(message, htmlLink, windowType, alertType);
                buildAndDisplayWindow(message, "https://github.com/precyz", BROWSER_WINDOW, alertType);
            });
        }
    }

    private static void buildAndDisplayWindow(String message, String link, int windowType, Alert.AlertType alertType) {
        Alert alert = buildDefaultAlert(alertType);
        Hyperlink hyperLink = buildHyperlink(link, windowType, alert);
        FlowPane fp = buildFlowPane(message, hyperLink);

        alert.getDialogPane().contentProperty().set(fp);
        alert.showAndWait();

    }

    @NotNull
    private static Alert buildDefaultAlert(Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(BundleUtils.getMsg("popup.title"));
        alert.setHeaderText(BundleUtils.getMsg("popup.header.error"));
        URL imgUrl = AbstractController.class.getClassLoader().getResource("img/chicken-face.jpg");
        if (imgUrl != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.toString()));
        }
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

    public static boolean displayAddWindow(String addText, String replaceText, String message) {
        Alert alert = buildDefaultAlert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType addButton = new ButtonType(addText, ButtonBar.ButtonData.OK_DONE);
        ButtonType replaceButton = new ButtonType(replaceText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addAll(addButton, replaceButton);

        FlowPane fp = buildFlowPane(message, new Hyperlink(""));
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(replaceButton) == addButton;
    }

    public static boolean displayOverrideWindow(String createText, String overrideText, String message) {
        Alert alert = buildDefaultAlert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType createButton = new ButtonType(createText, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType overrideButton = new ButtonType(overrideText, ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().addAll(overrideButton, createButton);

        FlowPane fp = buildFlowPane(message, new Hyperlink(""));
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(createButton) == overrideButton ;
    }
}
