package pg.gipter.services.platforms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.SystemUtils;

public class AppManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(AppManagerFactory.class);

    private AppManagerFactory() {}

    public static AppManager getInstance() {
        String platform = SystemUtils.osName().toLowerCase();
        if (platform.contains("win")) {
            return new WindowsAppManager();
        } else if (platform.contains("nix") || platform.contains("nux") || platform.contains("aix")) {
            return new LinuxAppManager();
        }
        logger.warn("Platform {} not supported yet.", platform);
        throw new RuntimeException("Not implemented yet!!!");
    }
}
