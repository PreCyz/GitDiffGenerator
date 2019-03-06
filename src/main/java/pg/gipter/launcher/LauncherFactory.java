package pg.gipter.launcher;

import javafx.stage.Stage;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.UILauncher;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage stage) {
        if (applicationProperties.isUseUI()) {
            return new UILauncher(stage, applicationProperties);
        }
        return new CLILauncher(applicationProperties);
    }
}
