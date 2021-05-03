package pg.gipter.jobs;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.services.GithubService;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.BrowserLinkAction;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.BundleUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpgradeJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeJob.class);
    private final GithubService githubService;
    public static final String NAME = UpgradeJob.class.getName();
    public static final String GROUP = NAME + "Group";

    UpgradeJob() {
        String[] args = {ArgName.preferredArgSource + "=" + PreferredArgSource.UI};
        githubService = new GithubService(ApplicationPropertiesFactory.getInstance(args).version());
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        logger.info("Executing check upgrade job {}.", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        checkUpgrades();
    }

    private void checkUpgrades() {
        if (githubService.isNewVersion()) {
            logger.info("New version available: {}.", githubService.getServerVersion());
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.upgrade.message", githubService.getServerVersion()))
                    .withLinkAction(new BrowserLinkAction(GithubService.GITHUB_URL + "/releases/latest"))
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWebViewDetails(WebViewService.getInstance().pullSuccessWebView())
                    .buildAndDisplayWindow()
            );
        }
    }
}
