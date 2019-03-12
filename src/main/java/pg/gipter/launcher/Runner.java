package pg.gipter.launcher;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.util.AlertHelper;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;

class Runner implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private ApplicationProperties applicationProperties;
    private final PropertiesHelper propertiesHelper;

    Runner(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.propertiesHelper = new PropertiesHelper();
    }

    public void start() {
        boolean error = false;
        try {
            DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
            diffProducer.produceDiff();

            if (!applicationProperties.isToolkitCredentialsSet()) {
                logger.error("Toolkit details not set. Check your settings.");
                throw new IllegalArgumentException();
            }

            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            diffUploader.uploadDiff();

            logger.info("Diff upload complete.");
        } catch (Exception ex) {
            logger.error("Diff upload failure. Program is terminated.");
            error = true;
            String errMsg = AlertHelper.createLogsErrorMessage();
            AlertHelper.displayWindow(errMsg, Alert.AlertType.ERROR);
        } finally {
            propertiesHelper.saveUploadInfo(error ? "FAIL" : "SUCCESS");
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            String confirmationMsg = BundleUtils.getMsg("popup.confirmation.message", applicationProperties.toolkitUserFolder());
            AlertHelper.displayWindow(confirmationMsg, Alert.AlertType.INFORMATION);
        }
    }

}