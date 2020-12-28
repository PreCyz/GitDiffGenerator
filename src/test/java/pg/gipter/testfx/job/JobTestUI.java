package pg.gipter.testfx.job;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import pg.gipter.MockitoExtension;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.dao.data.DataDaoFactory;
import pg.gipter.jobs.*;
import pg.gipter.testfx.UITestUtils;
import pg.gipter.ui.*;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;


@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
public class JobTestUI {

    final JobService jobHandler = new JobService();

    @Mock
    private UILauncher uiLauncherMock;

    @AfterEach
    private void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
            Files.deleteIfExists(Paths.get(DaoConstants.DATA_JSON));
            Files.deleteIfExists(Paths.get(DaoConstants.DATA_PROPERTIES));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
    }

    @Start
    public void start(Stage stage) {
        try {
            UITestUtils.generateSimpleConfigurations(2);
            createWindow(stage);
        } catch (Exception ex) {
            System.err.printf("UPS !!! %s", ex.getMessage());
            teardown();
            fail("Something went terribly wrong");
        }
    }

    private void createWindow(Stage stage) throws IOException {
        AbstractWindow window = WindowFactory.JOB.createWindow(
                ApplicationPropertiesFactory.getInstance(new String[]{}),
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
    void given2Configs_whenJobWindowShown_thenConfigurationNameComboBoxContainsTheseConfigs(FxRobot robot) {
        List<String> actualItems = new JobWindowObject(robot).getConfigurationNameComboBoxItems();

        final Set<String> existingConfigNames = new LinkedHashSet<>(
                DaoFactory.getCachedConfiguration().loadRunConfigMap().keySet()
        );
        existingConfigNames.add("all-configs");

        assertThat(actualItems).containsAll(existingConfigNames);
    }

    @Test
    void givenConfigs_whenSetEVERY_WEEKJob_thenJobIsCreated(FxRobot robot) {
        when(uiLauncherMock.getJobService()).thenReturn(jobHandler);
        final JobWindowObject windowObject = new JobWindowObject(robot)
                .chooseDayOfWeek(DayOfWeek.MONDAY)
                .pressEveryWeekRadioButton()
                .chooseHourOfTheDay(8)
                .chooseMinuteOfHour(10)
                .chooseConfigEntry(2);

        final String actualJobType = windowObject.getJobTypeLabelText();
        assertThat(actualJobType).isEqualTo(JobType.EVERY_WEEK.name());
        String actualJobDetails = windowObject.getJobDetailsLabelText();
        String expectedJobDetails = String.format("SCHEDULE_START: %s%nDAY_OF_WEEK: %s%nHOUR_OF_THE_DAY: 8:10",
        LocalDate.now().format(DateTimeFormatter.ISO_DATE), DayOfWeek.MONDAY.name());
        assertThat(actualJobDetails).contains(expectedJobDetails);
        final LinkedList<String> configs = new LinkedList<>(DaoFactory.getCachedConfiguration().loadRunConfigMap().keySet());
        String actualLabelText = windowObject.getConfigsLabelText();
        assertThat(actualLabelText).contains(configs.getFirst(), configs.getLast());

        windowObject.pressScheduleButton();

        JobParam jobParam = DataDaoFactory.getDataDao().loadJobParam().orElseThrow(IllegalStateException::new);
        assertThat(jobParam).isNotNull();
        assertThat(jobParam.getConfigs()).hasSize(2);
        assertThat(jobParam.getNextFireDate()).isNotNull();
        assertThat(jobParam.getHourOfDay()).isEqualTo(8);
        assertThat(jobParam.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(jobParam.getScheduleStart()).isNotNull();
        assertThat(jobParam.getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(jobParam.getCronExpression()).isEmpty();
    }
}
