package pg.gipter;

import org.mockito.stubbing.Answer;
import pg.gipter.configuration.ConfigurationDao;
import pg.gipter.settings.dto.ApplicationConfig;
import pg.gipter.settings.dto.RunConfig;
import pg.gipter.settings.dto.ToolkitConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {

    private TestUtils() {
    }

    public static ConfigurationDao mockConfigurtionDao(RunConfig runConfig) {
        return mockConfigurtionDao(runConfig, null, null);
    }

    public static ConfigurationDao mockConfigurtionDao(ApplicationConfig applicationConfig) {
        return mockConfigurtionDao(null, applicationConfig, null);
    }

    public static ConfigurationDao mockConfigurtionDao(ToolkitConfig toolkitConfig) {
        return mockConfigurtionDao(null, null, toolkitConfig);
    }

    public static ConfigurationDao mockConfigurtionDao(RunConfig runConfig, ApplicationConfig applicationConfig, ToolkitConfig toolkitConfig) {
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
