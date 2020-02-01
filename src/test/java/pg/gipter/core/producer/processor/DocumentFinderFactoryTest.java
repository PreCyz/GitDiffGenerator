package pg.gipter.core.producer.processor;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class DocumentFinderFactoryTest {

    @Test
    void givenEndDateEqualNow_whenGetInstance_thenReturnSimpleDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                }
        );

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        AssertionsForClassTypes.assertThat(documentFinder).isInstanceOf(SimpleDocumentFinder.class);
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

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        AssertionsForClassTypes.assertThat(documentFinder).isInstanceOf(ComplexDocumentFinder.class);
    }

}