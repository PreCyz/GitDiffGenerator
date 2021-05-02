package pg.gipter.launchers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.producers.DiffProducer;
import pg.gipter.core.producers.DiffProducerFactory;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.ui.UploadStatus;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;

class Runner implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private final ApplicationProperties applicationProperties;
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
                    .withLinkAction(new LogLinkAction())
                    .withAlertType(Alert.AlertType.ERROR)
                    .withWebViewDetails(new WebViewDetails(new WebViewService().createImageView(ImageFile.ERROR_CHICKEN_PNG)))
                    .buildAndDisplayWindow()
            );
        } finally {
            dataDao.saveUploadStatus(error ? UploadStatus.FAIL : UploadStatus.SUCCESS);
            logger.info("{} ended.", this.getClass().getName());
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.confirmation.message"))
                    .withLinkAction(new BrowserLinkAction(applicationProperties.toolkitUserFolder()))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWebViewDetails(new WebViewDetails(new WebViewService().createImageView((ImageFile.GOOD_JOB_PNG))))
                    .buildAndDisplayWindow()
            );
        }
    }

}