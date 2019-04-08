package pg.gipter.producer.processor;

import pg.gipter.settings.ApplicationProperties;

import java.time.LocalDate;

public final class DocumentFinderFactory {

    private DocumentFinderFactory() { }

    public static DocumentFinder getInstance(ApplicationProperties applicationProperties) {
        if (LocalDate.now().isEqual(applicationProperties.endDate())) {
            return new SimpleDocumentFinder(applicationProperties);
        }
        return new ComplexDocumentFinder(applicationProperties);
    }
}
