package pg.gipter.core.producers;

import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationPropertiesFactory;

import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class DiffProducerFactoryTest {

    @Test
    void given_codeProtectionStatement_when_getInstance_then_returnStatementDiffProducer() {
        DiffProducer instance = DiffProducerFactory.getInstance(ApplicationPropertiesFactory.getInstance(new String[]{
                "preferredArgSource=FILE",
                "itemType=statement"
        }), Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        assertThat(instance).isInstanceOf(StatementDiffProducer.class);
    }

    @Test
    void given_codeProtectionDefault_when_getInstance_then_returnProducerDependOnEnvironment() {
        DiffProducer instance = DiffProducerFactory.getInstance(
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"}),
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        );
        String platform = System.getProperty("os.name");
        if ("Linux".equalsIgnoreCase(platform)) {
            assertThat(instance).isInstanceOf(LinuxDiffProducer.class);
        } else if (platform.startsWith("Windows")) {
            assertThat(instance).isInstanceOf(WindowsDiffProducer.class);
        }
    }
}