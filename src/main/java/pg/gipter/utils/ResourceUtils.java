package pg.gipter.utils;

import java.net.URL;
import java.util.Optional;

public final class ResourceUtils {

    private ResourceUtils() { }

    public static Optional<URL> getImgResource(String resourceName) {
        return Optional.ofNullable(getResource("img", resourceName));
    }

    private static URL getResource(String resourceFolder, String resourceName) {
        return ResourceUtils.class.getClassLoader().getResource(getResourcePath(resourceFolder, resourceName));
    }

    private static String getResourcePath(String resourceFolder, String resourceName) {
        return String.format("%s/%s", resourceFolder, resourceName);
    }

    public static String getImgResourcePath(String resourceName) {
        return getResourcePath("img", resourceName);
    }

    public static Optional<URL> getFxmlResource(String resourceName) {
        return Optional.ofNullable(getResource("fxml", resourceName));
    }

    public static Optional<URL> getCssResource(String resourceName) {
        return Optional.ofNullable(getResource("css", resourceName));
    }
}
