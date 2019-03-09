package pg.gipter.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import pg.gipter.util.BundleUtils;

import java.io.IOException;
import java.net.URL;

/**Created by Gawa 2017-10-04*/
public abstract class AbstractWindow {

    private final AbstractController controller;

    protected AbstractWindow(AbstractController controller) {
        this.controller = controller;
    }

    Parent root() throws IOException {
        FXMLLoader loader = new FXMLLoader(url(), BundleUtils.loadBundle());
        loader.setController(controller);
        return loader.load();
    }

    private URL url() {
        return getClass()
                .getClassLoader()
                .getResource(String.format("fxml/%s", fxmlFileName()));
    }

    String windowImgFilePath() {
        return String.format("img/%s", windowImgFileName());
    }

    boolean resizable() {
        return false;
    }

    String css() {
        URL css = getClass()
                .getClassLoader()
                .getResource(String.format("css/%s", cssFileName()));
        String result = "";
        if (css != null) {
            result = css.toExternalForm();
        }
        return result;
    }

    protected abstract String fxmlFileName();
    protected abstract String windowImgFileName();
    protected abstract String cssFileName();
    public abstract String windowTitleBundle();
}
