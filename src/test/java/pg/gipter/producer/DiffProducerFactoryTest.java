package pg.gipter.producer;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationPropertiesFactory;

import static org.assertj.core.api.Assertions.assertThat;

class DiffProducerFactoryTest {

    @Test
    void given_codeProtectionStatement_when_getInstance_then_returnStatementDiffProducer() {
        DiffProducer instance = DiffProducerFactory.getInstance(ApplicationPropertiesFactory.getInstance(new String[]{
                "preferredArgSource=FILE",
                "uploadType=statement"
        }));

        assertThat(instance).isInstanceOf(StatementDiffProducer.class);
    }

    @Test
    void given_codeProtectionDefault_when_getInstance_then_returnProducerDependOnEnvironment() {
        DiffProducer instance = DiffProducerFactory.getInstance(
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );
        String platform = System.getProperty("os.name");
        if ("Linux".equalsIgnoreCase(platform)) {
            assertThat(instance).isInstanceOf(LinuxDiffProducer.class);
        } else if (platform.startsWith("Windows")) {
            assertThat(instance).isInstanceOf(WindowsDiffProducer.class);
        }
    }
}