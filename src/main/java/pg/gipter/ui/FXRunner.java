package pg.gipter.ui;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Starter;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.util.AlertHelper;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;

/** Created by Pawel Gawedzki on 10-Mar-2019. */
public class FXRunner extends Task<Void> implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(FXRunner .class);
    private ApplicationProperties applicationProperties;
    private final PropertiesHelper propertiesHelper;

    public FXRunner(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.propertiesHelper = new PropertiesHelper();
    }

    @Override
    public Void call() {
        start();
        return null;
    }

    public void start() {
        logger.info("{} started.", this.getClass().getName());
        boolean error = false;
        try {
            updateMessage(BundleUtils.getMsg("progress.start"));
            updateProgress(0, 5);
            DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
            updateProgress(1, 5);
            updateMessage(BundleUtils.getMsg("progress.generatingDiff"));
            diffProducer.produceDiff();
            updateProgress(2, 5);
            updateMessage(BundleUtils.getMsg("progress.diffGenerated"));

            if (!applicationProperties.isToolkitCredentialsSet()) {
                logger.error("Toolkit details not set. Check your settings.");
                throw new IllegalArgumentException();
            }

            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            updateProgress(3, 5);
            updateMessage(BundleUtils.getMsg("progress.uploadingToToolkit"));
            diffUploader.uploadDiff();
            updateProgress(4, 5);
            updateMessage(BundleUtils.getMsg("progress.itemUploaded"));

            logger.info("Diff upload complete.");
        } catch (Exception ex) {
            logger.error("Diff upload failure. Program is terminated.");
            error = true;
            AlertHelper.displayWindow(
                    BundleUtils.getMsg("popup.error.messageWithLog"),
                    AlertHelper.logsFolder(),
                    AlertHelper.LOG_WINDOW,
                    Alert.AlertType.ERROR
            );
        } finally {
            String status = error ? "FAIL" : "SUCCESS";
            propertiesHelper.saveUploadInfo(status);
            updateProgress(5, 5);
            updateMessage(BundleUtils.getMsg("progress.finished", status));
            logger.info("{} ended.", this.getClass().getName());
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            AlertHelper.displayWindow(
                    BundleUtils.getMsg("popup.confirmation.message"),
                    applicationProperties.toolkitUserFolder(),
                    AlertHelper.BROWSER_WINDOW,
                    Alert.AlertType.INFORMATION
            );
        }
    }
}
