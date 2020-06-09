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
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.testfx.UITestUtils;
import pg.gipter.ui.*;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class SharePointProjectAddConfigTestUI {

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
    void givenSharePointConfigWindow_whenEnterSharePointConfigAndSave_thenItIsStored(FxRobot robot) {
        when(uiLauncherMock.hasWizardProperties()).thenReturn(false);

        final SharePointConfigWindowObject windowObject = new SharePointConfigWindowObject(robot)
                .writeName("someName")
                .writeUsername("someUser")
                .writePassword("somePassword")
                .writeDomain("someDomain")
                .clickAddButton();

        assertThat(windowObject.getComboBoxSize()).isEqualTo(1);
        final Optional<RunConfig> runConfig = DaoFactory.getCachedConfiguration().loadRunConfig("testConfiguration");
        assertThat(runConfig.isPresent()).isTrue();
        runConfig.ifPresent(rc -> {
            assertThat(rc.getSharePointConfigs()).hasSize(1);
            final SharePointConfig spc = new LinkedList<>(rc.getSharePointConfigs()).getFirst();
            assertThat(spc.getName()).isEqualTo("someName");
            assertThat(spc.getUsername()).isEqualTo("someUser");
            assertThat(spc.getPassword()).isNotNull();
            assertThat(spc.getDomain()).isEqualTo("someDomain");
            assertThat(windowObject.getComboBoxItems().get(0)).isEqualTo(spc);
        });
        assertThat(windowObject.getSharePointLink()).isNotBlank();
    }

}