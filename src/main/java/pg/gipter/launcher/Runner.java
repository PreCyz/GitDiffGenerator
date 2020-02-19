package pg.gipter.launcher;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.producer.DiffProducer;
import pg.gipter.core.producer.DiffProducerFactory;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.ui.alert.*;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;

class Runner implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private ApplicationProperties applicationProperties;
    private final DataDao dataDao;

    Runner(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.dataDao = DaoFactory.getDataDao();
    }

    @Override
    public void start() {
        logger.info("{} started.", this.getClass().getName());
        boolean error = false;
        try {
            DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
            diffProducer.produceDiff();

            if (!applicationProperties.isToolkitCredentialsSet()) {
                String errorMsg = "Toolkit credentials not set. Check your settings.";
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            diffUploader.uploadDiff();

            logger.info("Diff upload complete.");
        } catch (Exception ex) {
            logger.error("Diff upload failure. Program will be terminated.", ex);
            error = true;
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.error.messageWithLog", ex.getMessage()))
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.ERROR)
                    .withImage(ImageFile.ERROR_CHICKEN_PNG)
                    .buildAndDisplayWindow()
            );
        } finally {
            dataDao.saveUploadStatus(error ? "FAIL" : "SUCCESS");
            logger.info("{} ended.", this.getClass().getName());
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.confirmation.message"))
                    .withLink(applicationProperties.toolkitUserFolder())
                    .withWindowType(WindowType.BROWSER_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withImage(ImageFile.GOOD_JOB_PNG)
                    .buildAndDisplayWindow()
            );
        }
    }

}