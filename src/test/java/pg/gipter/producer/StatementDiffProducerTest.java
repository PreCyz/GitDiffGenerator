package pg.gipter.producer;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class StatementDiffProducerTest {

    private StatementDiffProducer producer;

    @Test
    void given_emptyList_when_getFullCommand_then_returnThatEmptyList() {
        producer = new StatementDiffProducer(new ApplicationProperties(new String[]{}));
        List<String> emptyList = new ArrayList<>();

        List<String> actual = producer.getFullCommand(emptyList);

        assertThat(actual).isSameAs(emptyList);
    }

    @Test
    void given_noItemPath_when_produceDiff_then_throwIllegalArgumentException() {
        producer = new StatementDiffProducer(new ApplicationProperties(new String[]{
                "itemPath=none"
        }));
        try {
            producer.produceDiff();
            fail("Should throw IllegalArgumentException with relevant message.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Statement does not exists or it is not a file. Can not produce diff.");
        }
    }
}