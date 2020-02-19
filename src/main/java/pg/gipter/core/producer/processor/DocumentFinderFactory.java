package pg.gipter.core.producer.processor;

import pg.gipter.core.ApplicationProperties;

import java.time.LocalDate;

public final class DocumentFinderFactory {

    private DocumentFinderFactory() { }

    public static DocumentFinder getInstance(ApplicationProperties applicationProperties) {
        if (LocalDate.now().isAfter(applicationProperties.endDate())) {
            //return new ComplexDocumentFinder(applicationProperties);
        }
        return new SimpleDocumentFinder(applicationProperties);
    }
}
