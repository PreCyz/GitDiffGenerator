package pg.gipter.launcher;

import javafx.stage.Stage;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.WizardLauncher;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage stage) {
        ConfigurationDao configurationDao = DaoFactory.getConfigurationDao();
        if (configurationDao.loadRunConfigMap().isEmpty()) {
            return new WizardLauncher(stage);
        }
        if (applicationProperties.isUseUI()) {
            return new UILauncher(stage, applicationProperties);
        }
        return new CLILauncher(applicationProperties);
    }
}
