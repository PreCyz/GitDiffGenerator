package pg.gipter.ui.project.toolkit;

import org.jetbrains.annotations.NotNull;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ToolkitProjectsController extends AbstractController {

    private ApplicationProperties applicationProperties;

    public ToolkitProjectsController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setUpColumns();
        initValues();
        setupButtons();
    }

    private void setUpColumns() {
    }

    private void initValues() {
    }

    @NotNull
    private Optional<String> getSupportedVcs(File project) {
        try {
            return Optional.of(VersionControlSystem.valueFrom(project).name());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private void setupButtons() {
    }
}
