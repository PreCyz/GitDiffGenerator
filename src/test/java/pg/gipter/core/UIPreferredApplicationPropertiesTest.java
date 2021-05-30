package pg.gipter.core;

import org.junit.jupiter.api.Test;
import pg.gipter.TestUtils;
import pg.gipter.core.model.ApplicationConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
class UIPreferredApplicationPropertiesTest {

    private UIApplicationProperties appProps;

    @Test
    void given_emptyActiveTray_when_isActiveTray_then_returnTrue() {
        String[] args = {""};
        appProps = new UIApplicationProperties(args);
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isTrue();
    }

    @Test
    void given_activeTrayYAndCliSetN_when_isActiveTray_then_returnFalse() {
        String[] args = {"activeTray=N"};
        appProps = new UIApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setActiveTray(Boolean.TRUE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noUseUICliAndPropertySetN_when_isActiveTray_then_returnFalse() {
        String[] args = {""};
        appProps = new UIApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setActiveTray(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

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
        appProps = new UIApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.TRUE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFromProperties_whenIsEnableOnStartup_thenReturnEnableOnStartupFromProperties() {
        String[] args = {};
        appProps = new UIApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFromPropertiesAndOtherArgs_whenIsEnableOnStartup_thenReturnEnableOnStartupFromProperties() {
        String[] args = {"author=test"};
        appProps = new UIApplicationProperties(args);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

}