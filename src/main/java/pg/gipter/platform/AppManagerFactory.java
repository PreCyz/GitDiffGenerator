package pg.gipter.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(AppManagerFactory.class);

    private AppManagerFactory() {}

    public static AppManager getInstance() {
        String platform = System.getProperty("os.name");
        if (platform.startsWith("Windows")) {
            return new WindowsAppManager();
        } else if ("Linux".equalsIgnoreCase(platform)) {
            return new LinuxAppManager();
        }
        logger.warn("Platform {} not supported yet.", platform);
        throw new RuntimeException("Not implemented yet!!!");
    }
}
