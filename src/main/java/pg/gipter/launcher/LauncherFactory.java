package pg.gipter.launcher;

import javafx.stage.Stage;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.WizardLauncher;

public class LauncherFactory {
    private LauncherFactory() {}

    public static Launcher getLauncher(ApplicationProperties applicationProperties, Stage stage) {
        PropertiesDao propertiesDao = DaoFactory.getPropertiesDao();
        if (propertiesDao.loadAllApplicationProperties().isEmpty()) {
            return new WizardLauncher(stage);
        }
        if (applicationProperties.isUseUI()) {
            return new UILauncher(stage, applicationProperties);
        }
        return new CLILauncher(applicationProperties);
    }
}
