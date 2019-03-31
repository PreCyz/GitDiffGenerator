package pg.gipter.utils;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManagerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

/** Created by Pawel Gawedzki on 10-Mar-2019.*/
public final class AlertHelper {

    public static int LOG_WINDOW = 0;
    public static int BROWSER_WINDOW = 1;
    private static int OVERRIDE_WINDOW = 2;

    private static final String LOGS_FOLDER_NAME = "logs";

    private AlertHelper() {
    }

    public static Optional<String> homeDirectoryPath() {
        Optional<File> jarFile = getJarFile();
        return jarFile.map(file -> file.getPath().replace(file.getName(), ""));
    }

    public static Optional<File> getJarFile() {
        try {
            return Optional.of(new File(AlertHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    public static String logsFolder() {
        return homeDirectoryPath().map(s -> s + LOGS_FOLDER_NAME).orElse("");
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
        FlowPane flowPane = buildFlowPane(message, hyperLink, windowType);

        alert.getDialogPane().contentProperty().set(flowPane);
        alert.showAndWait();

    }

    @NotNull
    private static Alert buildDefaultAlert(Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(BundleUtils.getMsg("popup.title"));
        alert.setHeaderText(BundleUtils.getMsg("popup.header.error"));
        URL imgUrl = AlertHelper.class.getClassLoader().getResource("img/chicken-face.jpg");
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
    private static FlowPane buildFlowPane(String message, Hyperlink hyperLink, int windowType) {
        FlowPane flowPane = new FlowPane();
        String imgResource = "";
        if (windowType == LOG_WINDOW) {
            imgResource = "img/error-chicken.png";
        } else if (windowType == BROWSER_WINDOW) {
            imgResource = "img/good-job.png";
        }
        URL imgUrl = AlertHelper.class.getClassLoader().getResource(imgResource);
        Image image = new Image(imgUrl.toString());
        ImageView imageView = new ImageView(image);
        Label lbl = new Label(message);
        flowPane.getChildren().addAll(lbl, hyperLink, imageView);
        return flowPane;
    }

    public static boolean displayOverrideWindow(String createText, String overrideText, String message) {
        Alert alert = buildDefaultAlert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType createButton = new ButtonType(createText, ButtonBar.ButtonData.OK_DONE);
        ButtonType overrideButton = new ButtonType(overrideText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addAll(createButton, overrideButton);

        FlowPane fp = buildFlowPane(message, new Hyperlink(""), OVERRIDE_WINDOW);
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(createButton) == overrideButton;
    }
}
