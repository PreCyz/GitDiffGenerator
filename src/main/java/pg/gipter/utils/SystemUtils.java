package pg.gipter.utils;

/** Created by Pawel Gawedzki on 16-Sep-2019. */
public final class SystemUtils {

    private SystemUtils() { }

    public static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String OS_LOWER_CASE = OS.toLowerCase();

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
        return System.getProperty("line.separator");
    }

}
