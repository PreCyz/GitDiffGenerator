package pg.gipter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.Properties;

/** Created by Pawel Gawedzki on 16-Sep-2019. */
public final class SystemUtils {

    private SystemUtils() { }

    public static final String OS = osName().toLowerCase();
    private static final String OS_LOWER_CASE = osName().toLowerCase();

    public static boolean isWindows() {
        return OS_LOWER_CASE.contains("win");
    }

    public static boolean isMac() {
        return OS_LOWER_CASE.contains("mac");
    }

    public static boolean isUnix() {
        return OS_LOWER_CASE.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static boolean isSolaris() {
        return (OS_LOWER_CASE.contains("sunos"));
    }

    public static String lineSeparator() {
        return System.lineSeparator();
    }

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static String javaHome() {
        return System.getProperty("java.home");
    }

    public static String userName() {
        return System.getProperty("user.name");
    }

    public static String osName() {
        return System.getProperty("os.name");
    }

    public static String processorArchitecture() {
        return System.getProperty("processor.architecture");
    }

    public static boolean isExe() {
        return Files.exists(Path.of(".", "Gipter.exe"));
    }

    public static boolean isCustomRuntime() {
        return SystemUtils.javaHome().endsWith("runtime");
    }

    public static String tmp() {
        return Optional.ofNullable(
                Optional.ofNullable(System.getenv("TMP")).orElseGet(() -> System.getenv("TEMP"))
                ).orElseThrow(() -> new IllegalArgumentException("Cannot find TMP directory"));
    }

    public static boolean isPortable() {
        if (isCustomRuntime()) {
            Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(Paths.get(SystemUtils.javaHome(), "release"))) {
                properties.load(is);
                return properties.getProperty("IMAGE_TYPE", "").toUpperCase().contains("JRE");
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }

    public static boolean isMsi() {
        if (isCustomRuntime()) {
            Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(Paths.get(SystemUtils.javaHome(), "release"))) {
                properties.load(is);
                return properties.getProperty("IMAGE_TYPE", "").isEmpty();
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }
}
