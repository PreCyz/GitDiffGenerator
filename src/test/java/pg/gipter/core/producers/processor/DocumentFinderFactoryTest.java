package pg.gipter.core.producers.processor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.producers.command.ItemType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentFinderFactoryTest {

    @Test
    void givenItemTypeToolkitDocs_whenGetInstance_thenReturnToolkitDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.itemType + "=" + ItemType.TOOLKIT_DOCS.name(),
                }
        );

        Optional<DocumentFinder> documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        assertThat(documentFinder.isPresent()).isTrue();
        assertThat(documentFinder.get()).isInstanceOf(ToolkitDocumentFinder.class);
    }

    @Test
    @Disabled("Until ComplexDocumentFinder is ready.")
    void givenEndDateNotEqualNow_whenGetInstance_thenReturnSimpleDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.endDate + "=" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE),
                }
        );

        Optional<DocumentFinder> documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        assertThat(documentFinder.isPresent()).isTrue();
        assertThat(documentFinder.get()).isInstanceOf(ComplexDocumentFinder.class);
    }

}