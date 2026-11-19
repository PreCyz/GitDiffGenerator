package pg.gipter.ui.main;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ArgName;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.services.vcs.VcsService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class CsvDetailsSectionControllerTestUI {
    @Test
    void authorControlsHandleMissingPathsAndUseTheFirstAvailableGitRepository(FxRobot robot) {
        var properties = mock(ApplicationProperties.class);
        when(properties.itemType()).thenReturn(ItemType.SIMPLE);
        var git = mock(VcsService.class);
        when(git.getUserName()).thenReturn(Optional.of("repo author"));
        var controller = new CsvDetailsSectionController(null, properties, mock(MainController.class));
        controller.setVcsService(git);

        robot.interact(() -> {
            var gitAuthor = new TextField();
            var defaultAuthor = new CheckBox();
            Map<String, Object> controls = Map.of(
                    "gitAuthorTextField", gitAuthor, "useDefaultAuthorCheckBox", defaultAuthor,
                    "authorsTextField", new TextField(), "committerEmailTextField", new TextField(),
                    "mercurialAuthorTextField", new TextField(), "svnAuthorTextField", new TextField(),
                    "itemTypeComboBox", new ComboBox<>(), "toolkitProjectListNamesTextField", new TextField(),
                    "useDefaultEmailCheckBox", new CheckBox());

            controller.initialize(null, null, controls,
                    VersionControlSystem.inspectProjectPaths(List.of("", ArgName.projectPath.defaultValue())));
            assertThat(defaultAuthor.isDisabled()).isTrue();

            var repositories = new LinkedHashMap<String, VersionControlSystem>();
            repositories.put("offline", VersionControlSystem.NA);
            controller.initialize(null, null, controls, repositories);
            assertThat(gitAuthor.isDisabled()).isTrue();
            verifyNoInteractions(git);

            repositories.put("available", VersionControlSystem.GIT);
            controller.initialize(null, null, controls, repositories);
            verify(git).setProjectPath("available");
            assertThat(gitAuthor.isDisabled()).isFalse();
            assertThat(defaultAuthor.isDisabled()).isFalse();
            defaultAuthor.setSelected(true);
            assertThat(gitAuthor.getText()).isEqualTo("repo author");
        });
    }
}
