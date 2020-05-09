package pg.gipter.core.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.SystemUtils;

/**Created by Pawel Gawedzki on 20-Sep-2018.*/
public final class DiffProducerFactory {

    private static final Logger logger = LoggerFactory.getLogger(DiffProducerFactory.class);

    private DiffProducerFactory() { }

    public static DiffProducer getInstance(ApplicationProperties applicationProperties) {
        logger.info("Running on platform [{}].", SystemUtils.OS);
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            return new StatementDiffProducer(applicationProperties);
        } else if (applicationProperties.itemType() == ItemType.TOOLKIT_DOCS) {
            return new ToolkitDocumentsDiffProducer(applicationProperties);
        } else if (applicationProperties.itemType() == ItemType.SHARE_POINT_DOCS) {
            return new SharePointDocumentsDiffProducer(ItemType.SHARE_POINT_DOCS, applicationProperties);
        } else if (SystemUtils.isUnix()) {
            return new LinuxDiffProducer(applicationProperties);
        } else if (SystemUtils.isWindows()) {
            return new WindowsDiffProducer(applicationProperties);
        } else if (SystemUtils.isMac()){
            return new MacDiffProducer(applicationProperties);
        } else {
            logger.warn("Platform {} not supported yet.", SystemUtils.OS);
            throw new RuntimeException("Not implemented yet!!!");
        }
    }

}
