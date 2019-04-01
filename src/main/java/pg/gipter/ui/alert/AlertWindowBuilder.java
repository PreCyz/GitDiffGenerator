package pg.gipter.ui.alert;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import pg.gipter.platform.AppManagerFactory;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/** Created by Pawel Gawedzki on 01-Apr-2019. */
public class AlertWindowBuilder {
    private String title;
    private String headerText;
    private String message;
    private String link;
    private WindowType windowType;
    private Alert.AlertType alertType;
    private boolean useImage;
    private String createText;
    private String overrideText;

    public AlertWindowBuilder() { }

    public AlertWindowBuilder withTitle(String title) {
        this.headerText = title;
        return this;
    }

    public AlertWindowBuilder withHeaderText(String headerText) {
        this.headerText = headerText;
        return this;
    }

    public AlertWindowBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public AlertWindowBuilder withLink(String link) {
        this.link = link;
        return this;
    }

    public AlertWindowBuilder withWindowType(WindowType windowType) {
        this.windowType = windowType;
        return this;
    }

    public AlertWindowBuilder withAlertType(Alert.AlertType alertType) {
        this.alertType = alertType;
        return this;
    }

    public AlertWindowBuilder withImage() {
        this.useImage = true;
        return this;
    }

    public AlertWindowBuilder withCreateText(String createText) {
        this.createText = createText;
        return this;
    }

    public AlertWindowBuilder withOverrideText(String overrideText) {
        this.overrideText = overrideText;
        return this;
    }

    public void buildAndDisplayWindow() {
        Alert alert = buildDefaultAlert();
        Hyperlink hyperLink = buildHyperlink(alert);
        FlowPane flowPane = buildFlowPane(message, hyperLink, windowType);

        alert.getDialogPane().contentProperty().set(flowPane);
        alert.showAndWait();
    }

    @NotNull
    private Alert buildDefaultAlert() {
        Alert alert = new Alert(alertType);
        alert.setTitle(StringUtils.nullOrEmpty(title) ? BundleUtils.getMsg("popup.title") : title);
        alert.setHeaderText(StringUtils.nullOrEmpty(headerText) ? BundleUtils.getMsg("popup.header.error") : headerText);
        URL imgUrl = AlertWindowBuilder.class.getClassLoader().getResource("img/chicken-face.png");
        if (imgUrl != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.toString()));
        }
        return alert;
    }

    @NotNull
    private Hyperlink buildHyperlink(Alert alert) {
        Hyperlink hyperLink = new Hyperlink(link);
        if (windowType == WindowType.LOG_WINDOW) {
            hyperLink.setOnAction((evt) -> {
                alert.close();
                AppManagerFactory.getInstance().launchFileManagerForLogs();
            });
        } else if (windowType == WindowType.BROWSER_WINDOW) {
            hyperLink.setOnAction((evt) -> {
                alert.close();
                AppManagerFactory.getInstance().launchDefaultBrowser(link);
            });
        }
        hyperLink.setFont(Font.font("Verdana", 12));
        return hyperLink;
    }

    @NotNull
    private FlowPane buildFlowPane(String message, Hyperlink hyperLink, WindowType windowType) {
        FlowPane flowPane = new FlowPane();
        flowPane.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = null;
        if (useImage) {
            String imgResource = "";
            if (windowType == WindowType.LOG_WINDOW) {
                imgResource = "img/error-chicken.png";
            } else if (windowType == WindowType.BROWSER_WINDOW) {
                imgResource = "img/good-job.png";
            } else if (windowType == WindowType.OVERRIDE_WINDOW) {
                imgResource = "img/override.png";
            }
            URL imgUrl = AlertWindowBuilder.class.getClassLoader().getResource(imgResource);
            Image image = new Image(imgUrl.toString());
            imageView = new ImageView(image);
        }

        Label lbl = new Label(message);

        List<Node> nodes = useImage ?
                new LinkedList<>(Arrays.asList(lbl, hyperLink, imageView)) :
                new LinkedList<>(Arrays.asList(lbl, hyperLink));

        flowPane.getChildren().addAll(nodes);
        return flowPane;
    }

    public boolean buildAndDisplayOverrideWindow() {
        Alert alert = buildDefaultAlert();
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType createButton = new ButtonType(createText, ButtonBar.ButtonData.OK_DONE);
        ButtonType overrideButton = new ButtonType(overrideText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addAll(createButton, overrideButton);

        FlowPane fp = buildFlowPane(message, new Hyperlink(""), WindowType.OVERRIDE_WINDOW);
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(createButton) == overrideButton;
    }
}
