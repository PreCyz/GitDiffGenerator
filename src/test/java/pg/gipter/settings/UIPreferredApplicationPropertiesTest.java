package pg.gipter.settings;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static pg.gipter.TestUtils.mockPropertiesLoader;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
public class UIPreferredApplicationPropertiesTest {

    private UIPreferredApplicationProperties appProps;

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