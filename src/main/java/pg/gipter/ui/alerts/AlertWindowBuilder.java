package pg.gipter.ui.alerts;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.UploadResult;
import pg.gipter.ui.alerts.controls.CustomControl;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.ResourceUtils;
import pg.gipter.utils.StringUtils;
import pg.gipter.utils.SystemUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/** Created by Pawel Gawedzki on 01-Apr-2019. */
public class AlertWindowBuilder {
    private String headerText;
    private String message;
    private Set<AbstractLinkAction> linkActions;
    private Alert.AlertType alertType;
    private WebViewDetails webViewDetails;
    private String cancelButtonText;
    private String okButtonText;
    private Map<String, UploadResult> msgResultMap;
    private int gridPaneRow;
    private CustomControl customControl;

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

    public AlertWindowBuilder withWebViewDetails(WebViewDetails webViewDetails) {
        this.webViewDetails = webViewDetails;
        return this;
    }

    public AlertWindowBuilder withImageFile(ImageFile imageFile) {
        this.webViewDetails = new WebViewDetails(WebViewService.getInstance().createImageView(imageFile));
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

    public AlertWindowBuilder withCustomControl(CustomControl customControl) {
        this.customControl = customControl;
        return this;
    }

    public void buildAndDisplayWindow() {
        Alert alert = buildDefaultAlert();
        List<Hyperlink> hyperLinks = buildHyperlinks(alert);
        GridPane gridPane = buildGridPane(hyperLinks, alert);

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
            Hyperlink hyperLink = new Hyperlink(linkAction.getText().isEmpty() ? linkAction.getLink() : linkAction.getText());
            hyperLink.setOnAction((evt) -> {
                alert.close();
                linkAction.run();
            });
            hyperLink.setFont(Font.font("Verdana", 13));
            hyperlinks.add(hyperLink);
        }
        return hyperlinks;
}

    private GridPane buildGridPane(List<Hyperlink> hyperLinks, Alert alert) {
        gridPaneRow = 0;

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        double preferredWidth = addHyperLinks(hyperLinks, gridPane);

        final double customControlWidth = addCustomControl(gridPane, alert);
        preferredWidth = Math.max(preferredWidth, customControlWidth);

        final double labelWidth = addLabels(gridPane);
        preferredWidth = Math.max(preferredWidth, labelWidth);

        final double imageWidth = addImageView(gridPane);
        preferredWidth = Math.max(preferredWidth, imageWidth);

        final double gifWidth = addWebView(gridPane);
        preferredWidth = Math.max(preferredWidth, gifWidth);

        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setHalignment(HPos.CENTER);
        columnConstraint.setPrefWidth(preferredWidth);
        gridPane.getColumnConstraints().add(columnConstraint);

        return gridPane;
    }

    private double addLabels(GridPane gridPane) {
        double preferredWidth = 0;
        final double pixelsPerLetterFactor = 10; //depends on font size
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

                final String[] lines = entry.getValue().logMsg().split(SystemUtils.lineSeparator());

                preferredWidth = pixelsPerLetterFactor * Arrays.stream(lines)
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

            double messageWidth = pixelsPerLetterFactor * Arrays.stream(message.split(SystemUtils.lineSeparator()))
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

    private double addCustomControl(GridPane gridPane, Alert alert) {
        double preferredWidth = 0;
        if (customControl != null) {
            final UILauncher uiLauncher = customControl.getUiLauncher();
            EventHandler<ActionEvent> eventHandler = event -> {
                alert.close();
                Platform.runLater(() -> {
                    uiLauncher.hideMainWindow();
                    uiLauncher.showUpgradeWindow();
                });
            };
            Control control = customControl.create(eventHandler);

            gridPane.add(control, 0, gridPaneRow++);
            preferredWidth = Math.max(preferredWidth, control.getWidth());
            return preferredWidth;
        }
        return preferredWidth;
    }

    private double addImageView(GridPane gridPane) {
        double preferredWidth = 0;
        if (webViewDetails != null && webViewDetails.getImageView() != null) {
            gridPane.add(webViewDetails.getImageView(), 0, gridPaneRow++);
            preferredWidth = webViewDetails.calculateWidth();
        }
        return preferredWidth;
    }

    private double addWebView(GridPane gridPane) {
        double preferredWidth = 0;
        if (webViewDetails != null && webViewDetails.getWebView() != null) {
            final int maxSize = 600;
            final int cropSize = 580;
            final int margin = 10;

            VBox vBox = new VBox(webViewDetails.getWebView());
            vBox.setPrefHeight(maxSize);
            vBox.setPrefWidth(maxSize);
            if (webViewDetails.calculateWidth() > webViewDetails.calculateHeight() && webViewDetails.calculateWidth() > maxSize) {
                double ratio = (webViewDetails.calculateWidth() - cropSize) / webViewDetails.calculateWidth();
                double height = margin + webViewDetails.calculateHeight() - webViewDetails.calculateHeight() * ratio;
                vBox.setPrefHeight(height);
            } else if (webViewDetails.calculateHeight() > webViewDetails.calculateWidth() && webViewDetails.calculateHeight() > maxSize) {
                double ratio = (webViewDetails.calculateHeight() - cropSize) / webViewDetails.calculateHeight();
                double width = margin + webViewDetails.calculateWidth() - webViewDetails.calculateWidth() * ratio;
                vBox.setPrefWidth(width);
            } else if (webViewDetails.calculateHeight() < webViewDetails.calculateWidth() && webViewDetails.calculateWidth() < cropSize) {
                double ratio = (cropSize - webViewDetails.calculateWidth()) / webViewDetails.calculateWidth();
                double height = margin + webViewDetails.calculateHeight() + ratio * webViewDetails.calculateHeight();
                vBox.setPrefHeight(height);
            } else if (webViewDetails.calculateWidth() < webViewDetails.calculateHeight() && webViewDetails.calculateHeight() < cropSize) {
                double ratio = (cropSize - webViewDetails.calculateHeight()) / webViewDetails.calculateHeight();
                double width = margin + webViewDetails.calculateWidth() + ratio * webViewDetails.calculateWidth();
                vBox.setPrefWidth(width);
            }

            gridPane.add(vBox, 0, gridPaneRow++);
            preferredWidth = maxSize;
        }
        return preferredWidth;
    }

    public boolean buildAndDisplayOverrideWindow() {
        Alert alert = buildDefaultAlert();
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType okButton = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelButtonText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addAll(okButton, cancelButton);

        GridPane fp = buildGridPane(Collections.singletonList(new Hyperlink("")), alert);
        alert.getDialogPane().contentProperty().set(fp);

        return alert.showAndWait().orElse(cancelButton) == okButton;
    }
}
