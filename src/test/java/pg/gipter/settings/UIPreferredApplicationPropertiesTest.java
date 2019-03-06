package pg.gipter.settings;

import org.junit.jupiter.api.Test;
import pg.gipter.util.PropertiesHelper;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
public class UIPreferredApplicationPropertiesTest {

    private UIPreferredApplicationProperties appProps;

    private PropertiesHelper mockPropertiesLoader(Properties properties) {
        PropertiesHelper loader = mock(PropertiesHelper.class);
        when(loader.loadPropertiesFromFile()).thenReturn(Optional.of(properties));
        return loader;
    }

    @Test
    void given_emptyActiveTray_when_isActiveTray_then_returnTrue() {
        String[] args = {""};
        Properties props = new Properties();
        appProps = new UIPreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isTrue();
    }

    @Test
    void given_activeTrayYAndCliSetN_when_isActiveTray_then_returnFalse() {
        String[] args = {"activeTray=N"};
        Properties props = new Properties();
        props.put("activeTray", "Y");
        appProps = new UIPreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noUseUICliAndPropertySetN_when_isActiveTray_then_returnFalse() {
        String[] args = {""};
        Properties props = new Properties();
        props.put("activeTray", "N");
        appProps = new UIPreferredApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

}