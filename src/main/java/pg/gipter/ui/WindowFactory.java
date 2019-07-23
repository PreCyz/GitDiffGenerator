package pg.gipter.ui;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.configuration.ConfigurationController;
import pg.gipter.ui.configuration.ConfigurationWindow;
import pg.gipter.ui.job.JobController;
import pg.gipter.ui.job.JobWindow;
import pg.gipter.ui.main.MainController;
import pg.gipter.ui.main.MainWindow;
import pg.gipter.ui.menu.ApplicationMenuController;
import pg.gipter.ui.menu.ApplicationMenuWindow;
import pg.gipter.ui.menu.ToolkitMenuController;
import pg.gipter.ui.menu.ToolkitMenuWindow;
import pg.gipter.ui.project.ProjectsController;
import pg.gipter.ui.project.ProjectsWindow;

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
            return new JobWindow(new JobController(uiLauncher));
        }
    },
    PROJECTS {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ProjectsWindow(new ProjectsController(applicationProperties, uiLauncher));
        }
    },
    NEW_CONFIGURATION {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ConfigurationWindow(new ConfigurationController(applicationProperties, uiLauncher));
        }
    },
    APPLICATION_MENU {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ApplicationMenuWindow(new ApplicationMenuController(applicationProperties, uiLauncher));
        }
    },
    TOOLKIT_MENU {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ToolkitMenuWindow(new ToolkitMenuController(applicationProperties, uiLauncher));
        }
    };

    public abstract AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher);
}
