package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;

import java.util.Objects;
import java.util.Optional;

public final class DocumentFinderFactory {

    private DocumentFinderFactory() { }

    public static Optional<DocumentFinder> getInstance(ApplicationProperties applicationProperties) {
        if (Objects.requireNonNull(applicationProperties.itemType()) == ItemType.TOOLKIT_DOCS) {
            return Optional.of(new ToolkitDocumentFinder(applicationProperties));
        }
        return Optional.empty();
    }
}
