package pg.gipter.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**Created by Gawa 2017-10-04*/
public abstract class AbstractWindow {

    private final AbstractController controller;

    protected AbstractWindow(AbstractController controller) {
        this.controller = controller;
    }

    public Parent root() throws IOException {
        BundleUtils.changeBundle(controller.getUiLanguage());
        FXMLLoader loader = new FXMLLoader(url(), BundleUtils.loadBundle());
        loader.setController(controller);
        return loader.load();
    }

    private URL url() {
        return ResourceUtils.getFxmlResource(fxmlFileName()).orElseThrow(IllegalArgumentException::new);
    }

    String windowImgFilePath() {
        return ResourceUtils.getImgResourcePath(windowImgFileName().fileUrl());
    }

    boolean resizable() {
        return false;
    }

    public String css() {
        Optional<URL> css = ResourceUtils.getCssResource(cssFileName());
        String result = "";
        if (css.isPresent()) {
            result = css.get().toExternalForm();
        }
        return result;
    }

    protected abstract String fxmlFileName();
    protected abstract ImageFile windowImgFileName();
    protected abstract String cssFileName();
    public abstract String windowTitleBundle();
}
