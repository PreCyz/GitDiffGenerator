package pg.gipter.launcher;

import javafx.stage.Stage;
import pg.gipter.settings.ApplicationProperties;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage primaryStage) {
        if (applicationProperties.isUseUI()) {
            return new FXMLLauncher(applicationProperties, primaryStage);
        }
        return new CLILauncher(applicationProperties);
    }
}
