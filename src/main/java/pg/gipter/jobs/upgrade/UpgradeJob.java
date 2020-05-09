package pg.gipter.jobs.upgrade;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.service.GithubService;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.BundleUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpgradeJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeJob.class);
    private GithubService githubService;
    static final String NAME = UpgradeJob.class.getName();
    static final String GROUP = NAME + "Group";

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
                    .withLink(GithubService.GITHUB_URL + "/releases/latest")
                    .withWindowType(WindowType.BROWSER_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withImage(ImageFile.MINION_AAAA_GIF)
                    .buildAndDisplayWindow()
            );
        }
    }
}
