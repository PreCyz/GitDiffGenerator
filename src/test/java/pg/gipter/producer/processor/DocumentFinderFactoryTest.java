package pg.gipter.producer.processor;

import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DocumentFinderFactoryTest {

    @Test
    void givenEndDateEqualNow_whenGetInstance_thenReturnSimpleDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.endDate + "=" + LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                }
        );

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        assertThat(documentFinder).isInstanceOf(SimpleDocumentFinder.class);
    }

    @Test
    void givenEndDateNotEqualNow_whenGetInstance_thenReturnSimpleDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.endDate + "=" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE),
                }
        );

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        assertThat(documentFinder).isInstanceOf(ComplexDocumentFinder.class);
    }
}