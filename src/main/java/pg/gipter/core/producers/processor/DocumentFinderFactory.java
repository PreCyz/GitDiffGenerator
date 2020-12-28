package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;

import java.time.LocalDate;
import java.util.Optional;

public final class DocumentFinderFactory {

    private DocumentFinderFactory() { }

    public static Optional<DocumentFinder> getInstance(ApplicationProperties applicationProperties) {
        if (LocalDate.now().isAfter(applicationProperties.endDate())) {
            //return new ComplexDocumentFinder(applicationProperties);
        }
        switch (applicationProperties.itemType()) {
            case SHARE_POINT_DOCS:
                return Optional.of(new SharePointDocumentFinder(applicationProperties));
            case TOOLKIT_DOCS:
                return Optional.of(new ToolkitDocumentFinder(applicationProperties));
            default:
                return Optional.empty();
        }
    }
}
