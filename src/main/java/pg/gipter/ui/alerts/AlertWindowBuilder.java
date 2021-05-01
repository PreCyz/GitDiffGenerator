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
import pg.gipter.ui.UploadResult;
import pg.gipter.utils.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/** Created by Pawel Gawedzki on 01-Apr-2019. */
public class AlertWindowBuilder {
    private String headerText;
    private String message;
    private Set<AbstractLinkAction> linkActions;
    private Alert.AlertType alertType;
    private ImageFile imageFile;
    private String cancelButtonText;
    private String okButtonText;
    private Map<String, UploadResult> msgResultMap;
    private int gridPaneRow;

    public AlertWindowBuilder() {
        linkActions = Collections.emptySet();
    }

    public AlertWindowBuilder withHeaderText(String headerText) {
        this.headerText = headerText;
        return this;
    }

    public AlertWindowBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public AlertWindowBuilder withUploadResultMap(Map<String, UploadResult> msgResultMap) {
        this.msgResultMap = new LinkedHashMap<>(msgResultMap);
        return this;
    }

    public AlertWindowBuilder withLinkAction(AbstractLinkAction... linkActions) {
        this.linkActions = Stream.of(linkActions).collect(toCollection(LinkedHashSet::new));
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
        alert.setTitle(BundleUtils.getMsg("popup.title"));
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
        for (AbstractLinkAction linkAction : linkActions) {
            Hyperlink hyperLink = new Hyperlink(linkAction.getLink());
            hyperLink.setOnAction((evt) -> {
                alert.close();
                linkAction.run();
            });
            hyperLink.setFont(Font.font("Verdana", 13));
            hyperlinks.add(hyperLink);
        }
        return hyperlinks;
}

    private GridPane buildGridPane(List<Hyperlink> hyperLinks) {
        gridPaneRow = 0;

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        double preferredWidth = addHyperLinks(hyperLinks, gridPane);

        final double labelWidth = addLabels(gridPane);
        preferredWidth = Math.max(preferredWidth, labelWidth);

        final double imageWidth = addImageFile(gridPane);
        preferredWidth = Math.max(preferredWidth, imageWidth);

        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setHalignment(HPos.CENTER);
        columnConstraint.setPrefWidth(preferredWidth);
        gridPane.getColumnConstraints().add(columnConstraint);

        return gridPane;
    }

    private double addLabels(GridPane gridPane) {
        double preferredWidth = 0;
        final double pixelsPerLetterFactor = 16; //depends on font size
        final double fontSize = 16;

        if (msgResultMap != null && !msgResultMap.isEmpty()) {
            for (Map.Entry<String, UploadResult> entry : msgResultMap.entrySet()) {
                Label messageLabel = new Label(entry.getValue().logMsg());
                String style = "-fx-font-family:monospace; " +
                        String.format("-fx-font-size:%spx; ", fontSize) +
                        "-fx-text-alignment:left; " +
                        "-fx-font-style:normal; " +
                        "-fx-font-weight:bolder; ";
                if (entry.getValue().getSuccess()) {
                    style += "-fx-text-fill:forestgreen;";
                } else {
                    style += "-fx-text-fill:crimson;";
                }
                messageLabel.setStyle(style);

                preferredWidth = pixelsPerLetterFactor * Arrays.stream(entry.getValue().logMsg().split(System.getProperty("line.separator")))
                        .map(String::length)
                        .max((o1, o2) -> o1 > o2 ? o1 : o2)
                        .orElseGet(() -> 0);
                gridPane.add(messageLabel, 0, gridPaneRow++);
            }
        }

        if (!StringUtils.nullOrEmpty(message)) {
            Label messageLabel = new Label(message);
            String style = "-fx-font-family:monospace; " +
                    String.format("-fx-font-size:%spx; ", fontSize) +
                    "-fx-text-alignment:left; " +
                    "-fx-font-style:normal; ";
            if (alertType == Alert.AlertType.ERROR) {
                style += "-fx-font-weight:bolder; " +
                        "-fx-text-fill:crimson;";
            } else {
                style += "-fx-text-fill:mediumblue;";
            }
            messageLabel.setStyle(style);
            gridPane.add(messageLabel, 0, gridPaneRow++);

            double messageWidth = pixelsPerLetterFactor * Arrays.stream(message.split(System.getProperty("line.separator")))
                    .map(String::length)
                    .max((o1, o2) -> o1 > o2 ? o1 : o2)
                    .orElseGet(() -> 0);
            preferredWidth = Math.max(preferredWidth, messageWidth);
        }

        return preferredWidth;
    }

    private double addHyperLinks(List<Hyperlink> hyperLinks, GridPane gridPane) {
        double preferredWidth = 0;
        for (Hyperlink hyperLink : hyperLinks) {
            gridPane.add(hyperLink, 0, gridPaneRow++);
            final int pixelsPerLetterFactor = 8; //depends on font size
            preferredWidth = Math.max(preferredWidth, pixelsPerLetterFactor * hyperLink.getText().length());
        }
        return preferredWidth;
    }

    private double addImageFile(GridPane gridPane) {
        double preferredWidth = 0;
        if (imageFile != null) {
            ImageView imageView = ResourceUtils.getImgResource(imageFile.fileUrl())
                    .map(url -> new ImageView(new Image(url.toString())))
                    .orElseGet(ImageView::new);
            gridPane.add(imageView, 0, gridPaneRow++);
            preferredWidth = imageView.getImage().getWidth();
        }
        return preferredWidth;
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
