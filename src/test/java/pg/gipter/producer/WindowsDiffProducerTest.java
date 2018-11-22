package pg.gipter.producer;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindowsDiffProducerTest {

    @Test
    void given_listOfCommands_when_getFullCommand_then_returnFullCommand() {
        WindowsDiffProducer producer = new WindowsDiffProducer(new ApplicationProperties(new String[]{}));

        List<String> actual = producer.getFullCommand(Arrays.asList("c", "\"c2\"", "c3"));

        assertThat(actual).containsExactly("c", "\"c2\"", "c3");
    }
}