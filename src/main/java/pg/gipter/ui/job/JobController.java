package pg.gipter.ui.job;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.AbstractController;
import pg.gipter.ui.UILauncher;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;

public class JobController extends AbstractController {

    private final ApplicationProperties applicationProperties;

    public JobController(UILauncher uiLauncher, ApplicationProperties applicationProperties) {
        super(uiLauncher);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        TimerJob job = new TimerJob("Gipter job", applicationProperties);

        Timer t = new Timer();
        t.scheduleAtFixedRate(job, 0,5*1000);
    }

}
