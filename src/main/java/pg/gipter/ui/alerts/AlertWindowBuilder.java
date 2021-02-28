package pg.gipter.ui.alerts;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.utils.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**
 * Created by Pawel Gawedzki on 01-Apr-2019.
 */
public class AlertWindowBuilder {
    private String title;
    private String headerText;
    private String message;
    private Set<String> links;
    private WindowType windowType;
    private Alert.AlertType alertType;
    private ImageFile imageFile;
    private String cancelButtonText;
    private String okButtonText;

    public AlertWindowBuilder() {
    }

    public AlertWindowBuilder withHeaderText(String headerText) {
        this.headerText = headerText;
        return this;
    }

    public AlertWindowBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public AlertWindowBuilder withLink(String... links) {
        this.links = Stream.of(links).collect(toCollection(LinkedHashSet::new));
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
        List<Hyperlink> hyperLinks = buildHyperlinks(alert);
        GridPane gridPane = buildGridPane(hyperLinks);

        alert.getDialogPane().contentProperty().set(gridPane);
        alert.showAndWait();
    }

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

    private List<Hyperlink> buildHyperlinks(Alert alert) {
        List<Hyperlink> hyperlinks = new LinkedList<>();
        for (String link : links) {
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
            hyperlinks.add(hyperLink);

        }
        return hyperlinks;
    }

    private GridPane buildGridPane(List<Hyperlink> hyperLinks) {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        double preferredWidth = 0;
        int row = 0;

        if (!StringUtils.nullOrEmpty(message)) {
            Label messageLabel = new Label(message);
            double pixelsPerLetter = 5.3; //depends on font size
            preferredWidth = pixelsPerLetter * Arrays.stream(message.split("\n"))
                    .map(String::length)
                    .max((o1, o2) -> o1 > o2 ? o1 : o2)
                    .orElseGet(() -> 0);
            gridPane.add(messageLabel, 0, row++);
        }

        for (Hyperlink hyperLink : hyperLinks) {
            int pixelsPerLetter = 8; //depends on font size
            preferredWidth = Math.max(preferredWidth, pixelsPerLetter * hyperLink.getText().length());
            gridPane.add(hyperLink, 0, row++);
        }

        if (imageFile != null) {
            ImageView imageView = ResourceUtils.getImgResource(imageFile.fileUrl())
                    .map(url -> new ImageView(new Image(url.toString())))
                    .orElseGet(ImageView::new);
            preferredWidth = Math.max(preferredWidth, imageView.getImage().getWidth());
            gridPane.add(imageView, 0, row);
        }

        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setHalignment(HPos.CENTER);
        columnConstraint.setPrefWidth(preferredWidth);
        gridPane.getColumnConstraints().add(columnConstraint);

        return gridPane;
    }

    public boolean buildAndDisplayOverrideWindow() {
        Alert alert = buildDefaultAlert();
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType okButton = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelButtonText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addAll(okButton, cancelButton);

        GridPane fp = buildGridPane(Collections.singletonList(new Hyperlink("")));
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(cancelButton) == okButton;
    }
}
