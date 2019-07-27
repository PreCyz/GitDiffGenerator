package pg.gipter.ui;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Starter;
import pg.gipter.utils.PropertiesHelper;

/** Created by Pawel Gawedzki on 15-Jul-2019. */
public class FXMultiRunner extends Task<Void> implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(FXMultiRunner.class);
    private final PropertiesHelper propertiesHelper;

    public FXMultiRunner() {
        this.propertiesHelper = new PropertiesHelper();
    }

    @Override
    public Void call() {
        start();
        return null;
    }

    public void start() {
        /*logger.info("{} started.", this.getClass().getName());
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
                String errorMessage = "Toolkit details not set. Check your settings.";
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            updateProgress(3, 5);
            updateMessage(BundleUtils.getMsg("progress.uploadingToToolkit"));
            diffUploader.uploadDiff();
            updateProgress(4, 5);
            updateMessage(BundleUtils.getMsg("progress.itemUploaded"));

            logger.info("Diff upload complete.");
        } catch (Exception ex) {
            logger.error("Diff upload failure.", ex);
            error = true;
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.error.messageWithLog", ex.getMessage()))
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.ERROR)
                    .withImage()
                    .buildAndDisplayWindow()
            );
        } finally {
            String status = error ? "FAIL" : "SUCCESS";
            propertiesHelper.saveUploadStatus(status);
            updateProgress(5, 5);
            updateMessage(BundleUtils.getMsg("progress.finished", status));
            logger.info("{} ended.", this.getClass().getName());
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.confirmation.message"))
                    .withLink(applicationProperties.toolkitUserFolder())
                    .withWindowType(WindowType.BROWSER_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withImage()
                    .buildAndDisplayWindow()
            );
        }*/
    }
}
