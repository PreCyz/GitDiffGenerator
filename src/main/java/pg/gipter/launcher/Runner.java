package pg.gipter.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;

class Runner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private ApplicationProperties applicationProperties;

    Runner(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void run() {
        DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
        diffProducer.produceDiff();

        if (!applicationProperties.isToolkitCredentialsSet()) {
            logger.error("Toolkit details not set. Check your settings.");
            throw new IllegalArgumentException();
        }

        DiffUploader diffUploader = new DiffUploader(applicationProperties);
        diffUploader.uploadDiff();
        logger.info("Diff upload complete.");
    }
}
