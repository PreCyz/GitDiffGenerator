package pg.gipter;

import org.mockito.stubbing.Answer;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.ToolkitConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {

    private TestUtils() {
    }

    public static ConfigurationDao mockConfigurationDao(RunConfig runConfig) {
        return mockConfigurationDao(runConfig, null, null);
    }

    public static ConfigurationDao mockConfigurationDao(ApplicationConfig applicationConfig) {
        return mockConfigurationDao(null, applicationConfig, null);
    }

    public static ConfigurationDao mockConfigurationDao(ToolkitConfig toolkitConfig) {
        return mockConfigurationDao(null, null, toolkitConfig);
    }

    public static ConfigurationDao mockConfigurationDao(RunConfig runConfig, ApplicationConfig applicationConfig, ToolkitConfig toolkitConfig) {
        ConfigurationDao loader = mock(ConfigurationDao.class);
        if (runConfig != null) {
            when(loader.loadRunConfig(eq(runConfig.getConfigurationName()))).thenReturn(Optional.of(runConfig));
            when(loader.loadRunConfigMap()).thenAnswer((Answer<Map<String, RunConfig>>) invocationOnMock -> {
                Map<String, RunConfig> answer = new HashMap<>();
                answer.put(runConfig.getConfigurationName(), runConfig);
                return answer;
            });
        }
        if (applicationConfig != null) {
            when(loader.loadApplicationConfig()).thenReturn(applicationConfig);
        }
        if (toolkitConfig != null) {
            when(loader.loadToolkitConfig()).thenReturn(toolkitConfig);
        }
        return loader;
    }
}
