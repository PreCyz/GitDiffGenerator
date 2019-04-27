package pg.gipter.producer.processor;

import pg.gipter.settings.ApplicationProperties;

import java.time.LocalDate;

public final class DocumentFinderFactory {

    private DocumentFinderFactory() { }

    public static DocumentFinder getInstance(ApplicationProperties applicationProperties) {
        if (LocalDate.now().isAfter(applicationProperties.endDate())) {
            //return new ComplexDocumentFinder(applicationProperties);
        }
        if (applicationProperties.isUploadAsHtml()) {
            return new HtmlDocumentFinder(applicationProperties);
        }
        return new SimpleDocumentFinder(applicationProperties);
    }
}
