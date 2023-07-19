package pg.gipter.core.dao.configuration;

import org.junit.jupiter.api.*;
import pg.gipter.core.*;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
        RunConfig runConfig = new RunConfigBuilder().withConfigurationName(confName).create();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        DaoFactory.getCachedConfiguration().saveConfiguration(
                new Configuration(applicationConfig, toolkitConfig, Arrays.asList(runConfig), null)
        );

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