package pg.gipter.testfx.main;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
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
import pg.gipter.core.dao.configuration.CachedConfiguration;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.vcs.VcsService;
import pg.gipter.testfx.UITestUtils;
import pg.gipter.ui.AbstractWindow;
import pg.gipter.ui.UILauncher;
import pg.gipter.ui.WindowFactory;
import pg.gipter.ui.main.MainController;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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
                ApplicationPropertiesFactory.getInstance(new String[]{
                        ArgName.projectPath + "=" + Paths.get(".").toFile().getAbsolutePath(),
                        ArgName.itemPath + "=" + Paths.get(".").toFile().getAbsolutePath()
                }),
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
        assertThat(windowObject.getTrayButton().isDisabled()).isTrue();
        assertThat(windowObject.getAddConfigurationButton().isDisabled()).isFalse();
        assertThat(windowObject.getExecuteButton().isDisabled()).isTrue();
        assertThat(windowObject.getExecuteAllButton().isDisabled()).isTrue();
        assertThat(windowObject.getConfigurationNameComboBox().isDisabled()).isTrue();
        assertThat(windowObject.getProjectPathButton().isDisabled()).isTrue();
        assertThat(windowObject.getToolkitProjectListNamesTextField().isDisabled()).isTrue();
        assertThat(windowObject.getDeleteDownloadedFilesCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isTrue();
    }

    @Test
    void givenSIMPLEConfiguration_whenSaveButtonPressed_thenConfigurationSaved(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot);

        windowObject.pressAddConfigurationButton()
                .enterTextInDialog("config")
                .pressEnter()
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
        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isTrue();
    }

    @Test
    void givenPROTECTEDConfiguration_whenSaveButtonPressed_thenConfigurationSaved(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("config")
                .pressEnter()
                .pressOkOnPopup()
                .chooseItemType(ItemType.PROTECTED)
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
        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isTrue();
    }

    @Test
    void givenSTATEMENTConfiguration_whenSaveButtonPressed_thenConfigurationSavedAndProjectButtonIsDisabled(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("config")
                .pressEnter()
                .pressOkOnPopup()
                .chooseItemType(ItemType.STATEMENT)
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
        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isTrue();
    }

    @Test
    void givenTOOLKIT_DOCSItemType_whenAddConfiguration_thenDisabledFields(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("config")
                .pressEnter()
                .pressOkOnPopup()
                .chooseItemType(ItemType.TOOLKIT_DOCS)
                .pressSaveButton()
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
    @Disabled("From some reason it does not work.")
    void given2DifferentConfigs_whenRemoveFirstConfig_thenOneIsRemovedAndLoaded(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("code")
                .pressEnter()
                .pressOkOnPopup()
                .enterAuthor("testAuthor")
                .checkFetchAll()
                .enterFetchTimeout(12)
                .uncheckSkipRemote()
                .pressSaveButton()
                .pressOkOnPopup()

                .pressAddConfigurationButton()
                .enterTextInDialog("docs")
                .pressEnter()
                .pressOkOnPopup()
                .chooseItemType(ItemType.TOOLKIT_DOCS)
                .enterListNames("Deliverables,General")
                .pressSaveButton()
                .pressOkOnPopup()

                .chooseConfiguration("code")
                .pressRemoveConfigurationButton()
                .pressOkOnPopup()
                .waitInSeconds(1d);

        assertThat(windowObject.getConfigurationNameComboBox().getValue()).isEqualTo("docs");
        assertThat(windowObject.getToolkitProjectListNamesTextField().getText()).isEqualTo("Deliverables,General");
        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isTrue();
        assertThat(windowObject.getItemTypeComboBox().getValue()).isEqualTo(ItemType.TOOLKIT_DOCS);
    }

    @Test
    void givenWrongToolkitCredentials_whenClickVerifyCredentials_thenAlertWithErrorMsg(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .clickVerifyCredentialsHyperlink();

        assertThat(windowObject.getVerifyCredentialsHyperLink().isVisited()).isFalse();
    }

    @Test
    void givenDifferentConfigUserThanEnteredUser_whenGitAuthorFocusLost_thenUseDefaultAuthorEnabled(FxRobot robot) {
        when(vcsService.getUserName()).thenReturn(Optional.of("configAuthor"));
        doNothing().when(vcsService).setProjectPath(anyString());
        MainController controller = (MainController) window.getController();
        controller.setVcsService(vcsService);

        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("code")
                .pressEnter()
                .pressOkOnPopup()
                .enterGitAuthor("testAuthor")
                .enterAuthor("configAuthor");

        assertThat(windowObject.getUseDefaultAuthorCheckBox().isDisabled()).isFalse();
    }

    @Test
    void givenDifferentConfigEmailThanEnteredEmail_whenCommitterEmailFocusLost_thenUseDefaultEmailEnabled(FxRobot robot) {
        when(vcsService.getUserEmail()).thenReturn(Optional.of("config@email.com"));
        doNothing().when(vcsService).setProjectPath(anyString());
        MainController controller = (MainController) window.getController();
        controller.setVcsService(vcsService);

        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("code")
                .pressEnter()
                .pressOkOnPopup()
                .enterCommitterEmail("test@email.com")
                .enterAuthor("configAuthor");

        assertThat(windowObject.getUseDefaultEmailCheckBox().isDisabled()).isFalse();
    }

    @Test
    void givenDifferentConfigEmailThanEnteredEmail_whenPressUseDefaultEmailCheckBox_thenEmailIsEqualDefault(FxRobot robot) {
        when(vcsService.getUserEmail()).thenReturn(Optional.of("config@email.com"));
        doNothing().when(vcsService).setProjectPath(anyString());
        MainController controller = (MainController) window.getController();
        controller.setVcsService(vcsService);

        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("code")
                .pressEnter()
                .pressOkOnPopup()
                .enterCommitterEmail("test@email.com")
                .enterAuthor("configAuthor")
                .checkDefaultEmail();

        assertThat(windowObject.getCommitterEmailTextField().getText()).isEqualTo("config@email.com");
    }

    @Test
    void givenDifferentConfigUserThanEnteredGitAuthor_whenPressUseDefaultAuthorCheckBox_thenGitAuthorIsEqualDefault(FxRobot robot) {
        when(vcsService.getUserName()).thenReturn(Optional.of("configUserName"));
        doNothing().when(vcsService).setProjectPath(anyString());
        MainController controller = (MainController) window.getController();
        controller.setVcsService(vcsService);

        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("code")
                .pressEnter()
                .pressOkOnPopup()
                .enterGitAuthor("enteredUserName")
                .enterAuthor("configAuthor")
                .checkDefaultAuthor();

        assertThat(windowObject.getGitAuthorTextField().getText()).isEqualTo("configUserName");
    }

    @Test
    void whenPressUseDefaultCheckBoxesTwice_thenGitAuthorAndEmailEqualsEntered(FxRobot robot) {
        when(vcsService.getUserName()).thenReturn(Optional.of("configUserName"));
        when(vcsService.getUserEmail()).thenReturn(Optional.of("config@email.com"));
        doNothing().when(vcsService).setProjectPath(anyString());
        MainController controller = (MainController) window.getController();
        controller.setVcsService(vcsService);

        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("code")
                .pressEnter()
                .pressOkOnPopup()
                .enterGitAuthor("enteredUserName")
                .enterCommitterEmail("entered@email.com")
                .pressSaveButton()
                .pressOkOnPopup()
                .checkDefaultAuthor()
                .checkDefaultEmail()
                .uncheckDefaultAuthor()
                .uncheckDefaultEmail();

        assertThat(windowObject.getGitAuthorTextField().getText()).isEqualTo("enteredUserName");
        assertThat(windowObject.getCommitterEmailTextField().getText()).isEqualTo("entered@email.com");
    }

    @Test
    void givenPROTECTEDItemTypeAndDisabledFetchAll_whenAddConfiguration_thenDisabledFetchTimeout(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("config")
                .pressEnter()
                .pressOkOnPopup()
                .chooseItemType(ItemType.PROTECTED)
                .uncheckFetchAll()
                .pressSaveButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getFetchAllCheckBox().isSelected()).isFalse();
        assertThat(windowObject.getFetchTimeoutTextField().isDisabled()).isTrue();
    }

    @Test
    void givenSIMPLEItemTypeAndEnabledFetchAll_whenAddConfiguration_thenDisabledFetchTimeout(FxRobot robot) {
        final MainWindowObject windowObject = new MainWindowObject(robot)
                .pressAddConfigurationButton()
                .enterTextInDialog("config")
                .pressEnter()
                .pressOkOnPopup()
                .checkFetchAll()
                .enterFetchTimeout(3)
                .pressSaveButton()
                .pressOkOnPopup();

        final Map<String, RunConfig> map = dao.loadRunConfigMap();

        assertThat(map.keySet()).containsExactly("config");
        assertThat(windowObject.getFetchAllCheckBox().isSelected()).isTrue();
        assertThat(windowObject.getFetchTimeoutTextField().isDisabled()).isFalse();
    }
}
