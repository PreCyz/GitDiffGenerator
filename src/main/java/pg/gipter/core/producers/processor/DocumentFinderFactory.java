package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;

import java.time.LocalDate;

public final class DocumentFinderFactory {

    private DocumentFinderFactory() { }

    public static DocumentFinder getInstance(ApplicationProperties applicationProperties) {
        if (LocalDate.now().isAfter(applicationProperties.endDate())) {
            //return new ComplexDocumentFinder(applicationProperties);
        }
        switch (applicationProperties.itemType()) {
            case SHARE_POINT_DOCS:
                return new SharePointDocumentFinder(applicationProperties);
            case TOOLKIT_DOCS:
                return new ToolkitDocumentFinder(applicationProperties);
            default:
                return null;
        }
    }
}
