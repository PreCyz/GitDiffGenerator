package pg.gipter.core.producer;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producer.command.ItemType;

class ToolkitDocumentsDiffProducer extends SharePointDocumentsDiffProducer {

    ToolkitDocumentsDiffProducer(ApplicationProperties applicationProperties) {
        super(ItemType.TOOLKIT_DOCS, applicationProperties);
    }

}
