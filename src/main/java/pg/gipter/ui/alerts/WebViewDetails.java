package pg.gipter.ui.alerts;

import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import pg.gipter.ui.UploadStatus;

public class WebViewDetails {
    private UploadStatus uploadStatus;
    private Gif gif;
    private WebView webView;
    private ImageView imageView;

    private WebViewDetails(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public WebViewDetails(UploadStatus uploadStatus, Gif gif) {
        this(uploadStatus);
        this.gif = gif;
    }

    public WebViewDetails(ImageView imageView) {
        this(UploadStatus.N_A, imageView);
    }

    public WebViewDetails(Gif gif) {
        this(UploadStatus.N_A, gif);
    }

    public WebViewDetails(UploadStatus uploadStatus, ImageView imageView) {
        this(uploadStatus);
        this.imageView = imageView;
    }

    public Gif getGif() {
        return gif;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public double calculateHeight() {
        if (gif != null) {
            return gif.height();
        } else if (imageView != null && imageView.getImage() != null) {
            return imageView.getImage().getHeight();
        }
        return 0;
    }

    public double calculateWidth() {
        if (gif != null) {
            return gif.width();
        } else if (imageView != null && imageView.getImage() != null) {
            return imageView.getImage().getWidth();
        }
        return 0;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getResourceUrl() {
        if (gif != null) {
            return gif.url();
        } else if (imageView != null && imageView.getImage() != null) {
            return imageView.getImage().impl_getUrl();
        }
        return "N/A";
    }
}
