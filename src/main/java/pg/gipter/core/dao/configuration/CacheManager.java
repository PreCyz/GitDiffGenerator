package pg.gipter.core.dao.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dto.RunConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    static final Map<String, ApplicationProperties> cacheMap = new LinkedHashMap<>();
    private static final ConfigurationDao configurationDao = DaoFactory.getCachedConfiguration();

    private CacheManager() { }

    public static ApplicationProperties getApplicationProperties(String configName) {
        if (cacheMap.containsKey(configName)) {
            logger.info("Configuration [{}] is taken from cache.", configName);
            return cacheMap.get(configName);
        }
        return ApplicationPropertiesFactory.getInstance(configurationDao.loadArgumentArray(configName));
    }

    public static Map<String, ApplicationProperties> getAllApplicationProperties() {
        Map<String, ApplicationProperties> result = new LinkedHashMap<>();

        Map<String, RunConfig> map = configurationDao.loadRunConfigMap();
        for (Map.Entry<String, RunConfig> entry : map.entrySet()) {
            if (cacheMap.containsKey(entry.getKey())) {
                logger.info("Configuration [{}] is taken from cache.", entry.getKey());
                result.put(entry.getKey(), cacheMap.get(entry.getKey()));
            } else {
                result.put(entry.getKey(), getApplicationProperties(entry.getKey()));
            }
        }
        return result;
    }

    public static void addToCache(String configName, ApplicationProperties applicationProperties) {
        cacheMap.put(configName, applicationProperties);
        logger.info("Configuration [{}] added to cache.", configName);
    }

    public static void removeFromCache(String configName) {
        cacheMap.remove(configName);
        logger.info("Configuration [{}] removed from cache.", configName);
    }

    public static void clearAllCache() {
        int size = cacheMap.size();
        cacheMap.clear();
        logger.info("Cache cleared. [{}] entries removed.", size);
    }

}
