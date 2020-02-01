package pg.gipter.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesFactoryTest {

    @Test
    void given_noArg_when_getInstance_then_returnCliApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{});

        assertThat(actual).isInstanceOf(CliApplicationProperties.class);
    }

    @Test
    void given_cliPreferredArgSource_when_getInstance_then_returnCliApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=cli"});

        assertThat(actual).isInstanceOf(CliApplicationProperties.class);
    }

    @Test
    void given_filePreferredArgSource_when_getInstance_then_returnFileApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=file"});

        assertThat(actual).isInstanceOf(FileApplicationProperties.class);
    }

    @Test
    void given_uiPreferredArgSource_when_getInstance_then_returnCliApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=ui"});

        assertThat(actual).isInstanceOf(CliApplicationProperties.class);
    }
}