package pg.gipter.launchers;

import javafx.stage.Stage;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.ui.UILauncher;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage stage) {
        if (applicationProperties.isUseUI()) {
            return new UILauncher(stage, applicationProperties);
        }
        return new CLILauncher(applicationProperties);
    }

    public static Launcher getLauncher(ApplicationProperties applicationProperties) {
        return new CLILauncher(applicationProperties);
    }
}
