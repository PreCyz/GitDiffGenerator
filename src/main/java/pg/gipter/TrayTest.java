package pg.gipter;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pg.gipter.ui.UICustomization;

public class TrayTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        new UICustomization(stage, null).createTrayIcon();
        Scene scene = new Scene(new Group(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}