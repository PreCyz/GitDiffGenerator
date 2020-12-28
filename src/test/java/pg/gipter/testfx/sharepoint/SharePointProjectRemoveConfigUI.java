package pg.gipter.testfx.sharepoint;

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
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.testfx.UITestUtils;
import pg.gipter.ui.*;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class SharePointProjectRemoveConfigUI {

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
            UITestUtils.generateConfigurationWithSPC(2);
            createWindow(stage);
        } catch (Exception ex) {
            System.err.printf("UPS !!! %s", ex.getMessage());
            teardown();
            fail("Something went terribly wrong");
        }
    }

    private void createWindow(Stage stage) throws IOException {
        AbstractWindow window = WindowFactory.SHARE_POINT_PROJECTS.createWindow(
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
    void given2SharePointConfigs_whenRemoveConfigPressed_thenOnlyOneLeftInComboBoxAndInTheFile(FxRobot robot) {
        when(uiLauncherMock.hasWizardProperties()).thenReturn(false);
        final SharePointConfigWindowObject windowObject = new SharePointConfigWindowObject(robot)
                .chooseComboBoxEntry(1)
                .clickRemoveButton();

        int actualItemsSize = windowObject.getComboBoxSize();
        assertThat(actualItemsSize).isEqualTo(1);

        actualItemsSize = windowObject.clickRemoveButton().getComboBoxSize();
        assertThat(actualItemsSize).isEqualTo(0);

        assertThat(windowObject.getName()).isEmpty();
        assertThat(windowObject.getUsername()).isEmpty();
        assertThat(windowObject.getPassword()).isEmpty();
        assertThat(windowObject.getDomain()).isEmpty();
        assertThat(windowObject.getUrl()).isEmpty();
        assertThat(windowObject.getProject()).isEmpty();
        assertThat(windowObject.getListNames()).isEmpty();
        assertThat(windowObject.getSharePointLink()).isEqualTo("Full link will appear here");
    }
}