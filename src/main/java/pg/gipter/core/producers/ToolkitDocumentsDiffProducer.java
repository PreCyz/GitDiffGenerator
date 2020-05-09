package pg.gipter.core.producers;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;

class ToolkitDocumentsDiffProducer extends SharePointDocumentsDiffProducer {

    ToolkitDocumentsDiffProducer(ApplicationProperties applicationProperties) {
        super(ItemType.TOOLKIT_DOCS, applicationProperties);
    }

}
