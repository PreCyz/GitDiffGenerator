package pg.gipter.services;

import mslinks.ShellLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

public class StartupService {

    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final String systemUsername;

    public StartupService() {
        systemUsername = SystemUtils.userName();
    }

    public void startOnStartup() {
        if (SystemUtils.isWindows()) {
            Path shortcutLnkPath = Paths.get(
                    "C:",
                    "Users",
                    systemUsername,
                    "AppData",
                    "Roaming",
                    "Microsoft",
                    "Windows",
                    "Start Menu",
                    "Programs",
                    "Startup",
                    "Gipter.lnk"
            );

            String cmdArgs = ArgName.silentMode.name() + "=" + Boolean.TRUE;
            Path target = JarHelper.getJarPath().orElse(Paths.get(""));
            Optional<String> jarHomeDirectory = JarHelper.homeDirectoryPath();
            if (SystemUtils.isExe()) {
                target = Paths.get(".", "Gipter.exe").normalize().toAbsolutePath();
            } else if (jarHomeDirectory.isPresent() && Files.exists(Paths.get(jarHomeDirectory.get(), "runtime"))) {
                target = Paths.get(jarHomeDirectory.get(), "runtime", "bin", "javaw.exe");
                cmdArgs = "-jar Gipter.jar "  + cmdArgs;
            }
            if (!Files.exists(shortcutLnkPath)) {
                logger.info("Creating shortcut to [{}] and placing it in Windows startup folder. [{}]", target, shortcutLnkPath);
                try {
                    String workingDir = jarHomeDirectory.orElse("");
                    if (SystemUtils.isWindows()) {
                        workingDir = Paths.get(".").normalize().toAbsolutePath().toString();
                    }

                    int iconNumber = 130;
                    ShellLink shellLink = ShellLink.createLink(target.toAbsolutePath().normalize().toString())
                            .setWorkingDir(workingDir)
                            .setIconLocation("%SystemRoot%\\system32\\SHELL32.dll")
                            .setCMDArgs(cmdArgs);
                    shellLink.getHeader().setIconIndex(iconNumber);
                    shellLink.saveTo(shortcutLnkPath.toAbsolutePath().toString());
                    logger.info("Shortcut located in startup folder [{}].", shortcutLnkPath);
                    logger.info("Link working dir {}", shellLink.getWorkingDir());
                    logger.info("Link target {}", shellLink.resolveTarget());
                    logger.info("Link arguments [{}]", shellLink.getCMDArgs());
                    logger.info("Shortcut created and placed in Windows startup folder.");
                } catch (IOException e) {
                    logger.warn("Can not create shortcut to [{}] file and place it in Windows startup folder: [{}]. {}",
                            target, shortcutLnkPath, e.getMessage());
                }
            } else {
                logger.info("Gipter have already been set to start on startup. Shortcut already exists [{}]. ", shortcutLnkPath);
            }
        }
    }

    public void disableStartOnStartup() {
        if (SystemUtils.isWindows()) {
            Path shortcutLnkPath = Paths.get(
                    "C:",
                    "Users",
                    systemUsername,
                    "AppData",
                    "Roaming",
                    "Microsoft",
                    "Windows",
                    "Start Menu",
                    "Programs",
                    "Startup",
                    "Gipter.lnk"
            );
            if (Files.exists(shortcutLnkPath) && Files.isRegularFile(shortcutLnkPath)) {
                try {
                    Files.deleteIfExists(shortcutLnkPath);
                    logger.info("Deletion of link done: [{}]", shortcutLnkPath);
                } catch (IOException e) {
                    logger.error("Can not delete link: [{}]. {}", shortcutLnkPath, e.getMessage());
                }
            }
        }
    }

    /** Conditions: When jar-distro with custom runtime
     *      - runtime folder exists
     *      - Gipter.jar exists
     **/
    public void createShortcut() {
        Optional<String> homeDirectory = JarHelper.homeDirectoryPath();
        if (homeDirectory.isEmpty()) {
            return;
        }
        Path runtime = Paths.get(homeDirectory.toString(), "runtime").normalize().toAbsolutePath();
        Path jar = Paths.get(homeDirectory.toString(), "Gipter.jar").normalize().toAbsolutePath();
        if (Files.exists(runtime) && Files.exists(jar)) {
            try {
                Path shortcutLnkPath = Paths.get(homeDirectory.toString(), "Gipter.lnk");
                if (Files.exists(shortcutLnkPath)) {
                    logger.info("Shortcut [{}] exists.", shortcutLnkPath);
                    return;
                }

                String target = Paths.get(runtime.toString(), "bin", "javaw.exe").toString();

                logger.info("Creating shortcut [{}] for [{}] with custom runtime image [{}].",
                        shortcutLnkPath,
                        jar,
                        target
                );

                ShellLink shellLink = ShellLink.createLink(target)
                        .setWorkingDir(homeDirectory.toString())
                        .setCMDArgs(String.format(" -jar \"%s\"", jar));

                Path iconPath = Paths.get(homeDirectory.toString(), "gipter.ico").toAbsolutePath();
                if (Files.exists(iconPath)) {
                    shellLink = shellLink.setIconLocation(iconPath.toString());
                    shellLink.getHeader().setIconIndex(0);
                }

                shellLink.saveTo(shortcutLnkPath.toString());
                logger.info("Shortcut created. [{}]", shortcutLnkPath);
            } catch (Exception e) {
                logger.error("Shortcut [Gipter.lnk] was not created. {}", e.getMessage());
            }
        }
    }
}
