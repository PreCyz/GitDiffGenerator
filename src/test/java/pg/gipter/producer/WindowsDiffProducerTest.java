package pg.gipter.producer;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationPropertiesFactory;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindowsDiffProducerTest {

    @Test
    void given_listOfCommands_when_getFullCommand_then_returnFullCommand() {
        WindowsDiffProducer producer = new WindowsDiffProducer(
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );

        List<String> actual = producer.getFullCommand(Arrays.asList("c", "\"c2\"", "c3"));

        assertThat(actual).containsExactly("c", "\"c2\"", "c3");
    }
}