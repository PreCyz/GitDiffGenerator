package pg.gipter.core.dao.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.RunConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CachedConfigurationProxyTest {

    private CachedConfiguration cachedConfiguration = ConfigurationDaoFactory.getCachedConfigurationDao();
    private ConfigurationDao configurationDao = ConfigurationDaoFactory.getConfigurationDao();

    @BeforeEach
    void setUp() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenRunConfig_whenRemoveConfig_thenConfigIsRemoved() {
        RunConfig runConfig = new RunConfig();
        String configurationName = "testName";
        runConfig.setConfigurationName(configurationName);
        cachedConfiguration.saveConfiguration(
                new Configuration(null, null, Arrays.asList(runConfig), null)
        );
        assertThat(configurationDao.loadRunConfig(configurationName).isPresent()).isTrue();

        cachedConfiguration.removeConfig(configurationName);

        Optional<RunConfig> actual = cachedConfiguration.loadRunConfig(configurationName);
        assertThat(actual.isPresent()).isFalse();
    }
}