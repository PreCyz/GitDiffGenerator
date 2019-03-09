package pg.gipter.launcher;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.ui.AbstractController;
import pg.gipter.util.BundleUtils;
import pg.gipter.util.PropertiesHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;

public class Runner {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private ApplicationProperties applicationProperties;
    private final PropertiesHelper propertiesHelper;

    public Runner(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.propertiesHelper = new PropertiesHelper();
    }

    public void run() {
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
            String errMsg = createErrorMessage();
            displayWindowFxSupport(errMsg, Alert.AlertType.ERROR);
        } finally {
            saveUploadInfo(error ? "FAIL" : "SUCCESS");
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            String confirmationMsg = BundleUtils.getMsg("popup.confirmation.message", applicationProperties.toolkitUserFolder());
            displayWindowFxSupport(confirmationMsg, Alert.AlertType.INFORMATION);
        }
    }

    private String createErrorMessage() {
        String errorMsg;
        try {
            File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            String logsDirectory = jarFile.getPath().replace(jarFile.getName(), "logs");
            errorMsg = BundleUtils.getMsg("popup.error.messageWithLog", logsDirectory);
        } catch (URISyntaxException ex) {
            errorMsg = BundleUtils.getMsg("popup.error.messageWithoutLog");
        }
        return errorMsg;
    }

    private void displayWindow(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(BundleUtils.getMsg("popup.title"));
        alert.setHeaderText(BundleUtils.getMsg("popup.header.error"));
        alert.setContentText(message);

        AbstractController.setImageOnAlertWindow(alert);

        alert.showAndWait();
    }

    private void displayWindowFxSupport(String message, Alert.AlertType alertType) {
        if (!applicationProperties.isUseUI()) {
            displayWindow(message, alertType);
        } else if (Platform.isFxApplicationThread()) {
            displayWindow(message, alertType);
        } else {
            Platform.runLater(() -> displayWindow(message, alertType));
        }
    }

    private void saveUploadInfo(String status) {
        Optional<Properties> dataProperties = propertiesHelper.loadDataProperties();
        Properties data = dataProperties.orElseGet(Properties::new);
        data.put(PropertiesHelper.UPLOAD_DATE_TIME_KEY, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        data.put(PropertiesHelper.UPLOAD_STATUS_KEY, status);
        propertiesHelper.saveToDataProperties(data);
    }

}