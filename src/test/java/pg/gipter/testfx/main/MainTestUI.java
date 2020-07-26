package pg.gipter.testfx.main;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import pg.gipter.MockitoExtension;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.CachedConfiguration;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.testfx.UITestUtils;
import pg.gipter.ui.*;
import pg.gipter.ui.main.MainController;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
public class MainTestUI {

    @Mock
    private UILauncher uiLauncherMock;
    @Mock
    private VcsService vcsService;

    private final CachedConfiguration dao = DaoFactory.getCachedConfiguration();
    private AbstractWindow window;

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
            UITestUtils.generateDefaultConfig();
            createWindow(stage);
        } catch (Exception ex) {
            System.err.printf("UPS !!! %s", ex.getMessage());
            teardown();
            fail("Something went terribly wrong");
        }
    }

    private void createWindow(Stage stage) throws IOException {
        window = WindowFactory.MAIN.createWindow(
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
    void givenNoConfiguration_whenSaveButtonPressed_thenNoConfigurationSavedAndSpecificControlsAreDisabled(FxRobot robot) {
        MainWindowObject windowObject = new MainWindowObject(robot)
                .pressSaveButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map).hasSize(0);
        assertThat(windowObject.getJobButton().isDisabled()).isTrue();
        assertThat(windowObject.getAddConfigurationButton().isDisabled()).isTrue();
        assertThat(windowObject.getExecuteButton().isDisabled()).isTrue();
        assertThat(windowObject.getExecuteAllButton().isDisabled()).isTrue();
        assertThat(windowObject.getConfigurationNameComboBox().isDisabled()).isTrue();
        assertThat(windowObject.getProjectPathButton().isDisabled()).isTrue();
        assertThat(windowObject.getToolkitProjectListNamesTextField().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isTrue();
    }

    @Test
    void givenSIMPLEConfiguration_whenSaveButtonPressed_thenConfigurationSaved(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot);
        verifyThat(windowObject.getAddConfigurationButton(), Node::isDisabled);

        windowObject.enterConfigurationName("config")
                .pressAddConfigurationButton()
                .pressOkOnPopup()
                .pressSaveButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getJobButton().isDisabled()).isFalse();
        assertThat(windowObject.getAddConfigurationButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteAllButton().isDisabled()).isFalse();
        assertThat(windowObject.getConfigurationNameComboBox().isDisabled()).isFalse();
        assertThat(windowObject.getProjectPathButton().isDisabled()).isFalse();
        assertThat(windowObject.getToolkitProjectListNamesTextField().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getStartDatePicker().isDisabled()).isFalse();
        assertThat(windowObject.getEndDatePicker().isDisabled()).isFalse();
    }

    @Test
    void givenPROTECTEDConfiguration_whenSaveButtonPressed_thenConfigurationSaved(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot);
        verifyThat(windowObject.getAddConfigurationButton(), Node::isDisabled);

        windowObject.enterConfigurationName("config")
                .chooseItemType(ItemType.PROTECTED)
                .pressAddConfigurationButton()
                .pressOkOnPopup()
                .pressSaveButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getJobButton().isDisabled()).isFalse();
        assertThat(windowObject.getAddConfigurationButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteAllButton().isDisabled()).isFalse();
        assertThat(windowObject.getConfigurationNameComboBox().isDisabled()).isFalse();
        assertThat(windowObject.getProjectPathButton().isDisabled()).isFalse();
        assertThat(windowObject.getToolkitProjectListNamesTextField().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getStartDatePicker().isDisabled()).isFalse();
        assertThat(windowObject.getEndDatePicker().isDisabled()).isFalse();
    }

    @Test
    void givenSTATEMENTConfiguration_whenSaveButtonPressed_thenConfigurationSavedAndProjectButtonIsDisabled(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot);
        verifyThat(windowObject.getAddConfigurationButton(), Node::isDisabled);

        windowObject.enterConfigurationName("config")
                .chooseItemType(ItemType.STATEMENT)
                .pressAddConfigurationButton()
                .pressOkOnPopup()
                .pressSaveButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getJobButton().isDisabled()).isFalse();
        assertThat(windowObject.getAddConfigurationButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteAllButton().isDisabled()).isFalse();
        assertThat(windowObject.getConfigurationNameComboBox().isDisabled()).isFalse();
        assertThat(windowObject.getProjectPathButton().isDisabled()).isTrue();
        assertThat(windowObject.getToolkitProjectListNamesTextField().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getStartDatePicker().isDisabled()).isFalse();
        assertThat(windowObject.getEndDatePicker().isDisabled()).isFalse();
    }

    @Test
    void givenTOOLKIT_DOCSItemType_whenAddConfiguration_thenDisabledFields(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .enterConfigurationName("config")
                .chooseItemType(ItemType.TOOLKIT_DOCS)
                .pressAddConfigurationButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getAuthorsTextField().isDisabled()).isTrue();
        assertThat(windowObject.getCommitterEmailTextField().isDisabled()).isTrue();
        assertThat(windowObject.getGitAuthorTextField().isDisabled()).isTrue();
        assertThat(windowObject.getMercurialAuthorTextField().isDisabled()).isTrue();
        assertThat(windowObject.getSvnAuthorTextField().isDisabled()).isTrue();
        assertThat(windowObject.getSkipRemoteCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getFetchAllCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isFalse();
        assertThat(windowObject.getStartDatePicker().isDisabled()).isFalse();
        assertThat(windowObject.getEndDatePicker().isDisabled()).isTrue();
    }

    @Test
    void givenSHARE_POINT_DOCSItemType_whenAddConfiguration_thenDisabledFields(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .enterConfigurationName("config")
                .chooseItemType(ItemType.SHARE_POINT_DOCS)
                .pressAddConfigurationButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getAuthorsTextField().isDisabled()).isTrue();
        assertThat(windowObject.getCommitterEmailTextField().isDisabled()).isTrue();
        assertThat(windowObject.getGitAuthorTextField().isDisabled()).isTrue();
        assertThat(windowObject.getMercurialAuthorTextField().isDisabled()).isTrue();
        assertThat(windowObject.getSvnAuthorTextField().isDisabled()).isTrue();
        assertThat(windowObject.getSkipRemoteCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getFetchAllCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isFalse();
        assertThat(windowObject.getStartDatePicker().isDisabled()).isFalse();
        assertThat(windowObject.getEndDatePicker().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isTrue();
    }

    @Test
    void given2DifferentConfigs_whenRemoveFirstConfig_thenOneIsRemovedAndLoaded(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .enterConfigurationName("code")
                .enterAuthor("testAuthor")
                .checkFetchAll()
                .uncheckSkipRemote()
                .chooseItemType(ItemType.SIMPLE)
                .pressAddConfigurationButton()
                .pressOkOnPopup()

                .enterConfigurationName("docs")
                .chooseItemType(ItemType.TOOLKIT_DOCS)
                .enterListNames("Deliverables,General")
                .pressAddConfigurationButton()
                .pressOkOnPopup()

                .chooseConfiguration("code")
                .pressRemoveConfigurationButton()
                .pressOkOnPopup();

        assertThat(windowObject.getConfigurationNameTextField().getText()).isEqualTo("docs");
        assertThat(windowObject.getConfigurationNameComboBox().getValue()).isEqualTo("docs");
        assertThat(windowObject.getItemTypeComboBox().getValue()).isEqualTo(ItemType.TOOLKIT_DOCS);
        assertThat(windowObject.getToolkitProjectListNamesTextField().getText()).isEqualTo("Deliverables,General");
    }

    @Test
    void givenWrongToolkitCredentials_whenClickVerifyCredentials_thenAlertWithErrorMsg(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .clickVerifyCredentialsHyperlink();

        assertThat(windowObject.getVerifyCredentialsHyperLink().isVisited()).isFalse();
    }

    @Test
    @Disabled
    void givenDifferentConfigUserThanEnteredUser_whenGitAuthorFocusLost_thenUseDefaultAuthorEnabled(FxRobot robot) {
        when(vcsService.getUserName()).thenReturn(Optional.of("configAuthor"));
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .enterConfigurationName("code")
                .pressAddConfigurationButton()
                .pressOkOnPopup();

        MainController controller = (MainController) window.getController();
        controller.setVcsService(vcsService);
        Optional<RunConfig> code = dao.loadRunConfig("code");
        code.ifPresent(rc -> {
            rc.setProjectPath(".");
            dao.updateCachedConfiguration(rc);
        });

        Platform.runLater(() -> {
            controller.initialize(null, BundleUtils.loadBundle());
            windowObject.enterGitAuthor("testAuthor").clickAuthor();
            assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isFalse();
        });
    }
}
