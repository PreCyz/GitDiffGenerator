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
import java.time.format.DateTimeFormatter;

/**Created by Pawel Gawedzki on 17-Sep-2018*/
public class Main extends Application {

    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String jarDirectory = jarFile.getPath().replace(jarFile.getName(), "");
        String errMsg = String.format("Hey!!%nIt's Gipter here && I have bad news!%nYour copyright item was not uploaded.%n" +
                "Check the logs to find out why.%nLogs are located here:%n%slogs", jarDirectory);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oh Chicken Face ('> ");
        alert.setHeaderText(null);
        alert.setContentText(errMsg);

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
            logger.error("Diff upload complete.");

            logger.error("Program is terminated.");
            System.exit(0);
        } catch (Exception ex) {
            logger.error("Diff upload failure. Program is terminated.");
            //TODO: inform user upload was unsuccessful
            launch(args);
            System.exit(-1);
        }
    }

}
