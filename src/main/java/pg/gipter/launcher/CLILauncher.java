package pg.gipter.launcher;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

class CLILauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(CLILauncher.class);
    private ApplicationProperties applicationProperties;
    private Runnable runner;


    CLILauncher(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        runner = new Runner(applicationProperties);
    }

    @Override
    public void execute() {
        boolean error = false;
        try {
            logger.info("Launching command line style.");
            runner.run();
        } catch (Exception ex) {
            logger.error("Diff upload failure. Program is terminated.");
            error = true;
            String errMsg = createErrorMessage();
            displayWindow(errMsg, Alert.AlertType.ERROR);
        }
        if (!error && applicationProperties.isConfirmationWindow()) {
            String confirmationMsg = String.format(
                    "Your copyright item was uploaded successfully. If you do not believe me, check it here %s.",
                    applicationProperties.toolkitUserFolder()
            );
            displayWindow(confirmationMsg, Alert.AlertType.INFORMATION);
        }

        logger.info("Program is terminated.");
        System.exit(-1);
    }

    private String createErrorMessage() {
        String errorMsg;
        try {
            File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            String logsDirectory = jarFile.getPath().replace(jarFile.getName(), "logs");
            errorMsg = String.format("Hey!!%nIt's Gipter here && I have bad news!%nYour copyright item was not uploaded.%n" +
                    "Check the logs to find out why.%nLogs are located here:%n%s", logsDirectory);
        } catch (URISyntaxException ex) {
            errorMsg = String.format("Hey!!%nIt's Gipter here && I have bad news!%nYour copyright item was not uploaded.%n" +
                    "Check the logs to find out why.%nLogs are located in Gipter home folder.");
        }
        return errorMsg;
    }

    private void displayWindow(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Oh Chicken Face ('> ");
        alert.setHeaderText(null);
        alert.setContentText(message);

        URL imgUrl = getClass().getClassLoader().getResource("img/chicken-face.jpg");
        if (imgUrl != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(imgUrl.toString()));
        }

        alert.showAndWait();
    }
}
