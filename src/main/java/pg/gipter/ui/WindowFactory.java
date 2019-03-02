package pg.gipter.ui;

import pg.gipter.settings.ApplicationProperties;

import java.util.ResourceBundle;

/**Created by Gawa 2017-10-04*/
public enum WindowFactory {
    MAIN {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher, ResourceBundle bundle) {
            return new MainWindow(new MainController(applicationProperties, uiLauncher), bundle);
        }
    };

    public abstract AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher UILauncher, ResourceBundle bundle);
}
