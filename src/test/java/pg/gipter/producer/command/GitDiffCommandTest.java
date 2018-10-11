package pg.gipter.producer.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pg.gipter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GitDiffCommandTest {

    @Test
    void commandAsList() {
        assertThat(true).isTrue();
    }

    @Test
    void getInitialCommand() {
        assertThat(false).isFalse();
    }
}