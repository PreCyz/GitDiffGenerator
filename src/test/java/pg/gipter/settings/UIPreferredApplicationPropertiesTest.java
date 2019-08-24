package pg.gipter.settings;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static pg.gipter.TestUtils.mockPropertiesLoader;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
class UIPreferredApplicationPropertiesTest {

    private UIApplicationProperties appProps;

    @Test
    void given_emptyActiveTray_when_isActiveTray_then_returnTrue() {
        String[] args = {""};
        Properties props = new Properties();
        appProps = new UIApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isTrue();
    }

    @Test
    void given_activeTrayYAndCliSetN_when_isActiveTray_then_returnFalse() {
        String[] args = {"activeTray=N"};
        Properties props = new Properties();
        props.put("activeTray", "Y");
        appProps = new UIApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noUseUICliAndPropertySetN_when_isActiveTray_then_returnFalse() {
        String[] args = {""};
        Properties props = new Properties();
        props.put("activeTray", "N");
        appProps = new UIApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNoEnableOnStartup_whenIsEnableOnStartup_thenReturnDefault() {
        appProps = new UIApplicationProperties(new String[]{});

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isTrue();
    }

    @Test
    void givenEnableOnStartupFromCLI_whenIsEnableOnStartup_thenReturnCliEnableOnStartup() {
        appProps = new UIApplicationProperties(
                new String[]{"enableOnStartup=n"}
        );

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFileAndCLI_whenIsEnableOnStartup_thenReturnCliEnableOnStartup() {
        String[] args = {"enableOnStartup=n"};
        Properties props = new Properties();
        props.put("enableOnStartup", "t");
        appProps = new UIApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFromProperties_whenIsEnableOnStartup_thenReturnEnableOnStartupFromProperties() {
        String[] args = {};
        Properties props = new Properties();
        props.put("enableOnStartup", "n");
        appProps = new UIApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFromPropertiesAndOtherArgs_whenIsEnableOnStartup_thenReturnEnableOnStartupFromProperties() {
        String[] args = {"author=test"};
        Properties props = new Properties();
        props.put("enableOnStartup", "n");
        appProps = new UIApplicationProperties(args);
        appProps.init(args, mockPropertiesLoader(props));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

}