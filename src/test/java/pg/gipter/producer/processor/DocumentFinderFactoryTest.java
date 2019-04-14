package pg.gipter.producer.processor;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        AssertionsForClassTypes.assertThat(documentFinder).isInstanceOf(SimpleDocumentFinder.class);
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

        AssertionsForClassTypes.assertThat(documentFinder).isInstanceOf(ComplexDocumentFinder.class);
    }

    @Test
    void find() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.endDate + "=" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE),
                        ArgName.startDate + "=2019-04-01",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.toolkitPassword + "=JanuarY12!@",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD",
                        ArgName.itemPath + "=/home/gawa/IdeaProjects/GitDiffGenerator/out/production"
                });

        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);

        List<File> downloadedFiles = documentFinder.find();

        assertThat(downloadedFiles).isNotEmpty();
    }

}