package pg.gipter.testfx.applicationconfig;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.SchedulerException;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import pg.gipter.MockitoExtension;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.model.ApplicationConfig;
import pg.gipter.jobs.JobCreator;
import pg.gipter.jobs.JobCreatorFactory;
import pg.gipter.jobs.JobService;
import pg.gipter.testfx.UITestUtils;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.WindowFactory;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
public class ApplicationConfigTraySupportedUI {

    @Mock
    private UILauncher uiLauncherMock;

    @AfterEach
    private void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
    }

    @Start
    public void start(Stage stage) {
        try {
            UITestUtils.generateConfigurationWithSPC(0);
            createWindow(stage);
        } catch (Exception ex) {
            System.err.printf("UPS !!! %s", ex.getMessage());
            teardown();
            fail("Something went terribly wrong");
        }
    }

    private void createWindow(Stage stage) throws IOException {
        when(uiLauncherMock.isTrayActivated()).thenReturn(true);
        when(uiLauncherMock.isTraySupported()).thenReturn(true);
        AbstractWindow window = WindowFactory.APPLICATION_MENU.createWindow(
                ApplicationPropertiesFactory.getInstance(new String[]{ArgName.configurationName + "=testConfiguration"}),
                uiLauncherMock
        );

        Scene scene = new Scene(window.root());
        if (!StringUtils.nullOrEmpty(window.css())) {
            scene.getStylesheets().add(window.css());
        }
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.toFront();
    }

    @Test
    void whenLanguageChangeToPL_thenAllLabelsTextsArePL(FxRobot robot) {
        doNothing().when(uiLauncherMock).changeApplicationSettingsWindowTitle();

        final ApplicationConfigWindowObject windowObject = new ApplicationConfigWindowObject(robot)
                .chooseLanguage("pl");

        assertThat(BundleUtils.getBundleName()).isEqualTo("bundle.translation_pl");
        assertThat(windowObject.getLanguageLabelText()).isEqualTo("Język");
    }

    @Test
    void whenDeselectConfirmationWindow_thenConfigurationIsSaved(FxRobot robot) {
        new ApplicationConfigWindowObject(robot).deselectConfirmationWindow();

        final ApplicationConfig applicationConfig = DaoFactory.getCachedConfiguration().loadApplicationConfig();

        assertThat(applicationConfig.getConfirmationWindow()).isFalse();
    }

    @Test
    void whenWindowLoaded_thenPreferredArgSourceComboBoxAndUseUICheckBoxDisabled(FxRobot robot) {
        final ApplicationConfigWindowObject windowObject = new ApplicationConfigWindowObject(robot);

        assertThat(windowObject.getUseUICheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getPreferredArgSourceCheckbox().isDisabled()).isTrue();
    }

    @Test
    void whenWindowLoaded_thenCheckLastItemCheckBoxSelected(FxRobot robot) {
        final ApplicationConfigWindowObject windowObject = new ApplicationConfigWindowObject(robot);

        assertThat(windowObject.getCheckLastItemCheckBox().isSelected()).isTrue();
    }

    @Test
    void whenDeselectCheckLastItem_thenJobCanceled(FxRobot robot) throws SchedulerException {
        final JobService jobService = spy(JobService.class);
        final JobCreator jobCreator = JobCreatorFactory.lastItemJobCreator(
                ApplicationPropertiesFactory.getInstance(new String[]{ArgName.configurationName + "=testConfiguration"})
        );
        jobService.scheduleJob(jobCreator);
        when(uiLauncherMock.getJobService()).thenReturn(jobService);
        Mockito.reset(jobService);

        new ApplicationConfigWindowObject(robot).deselectCheckLastItem();

        final ApplicationConfig applicationConfig = DaoFactory.getCachedConfiguration().loadApplicationConfig();
        assertThat(applicationConfig.getCheckLastItemEnabled()).isFalse();
        verify(jobService, times(0)).scheduleJob(eq(jobCreator));
        verify(jobService, times(1)).deleteJob(any());
        assertThat(jobService.isJobExist(jobCreator)).isFalse();
    }

    @Test
    void whenSelectCheckLastItem_thenJobScheduled(FxRobot robot) throws SchedulerException {
        final JobService jobService = spy(JobService.class);
        final JobCreator jobCreator = JobCreatorFactory.lastItemJobCreator(
                ApplicationPropertiesFactory.getInstance(new String[]{ArgName.configurationName + "=testConfiguration"})
        );
        jobService.scheduleJob(jobCreator);
        when(uiLauncherMock.getJobService()).thenReturn(jobService);

        new ApplicationConfigWindowObject(robot).deselectCheckLastItem()
                .selectCheckLastItem();

        final ApplicationConfig applicationConfig = DaoFactory.getCachedConfiguration().loadApplicationConfig();
        assertThat(applicationConfig.getCheckLastItemEnabled()).isTrue();
        verify(jobService, times(1)).deleteJob(any());
        assertThat(jobService.isJobExist(jobCreator)).isTrue();
    }

}
