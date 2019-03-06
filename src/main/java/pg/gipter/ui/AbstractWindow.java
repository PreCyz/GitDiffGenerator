package pg.gipter.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import pg.gipter.util.BundleUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

/**Created by Gawa 2017-10-04*/
public abstract class AbstractWindow {

    private final AbstractController controller;

    protected AbstractWindow(AbstractController controller) {
        this.controller = controller;
    }

    public Parent root() throws IOException {
        FXMLLoader loader = new FXMLLoader(url(), BundleUtils.loadBundle());
        loader.setController(controller);
        return loader.load();
    }

    private URL url() {
        return getClass()
                .getClassLoader()
                .getResource(Paths.get("fxml", fxmlFileName()).toString());
    }

    public String windowImgFilePath() {
        return Paths.get("img", windowImgFileName()).toString();
    }

    public boolean resizable() {
        return false;
    }

    public String css() {
        URL css = getClass()
                .getClassLoader()
                .getResource(Paths.get("css", cssFileName()).toString());
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
