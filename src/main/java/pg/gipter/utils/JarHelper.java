package pg.gipter.utils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/** Created by Pawel Gawedzki on 10-Mar-2019.*/
public final class JarHelper {

    private static final String LOGS_FOLDER_NAME = "logs";
    private static final String CERT_FOLDER_NAME = "cert";

    private JarHelper() {
    }

    public static Optional<String> homeDirectoryPath() {
        Optional<Path> jarFile = getJarPath();
        return jarFile.map(path -> path.toString().replace(path.getFileName().toString(), ""));
    }

    public static Optional<Path> getJarPath() {
        try {
            return Optional.of(Paths.get(JarHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    public static String logsFolder() {
        return homeDirectoryPath().map(s -> s + LOGS_FOLDER_NAME).orElse("");
    }

    public static String certFolder() {
        return homeDirectoryPath().map(s -> s + CERT_FOLDER_NAME).orElse("");
    }
}
