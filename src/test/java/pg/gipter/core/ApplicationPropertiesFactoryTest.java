package pg.gipter.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesFactoryTest {

    @Test
    void givenNoArg_whenGetInstance_thenReturnUIApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{});

        assertThat(actual).isInstanceOf(UIApplicationProperties.class);
    }

    @Test
    void givenCliPreferredArgSourceWithNoUseUI_whenGetInstance_thenReturnUIApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=cli"});

        assertThat(actual).isInstanceOf(UIApplicationProperties.class);
    }

    @Test
    void givenCliPreferredArgSourceAndUseUINo_whenGetInstance_thenReturnUIApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{
                ArgName.preferredArgSource.name() + "=cli",
                ArgName.useUI.name() + "=n",
        });

        assertThat(actual).isInstanceOf(CliApplicationProperties.class);
    }

    @Test
    void given_filePreferredArgSource_when_getInstance_then_returnFileApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=file"});

        assertThat(actual).isInstanceOf(FileApplicationProperties.class);
    }

    @Test
    void givenUIPreferredArgSource_whenGetInstance_thenReturnUIApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=ui"});

        assertThat(actual).isInstanceOf(UIApplicationProperties.class);
    }
}