package pg.gipter.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

/** Created by Pawel Gawedzki on 10-Mar-2019.*/
public final class AlertHelper {

    private static final String LOGS_FOLDER_NAME = "logs";

    private AlertHelper() {
    }

    public static Optional<String> homeDirectoryPath() {
        Optional<File> jarFile = getJarFile();
        return jarFile.map(file -> file.getPath().replace(file.getName(), ""));
    }

    public static Optional<File> getJarFile() {
        try {
            return Optional.of(new File(AlertHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    public static String logsFolder() {
        return homeDirectoryPath().map(s -> s + LOGS_FOLDER_NAME).orElse("");
    }
}
