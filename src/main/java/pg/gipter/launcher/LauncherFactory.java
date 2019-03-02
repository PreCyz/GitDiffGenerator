package pg.gipter.launcher;

import javafx.stage.Stage;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.UILauncher;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage primaryStage) {
        if (applicationProperties.isUseUI()) {
            return new UILauncher(primaryStage, applicationProperties);
        }
        return new CLILauncher(applicationProperties);
    }
}
