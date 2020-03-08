package pg.gipter.core.producer.processor;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.producer.command.ItemType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class DocumentFinderFactoryTest {

    @Test
    void givenItemTypeToolkitDocs_whenGetInstance_thenReturnToolkitDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.itemType + "=" + ItemType.TOOLKIT_DOCS.name(),
                }
        );

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        AssertionsForClassTypes.assertThat(documentFinder).isInstanceOf(ToolkitDocumentFinder.class);
    }

    @Test
    void givenItemTypeSharePointDocs_whenGetInstance_thenReturnSharePointDocumentFinder() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.itemType + "=" + ItemType.SHARE_POINT_DOCS.name(),
                }
        );

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        AssertionsForClassTypes.assertThat(documentFinder).isInstanceOf(SharePointDocumentFinder.class);
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