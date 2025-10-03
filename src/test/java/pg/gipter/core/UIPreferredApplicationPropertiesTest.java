package pg.gipter.core;

import org.junit.jupiter.api.Test;
import pg.gipter.TestUtils;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.ui.UITheme;

import static org.assertj.core.api.Assertions.assertThat;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
class UIPreferredApplicationPropertiesTest {

    private ApplicationProperties appProps;

    @Test
    void given_emptyActiveTray_when_isActiveTray_then_returnTrue() {
        String[] args = {""};
        appProps = new UIApplicationProperties(args).init();
        appProps.init(TestUtils.mockConfigurationDao(new ApplicationConfig()));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isTrue();
    }

    @Test
    void given_activeTrayYAndCliSetN_when_isActiveTray_then_returnFalse() {
        String[] args = {"activeTray=N"};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setActiveTray(Boolean.TRUE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

    @Test
    void given_noUseUICliAndPropertySetN_when_isActiveTray_then_returnFalse() {
        String[] args = {""};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setActiveTray(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isActiveTray();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNoEnableOnStartup_whenIsEnableOnStartup_thenReturnDefault() {
        appProps = new UIApplicationProperties(new String[]{}).init();

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isTrue();
    }

    @Test
    void givenEnableOnStartupFromCLI_whenIsEnableOnStartup_thenReturnCliEnableOnStartup() {
        String[] args = {"enableOnStartup=n"};
        appProps = new UIApplicationProperties(args).init();

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFileAndCLI_whenIsEnableOnStartup_thenReturnCliEnableOnStartup() {
        String[] args = {"enableOnStartup=n"};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.TRUE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFromProperties_whenIsEnableOnStartup_thenReturnEnableOnStartupFromProperties() {
        String[] args = {};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenEnableOnStartupFromPropertiesAndOtherArgs_whenIsEnableOnStartup_thenReturnEnableOnStartupFromProperties() {
        String[] args = {"author=test"};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setEnableOnStartup(Boolean.FALSE);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        boolean actual = appProps.isEnableOnStartup();

        assertThat(actual).isFalse();
    }

    @Test
    void givenNoUITheme_whenUITheme_thenReturnDEFAULT() {
        appProps = new UIApplicationProperties(new String[]{}).init();

        UITheme actual = appProps.uiTheme();

        assertThat(actual).isEqualTo(UITheme.DEFAULT);
    }

    @Test
    void givenUIThemeFromCLI_whenUITheme_thenReturnDEFAULT() {
        String[] args = {"uiTheme=DRACULA"};
        appProps = new UIApplicationProperties(args).init();

        UITheme actual = appProps.uiTheme();

        assertThat(actual).isEqualTo(UITheme.DEFAULT);
    }

    @Test
    void givenUIThemeFileAndCLI_whenUITheme_thenReturnFileUITheme() {
        String[] args = {"uiTheme=DEFAULT"};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUiTheme(UITheme.NORD_DARK);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        UITheme actual = appProps.uiTheme();

        assertThat(actual).isEqualTo(UITheme.NORD_DARK);
    }

    @Test
    void givenUIThemeFromProperties_whenUITheme_thenReturnUIThemeFromProperties() {
        String[] args = {};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUiTheme(UITheme.CUPERTINO_DARK);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        UITheme actual = appProps.uiTheme();

        assertThat(actual).isEqualTo(UITheme.CUPERTINO_DARK);
    }

    @Test
    void givenUIThemeFromPropertiesAndOtherArgs_whenUITheme_thenReturnUIThemeFromProperties() {
        String[] args = {"author=test"};
        appProps = new UIApplicationProperties(args).init();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setUiTheme(UITheme.DRACULA);
        appProps.init(TestUtils.mockConfigurationDao(applicationConfig));

        UITheme actual = appProps.uiTheme();

        assertThat(actual).isEqualTo(UITheme.DRACULA);
    }

}