package pg.gipter.ui.alerts;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.ui.UploadStatus;
import pg.gipter.utils.ResourceUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class WebViewService {

    private static final Logger logger = LoggerFactory.getLogger(WebViewService.class);
    private static final String URL_PLACEHOLDER = "%URL_PLACEHOLDER%";
    public static final String CUSTOM_ALERT_HTML = "custom-alert.html";
    private String htmlContent;

    public List<WebViewDetails> loadGifs() {
        LinkedList<WebViewDetails> webViewDetails = new LinkedList<>();

        if (hasInternetConnection()) {
            webViewDetails.add(new WebViewDetails(UploadStatus.FAIL, Gif.randomFailGif()));
            webViewDetails.add(new WebViewDetails(UploadStatus.PARTIAL_SUCCESS, Gif.randomPartialSuccessGif()));
            webViewDetails.add(new WebViewDetails(UploadStatus.SUCCESS, Gif.randomSuccessGif()));

            for (WebViewDetails wvd : webViewDetails) {
                Platform.runLater(() -> {
                    WebView webView = new WebView();
                    final WebEngine engine = webView.getEngine();
                    engine.loadContent(getHtmlContent(wvd.getGif()), "text/html");
                    wvd.setWebView(webView);
                    logger.info("Web view loaded the gif from [{}].", wvd.getGif().url());
                });
            }
        } else {
            webViewDetails.add(new WebViewDetails(UploadStatus.FAIL, createImageView(ImageFile.randomFailImage())));
            webViewDetails.add(new WebViewDetails(UploadStatus.PARTIAL_SUCCESS, createImageView(ImageFile.randomPartialSuccessImage())));
            webViewDetails.add(new WebViewDetails(UploadStatus.SUCCESS, createImageView(ImageFile.randomSuccessImage())));
        }

        return webViewDetails;
    }

    public ImageView createImageView(ImageFile imageFile) {
        return ResourceUtils.getImgResource(imageFile.fileUrl())
                .map(url -> new ImageView(new Image(url.toString())))
                .orElseGet(ImageView::new);
    }

    private String getHtmlContent(Gif gif) {
        try (final InputStream is = ResourceUtils.getHtmlResource(CUSTOM_ALERT_HTML);
             final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             final BufferedReader bufferedReader = new BufferedReader(isr)) {

            if (htmlContent == null) {
                htmlContent = bufferedReader.lines()
                        .collect(joining(System.getProperty("line.separator")))
                        .replaceAll(URL_PLACEHOLDER, gif.url());
            }
            return htmlContent;
        } catch (IOException e) {
            logger.error("Could not load the [{}].", CUSTOM_ALERT_HTML, e);
            return "";
        }
    }

    private boolean hasInternetConnection() {
        try {
            new URL(Gif.randomFailGif().url()).openConnection().connect();
            return true;
        } catch (IOException e) {
            logger.error("No Internet connection", e);
            return false;
        }
    }

}
