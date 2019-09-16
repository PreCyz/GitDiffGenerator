package pg.gipter.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;

/**Created by Pawel Gawedzki on 20-Sep-2018.*/
public final class DiffProducerFactory {

    private static final Logger logger = LoggerFactory.getLogger(DiffProducerFactory.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private DiffProducerFactory() { }

    public static DiffProducer getInstance(ApplicationProperties applicationProperties) {
        logger.info("Running on platform [{}].", OS);
        if (applicationProperties.uploadType() == UploadType.STATEMENT) {
            return new StatementDiffProducer(applicationProperties);
        } else if (applicationProperties.uploadType() == UploadType.TOOLKIT_DOCS) {
            return new ToolkitDocumentsDiffProducer(applicationProperties);
        } else if (isUnix()) {
            return new LinuxDiffProducer(applicationProperties);
        } else if (isWindows()) {
            return new WindowsDiffProducer(applicationProperties);
        } else if (isMac()){
            return new MacDiffProducer(applicationProperties);
        } else {
            logger.warn("Platform {} not supported yet.", OS);
            throw new RuntimeException("Not implemented yet!!!");
        }
    }

    private static boolean isWindows() {
        return OS.contains("win");
    }

    private static boolean isMac() {
        return OS.contains("mac");
    }

    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    private static boolean isSolaris() {
        return (OS.contains("sunos"));
    }
}
