package pg.gipter.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    static final Map<String, ApplicationProperties> cacheMap = new LinkedHashMap<>();
    private static final PropertiesDao propertiesDao = DaoFactory.getPropertiesDao();

    private CacheManager() { }

    public static ApplicationProperties getApplicationProperties(String configName) {
        if (cacheMap.containsKey(configName)) {
            logger.info("Configuration [{}] is taken from cache.", configName);
            return cacheMap.get(configName);
        }
        return ApplicationPropertiesFactory.getInstance(propertiesDao.loadArgumentArray(configName));
    }

    public static Map<String, ApplicationProperties> getAllApplicationProperties() {
        Map<String, ApplicationProperties> result = new LinkedHashMap<>();

        Map<String, Properties> map = propertiesDao.loadAllApplicationProperties();
        for (Map.Entry<String, Properties> entry : map.entrySet()) {
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

    static void clearAllCache() {
        int size = cacheMap.size();
        cacheMap.clear();
        logger.info("Cached cleared. [{}] entries removed.", size);
    }


}
