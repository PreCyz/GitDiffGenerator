package pg.gipter.producer.command;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.FilePreferredApplicationProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyDiffCommandTest {

    private EmptyDiffCommand command;

    @Test
    void when_commandAsList_then_returnEmptyList() {
        command = new EmptyDiffCommand(new FilePreferredApplicationProperties(new String[]{}));

        List<String> actual = command.commandAsList();

        assertThat(actual).isEmpty();
    }

    @Test
    void when_getInitialCommand_then_return_emptyList() {
        command = new EmptyDiffCommand(new FilePreferredApplicationProperties(new String[]{}));

        List<String> actual = command.getInitialCommand();

        assertThat(actual).isEmpty();
    }
}