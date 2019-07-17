package pg.gipter.ui.configuration;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.PropertiesHelper;

public class ConfigurationController extends AbstractController {

    private ApplicationProperties applicationProperties;
    private UILauncher uiLauncher;
    private PropertiesHelper propertiesHelper;

    public ConfigurationController(ApplicationProperties applicationProperties, UILauncher uiLauncher) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
        propertiesHelper = new PropertiesHelper();
    }
}
