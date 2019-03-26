package pg.gipter.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;

/**Created by Pawel Gawedzki on 20-Sep-2018.*/
public final class DiffProducerFactory {

    private static final Logger logger = LoggerFactory.getLogger(DiffProducerFactory.class);

    private DiffProducerFactory() { }

    public static DiffProducer getInstance(ApplicationProperties applicationProperties) {
        String platform = System.getProperty("os.name");
        logger.info("Running on platform [{}].", platform);
        if (applicationProperties.uploadType() == UploadType.STATEMENT) {
            return new StatementDiffProducer(applicationProperties);
        } else if (applicationProperties.uploadType() == UploadType.DOCUMENTS) {
            return new DocumentsDiffProducer(applicationProperties);
        } else if ("Linux".equalsIgnoreCase(platform)) {
            return new LinuxDiffProducer(applicationProperties);
        } else if (platform.startsWith("Windows")) {
            return new WindowsDiffProducer(applicationProperties);
        } else {
            logger.warn("Platform {} not supported yet.", platform);
            throw new RuntimeException("Not implemented yet!!!");
        }
    }
}
