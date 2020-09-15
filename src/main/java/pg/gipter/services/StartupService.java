package pg.gipter.services;

import mslinks.ShellLink;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.io.File;
import java.io.IOException;

public class StartupService {

    private static final Logger logger = LoggerFactory.getLogger(UILauncher.class);
    private final String systemUsername;

    public StartupService() {
        systemUsername = System.getProperty("user.name");
    }

    public void startOnStartup() {
        if (SystemUtils.isWindows()) {
            String shortcutLnkPath = String.format(
                    "C:\\Users\\%s\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\Gipter.lnk",
                    systemUsername
            );

            String target = JarHelper.getJarFile().map(File::getAbsolutePath).orElse("");
            if (!new File(shortcutLnkPath).exists()) {
                logger.info("Creating shortcut to [{}] and placing it in Windows startup folder. [{}]", target, shortcutLnkPath);
                try {
                    String workingDir = JarHelper.homeDirectoryPath().orElse("");

                    int iconNumber = 130;
                    ShellLink shellLink = ShellLink.createLink(target)
                            .setWorkingDir(workingDir)
                            .setIconLocation("%SystemRoot%\\system32\\SHELL32.dll")
                            .setCMDArgs(ArgName.silentMode.name() + "=" + Boolean.TRUE);
                    shellLink.getHeader().setIconIndex(iconNumber);
                    shellLink.saveTo(shortcutLnkPath);
                    logger.info("Shortcut located in startup folder [{}].", shortcutLnkPath);
                    logger.info("Link working dir {}", shellLink.getWorkingDir());
                    logger.info("Link target {}", shellLink.resolveTarget());
                    logger.info("Link arguments [{}]", shellLink.getCMDArgs());
                    logger.info("Shortcut created and placed in Windows startup folder.");
                } catch (IOException e) {
                    logger.warn("Can not create shortcut to [{}] file and place it in Windows startup folder. [{}]", target, shortcutLnkPath, e);
                }
            } else {
                logger.info("Gipter have already been set to start on startup. Shortcut already exists [{}]. ", shortcutLnkPath);
            }
        }
    }

    public void disableStartOnStartup() {
        if (SystemUtils.isWindows()) {
            String shortcutLnkPath = String.format(
                    "C:\\Users\\%s\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\Gipter.lnk",
                    systemUsername
            );
            File link = new File(shortcutLnkPath);
            if (link.exists() && link.isFile()) {
                try {
                    FileUtils.forceDelete(link);
                    logger.info("Deletion of link done: [{}]", shortcutLnkPath);
                } catch (IOException e) {
                    logger.error("Can not delete link: [{}]", shortcutLnkPath, e);
                }
            }
        }
    }
}
