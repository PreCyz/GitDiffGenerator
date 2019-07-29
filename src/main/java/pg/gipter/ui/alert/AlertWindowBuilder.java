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
import pg.gipter.utils.ResourceUtils;
import pg.gipter.utils.StringUtils;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/** Created by Pawel Gawedzki on 01-Apr-2019. */
public class AlertWindowBuilder {
    private String title;
    private String headerText;
    private String message;
    private String link;
    private WindowType windowType;
    private Alert.AlertType alertType;
    private ImageFile imageFile;
    private String cancelButtonText;
    private String okButtonText;

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

    public AlertWindowBuilder withImage(ImageFile imageFile) {
        this.imageFile = imageFile;
        return this;
    }

    public AlertWindowBuilder withCancelButtonText(String cancelButtonText) {
        this.cancelButtonText = cancelButtonText;
        return this;
    }

    public AlertWindowBuilder withOkButtonText(String okButtonText) {
        this.okButtonText = okButtonText;
        return this;
    }

    public void buildAndDisplayWindow() {
        Alert alert = buildDefaultAlert();
        Hyperlink hyperLink = buildHyperlink(alert);
        FlowPane flowPane = buildFlowPane(hyperLink, windowType);

        alert.getDialogPane().contentProperty().set(flowPane);
        alert.showAndWait();
    }

    @NotNull
    private Alert buildDefaultAlert() {
        Alert alert = new Alert(alertType);
        alert.setTitle(StringUtils.nullOrEmpty(title) ? BundleUtils.getMsg("popup.title") : title);
        alert.setHeaderText(StringUtils.nullOrEmpty(headerText) ? BundleUtils.getMsg("popup.header.error") : headerText);
        Optional<URL> imgUrl = ResourceUtils.getImgResource("chicken-face.png");
        if (imgUrl.isPresent()) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.get().toString()));
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
        hyperLink.setFont(Font.font("Verdana", 14));
        return hyperLink;
    }

    @NotNull
    private FlowPane buildFlowPane(Hyperlink hyperLink, WindowType windowType) {
        FlowPane flowPane = new FlowPane();
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.setVgap(10);
        flowPane.setHgap(100);

        ImageView imageView = null;
        if (imageFile != null) {
            imageView = ResourceUtils.getImgResource(imageFile.fileName())
                    .map(url -> new ImageView(new Image(url.toString())))
                    .orElseGet(ImageView::new);
            flowPane.setPrefWrapLength(imageView.getImage().getWidth());
        }

        List<Node> nodes = new LinkedList<>();

        if (!StringUtils.nullOrEmpty(message)) {
            Label lbl = new Label(message);
            nodes.add(lbl);
        }

        if (hyperLink != null && !StringUtils.nullOrEmpty(hyperLink.getText())) {
            nodes.add(hyperLink);
        }

        if (imageView != null && imageView.getImage() != null) {
            nodes.add(imageView);
        }

        flowPane.getChildren().addAll(nodes);
        return flowPane;
    }

    public boolean buildAndDisplayOverrideWindow() {
        Alert alert = buildDefaultAlert();
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType okButton = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelButtonText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addAll(okButton, cancelButton);

        FlowPane fp = buildFlowPane(new Hyperlink(""), WindowType.OVERRIDE_WINDOW);
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(cancelButton) == okButton;
    }
}
