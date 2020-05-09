package pg.gipter.launchers;

import javafx.stage.Stage;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.WizardLauncher;

import java.util.Set;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage stage) {
        Set<String> configNameSet = applicationProperties.getRunConfigMap().keySet();
        if (configNameSet.isEmpty()) {
            return new WizardLauncher(stage);
        }
        if (applicationProperties.isUseUI()) {
            return new UILauncher(stage, applicationProperties);
        }
        return new CLILauncher(applicationProperties);
    }
}
