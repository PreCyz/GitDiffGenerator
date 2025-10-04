package pg.gipter.ui;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.ui.job.*;
import pg.gipter.ui.main.*;
import pg.gipter.ui.menu.*;
import pg.gipter.ui.project.*;
import pg.gipter.ui.project.toolkit.ToolkitProjectsController;
import pg.gipter.ui.project.toolkit.ToolkitProjectsWindow;
import pg.gipter.ui.upgrade.UpgradeController;
import pg.gipter.ui.upgrade.UpgradeWindow;

/** Created by Gawa 2017-10-04 */
public enum WindowFactory {
    MAIN {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return applicationProperties.uiTheme() == UITheme.DEFAULT ?
                    new MainWindow(new MainController(applicationProperties, uiLauncher)) :
                    new MainThemedWindow(new MainController(applicationProperties, uiLauncher));
        }
    },
    JOB {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return applicationProperties.uiTheme() == UITheme.DEFAULT ?
                    new JobWindow(new JobController(applicationProperties, uiLauncher)) :
                    new JobThemedWindow(new JobController(applicationProperties, uiLauncher));
        }
    },
    PROJECTS {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return applicationProperties.uiTheme() == UITheme.DEFAULT ?
                    new ProjectsWindow(new ProjectsController(applicationProperties, uiLauncher)) :
                    new ProjectsThemedWindow(new ProjectsController(applicationProperties, uiLauncher));

        }
    },
    APPLICATION_MENU {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return applicationProperties.uiTheme() == UITheme.DEFAULT ?
                    new ApplicationSettingsWindow(new ApplicationSettingsController(applicationProperties, uiLauncher)) :
                    new ApplicationSettingsThemedWindow(new ApplicationSettingsController(applicationProperties, uiLauncher));
        }
    },
    TOOLKIT_MENU {
        @Override
        public AbstractWindow createWindow(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
            return applicationProperties.uiTheme() == UITheme.DEFAULT ?
                    new ToolkitSettingsWindow(new ToolkitSettingsController(applicationProperties, uiLauncher)) :
                    new ToolkitSettingsThemedWindow(new ToolkitSettingsController(applicationProperties, uiLauncher));
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
