package pg.gipter.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class CacheManagerTest {

    @BeforeEach
    void setUp() {
        CacheManager.clearAllCache();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenCacheManager_whenAddToCache_thenCacheNotEmpty() {
        String confName = "confName";
        String[] args = new String[] {
                ArgName.configurationName + "=" + confName
        };
        CacheManager.addToCache(confName, ApplicationPropertiesFactory.getInstance(args));

        assertThat(CacheManager.cacheMap).isNotEmpty();
    }

    @Test
    void givenEmptyCache_whenGetApplicationProperties_thenCacheEmptyAndReturnResult() {
        String confName = "confName";
        Properties props = new Properties();
        props.put(ArgName.configurationName.name(), confName);
        DaoFactory.getPropertiesDao().saveRunConfig(props);

        ApplicationProperties applicationProperties = CacheManager.getApplicationProperties(confName);

        assertThat(CacheManager.cacheMap).isEmpty();
        assertThat(applicationProperties).isNotNull();
    }

    @Test
    void givenCacheManager_whenRemoveFromCache_thenCacheEmpty() {
        String confName = "confName";
        String[] args = new String[] {
                ArgName.configurationName + "=" + confName
        };
        CacheManager.addToCache(confName, ApplicationPropertiesFactory.getInstance(args));

        CacheManager.removeFromCache(confName);

        assertThat(CacheManager.cacheMap).isEmpty();
    }

    @Test
    void givenCacheManager_whenClearAllCache_thenCacheEmpty() {
        String confName = "confName";
        String[] args = new String[] {
                ArgName.configurationName + "=" + confName
        };
        CacheManager.addToCache(confName, ApplicationPropertiesFactory.getInstance(args));

        CacheManager.clearAllCache();

        assertThat(CacheManager.cacheMap).isEmpty();
    }
}