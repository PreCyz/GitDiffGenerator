package pg.gipter.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesFactoryTest {

    @Test
    void given_noArg_when_getInstance_then_returnCliApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{});

        assertThat(actual).isInstanceOf(CliPreferredApplicationProperties.class);
    }

    @Test
    void given_cliPreferredArgSource_when_getInstance_then_returnCliApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=cli"});

        assertThat(actual).isInstanceOf(CliPreferredApplicationProperties.class);
    }

    @Test
    void given_filePreferredArgSource_when_getInstance_then_returnCliApplicationProperties() {
        ApplicationProperties actual = ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=file"});

        assertThat(actual).isInstanceOf(FilePreferredApplicationProperties.class);
    }
}