package pg.gipter;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.DiffUploader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**Created by Pawel Gawedzki on 17-Sep-2018*/
public class Main extends Application {

    private static ApplicationProperties applicationProperties;
    private static boolean error = false;


    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        if (error) {
            String errMsg = createErrorMessage();
            displayWindow(errMsg, Alert.AlertType.ERROR);
        } else if (applicationProperties.isConfirmation()) {
            String confirmationMsg = String.format(
                    "Your copyright item was uploaded successfully. If you do not believe me, check it here %s.",
                    applicationProperties.toolkitUserFolder()
            );
            displayWindow(confirmationMsg, Alert.AlertType.CONFIRMATION);
        }
    }

    private String createErrorMessage() throws URISyntaxException {
        File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String logsDirectory = jarFile.getPath().replace(jarFile.getName(), "logs");
        return String.format("Hey!!%nIt's Gipter here && I have bad news!%nYour copyright item was not uploaded.%n" +
                "Check the logs to find out why.%nLogs are located here:%n%s", logsDirectory);
    }

    private void displayWindow(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Oh Chicken Face ('> ");
        alert.setHeaderText(null);
        alert.setContentText(message);

        URL imgUrl = this.getClass().getClassLoader().getResource("img/chicken-face.jpg");
        if (imgUrl != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.toString()));
        }

        alert.showAndWait();
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        try {
            logger.info("Gipter started.");
            applicationProperties = new ApplicationProperties(args);

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
        } finally {
            launch(args);
            logger.info("Program is terminated.");
        }
        System.exit(-1);
    }

}
