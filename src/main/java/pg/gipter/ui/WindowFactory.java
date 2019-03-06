package pg.gipter.ui;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.job.JobController;
import pg.gipter.ui.job.JobWindow;
import pg.gipter.ui.main.MainController;
import pg.gipter.ui.main.MainWindow;

/**Created by Gawa 2017-10-04*/
public enum WindowFactory {
    MAIN {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new MainWindow(new MainController(applicationProperties, uiLauncher));
        }
    },
    JOB {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new JobWindow(new JobController(applicationProperties, uiLauncher));
        }
    };

    public abstract AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher);
}
