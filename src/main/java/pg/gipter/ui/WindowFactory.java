package pg.gipter.ui;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.ui.job.JobController;
import pg.gipter.ui.job.JobWindow;
import pg.gipter.ui.main.MainController;
import pg.gipter.ui.main.MainWindow;
import pg.gipter.ui.menu.ApplicationSettingsController;
import pg.gipter.ui.menu.ApplicationSettingsWindow;
import pg.gipter.ui.menu.ToolkitSettingsController;
import pg.gipter.ui.menu.ToolkitSettingsWindow;
import pg.gipter.ui.project.ProjectsController;
import pg.gipter.ui.project.ProjectsWindow;
import pg.gipter.ui.project.toolkit.ToolkitProjectsController;
import pg.gipter.ui.project.toolkit.ToolkitProjectsWindow;
import pg.gipter.ui.upgrade.UpgradeController;
import pg.gipter.ui.upgrade.UpgradeWindow;

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
    },
    PROJECTS {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ProjectsWindow(new ProjectsController(applicationProperties, uiLauncher));
        }
    },
    APPLICATION_MENU {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ApplicationSettingsWindow(new ApplicationSettingsController(applicationProperties, uiLauncher));
        }
    },
    TOOLKIT_MENU {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ToolkitSettingsWindow(new ToolkitSettingsController(applicationProperties, uiLauncher));
        }
    },
    TOOLKIT_PROJECTS {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new ToolkitProjectsWindow(new ToolkitProjectsController(applicationProperties, uiLauncher));
        }
    },
    UPGRADE {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return new UpgradeWindow(new UpgradeController(applicationProperties, uiLauncher));
        }
    };

    public abstract AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher);
}
