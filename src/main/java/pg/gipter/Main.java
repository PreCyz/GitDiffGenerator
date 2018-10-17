package pg.gipter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;

import java.time.format.DateTimeFormatter;

/**Created by Pawel Gawedzki on 17-Sep-2018*/
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        try {
            logger.error("Gipter started.");
            ApplicationProperties applicationProperties = new ApplicationProperties(args);

            DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
            diffProducer.produceDiff();

            if (!applicationProperties.isToolkitPropertiesSet()) {
                logger.error("Toolkit details not set. Check your settings");
                throw new IllegalArgumentException();
            }

            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            diffUploader.uploadDiff();
            logger.error("Diff upload success.");

            logger.error("Program is terminated.");
            System.exit(0);
        } catch (Exception ex) {
            logger.error("Diff upload failure. Program is terminated.");
            //TODO: inform user upload was unsuccessful
            System.exit(-1);
        }

    }

}
