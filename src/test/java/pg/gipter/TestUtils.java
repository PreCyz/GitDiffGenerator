package pg.gipter;

import org.mockito.stubbing.Answer;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.settings.ArgName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {

    private TestUtils() {
    }

    public static PropertiesDao mockPropertiesLoader(Properties properties) {
        PropertiesDao loader = mock(PropertiesDao.class);
        when(loader.loadApplicationProperties(eq(properties.getProperty(ArgName.configurationName.name())))).thenReturn(Optional.of(properties));
        when(loader.loadAllApplicationProperties()).thenAnswer((Answer<Map<String, Properties>>) invocationOnMock -> {
            Map<String, Properties> answer = new HashMap<>();
            answer.put(properties.getProperty(ArgName.configurationName.name()), properties);
            return answer;
        });
        return loader;
    }
}
