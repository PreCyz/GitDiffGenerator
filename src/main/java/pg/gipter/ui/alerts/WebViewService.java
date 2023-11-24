package pg.gipter.ui.alerts;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.ui.UploadStatus;
import pg.gipter.utils.ResourceUtils;
import pg.gipter.utils.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class WebViewService {

    private static final Logger logger = LoggerFactory.getLogger(WebViewService.class);
    private static final String URL_PLACEHOLDER = "%URL_PLACEHOLDER%";
    private static final String FIXED_HEIGHT_ALERT = "fixed-height-alert.html";
    private static final String FIXED_WIDTH_ALERT = "fixed-width-alert.html";
    private static final EnumMap<UploadStatus, WebViewDetails> cachedWebViewMap = new EnumMap<>(UploadStatus.class);

    private static class WebViewServiceHolder {
        private static final WebViewService INSTANCE = new WebViewService().init();
    }

    private WebViewService() {
    }

    private WebViewService init() {
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
                    logger.info("Web view loaded gif [{}] from [{}].", wvd.getGif().name(), wvd.getGif().url());
                });
            }
        } else {
            webViewDetails.add(new WebViewDetails(UploadStatus.FAIL, ImageFile.randomFailImage()));
            webViewDetails.add(new WebViewDetails(UploadStatus.PARTIAL_SUCCESS, ImageFile.randomPartialSuccessImage()));
            webViewDetails.add(new WebViewDetails(UploadStatus.SUCCESS, ImageFile.randomSuccessImage()));
        }

        for (WebViewDetails wvd : webViewDetails) {
            cachedWebViewMap.put(wvd.getUploadStatus(), wvd);
        }

        return this;
    }

    public static WebViewService getInstance() {
        return WebViewServiceHolder.INSTANCE;
    }

    private WebViewDetails createWebViewDetails(final Gif gif) {
        final WebViewDetails wvd;
        if (hasInternetConnection()) {
            wvd = new WebViewDetails(gif);
            Platform.runLater(() -> {
                WebView webView = new WebView();
                final WebEngine engine = webView.getEngine();
                engine.loadContent(getHtmlContent(gif), "text/html");
                wvd.setWebView(webView);
                logger.info("New web view was created with gif [{}] from [{}].", gif.name(), gif.url());
            });
        } else {
            ImageFile imageFile = ImageFile.randomImage(EnumSet.allOf(ImageFile.class));
            wvd = new WebViewDetails(UploadStatus.N_A, imageFile);
            logger.info("New web view was created with image [{}] from [{}].",
                    imageFile.name(),
                    imageFile.fileUrl()
            );
        }
        return wvd;
    }

    private String getHtmlContent(Gif gif) {
        String htmlTemplatePath = FIXED_HEIGHT_ALERT;
        if (gif.width() > gif.height()) {
            htmlTemplatePath = FIXED_WIDTH_ALERT;
        }
        try (final InputStream is = ResourceUtils.getHtmlResource(htmlTemplatePath);
             final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             final BufferedReader bufferedReader = new BufferedReader(isr)) {

            String htmlContent = bufferedReader.lines().collect(joining(SystemUtils.lineSeparator()));

            return htmlContent.replaceAll(URL_PLACEHOLDER, gif.url());
        } catch (IOException e) {
            logger.error("Could not load the [{}].", htmlTemplatePath, e);
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

    public WebViewDetails pullWebView(UploadStatus uploadStatus, Gif nextGif) {
        WebViewDetails result = cachedWebViewMap.get(uploadStatus);

        String gifName = Optional.ofNullable(result.getGif())
                .map(Gif::name)
                .orElseGet(result::getResourceUrl);
        logger.info("Web view [{}] pulled from cache.", gifName);

        WebViewDetails webViewDetails = createWebViewDetails(nextGif);
        webViewDetails.setUploadStatus(uploadStatus);
        cachedWebViewMap.put(uploadStatus, webViewDetails);

        return result;
    }

    public WebViewDetails pullWebView(UploadStatus uploadStatus) {
        switch (uploadStatus) {
            case PARTIAL_SUCCESS:
                return pullWebView(uploadStatus, Gif.randomPartialSuccessGif());
            case SUCCESS:
                return pullWebView(uploadStatus, Gif.randomSuccessGif());
            default:
                return pullWebView(uploadStatus, Gif.randomFailGif());
        }
    }

    public WebViewDetails pullFailWebView() {
        return pullWebView(UploadStatus.FAIL, Gif.randomFailGif());
    }

    public WebViewDetails pullPartialSuccessWebView() {
        return pullWebView(UploadStatus.PARTIAL_SUCCESS, Gif.randomPartialSuccessGif());
    }

    public WebViewDetails pullSuccessWebView() {
        return pullWebView(UploadStatus.SUCCESS, Gif.randomSuccessGif());
    }

}
