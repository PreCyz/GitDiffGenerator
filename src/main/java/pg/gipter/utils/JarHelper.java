package pg.gipter.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

/** Created by Pawel Gawedzki on 10-Mar-2019.*/
public final class JarHelper {

    private static final String LOGS_FOLDER_NAME = "logs";
    private static final String CERT_FOLDER_NAME = "cert";

    private JarHelper() {
    }

    public static Optional<String> homeDirectoryPath() {
        Optional<File> jarFile = getJarFile();
        return jarFile.map(file -> file.getPath().replace(file.getName(), ""));
    }

    public static Optional<File> getJarFile() {
        try {
            return Optional.of(new File(JarHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
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
