package pg.gipter;

import pg.gipter.utils.PropertiesHelper;

import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {

    private TestUtils() {
    }

    public static PropertiesHelper mockPropertiesLoader(Properties properties) {
        PropertiesHelper loader = mock(PropertiesHelper.class);
        when(loader.loadApplicationProperties(anyString())).thenReturn(Optional.of(properties));
        return loader;
    }
}
