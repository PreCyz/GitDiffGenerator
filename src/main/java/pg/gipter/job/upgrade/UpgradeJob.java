package pg.gipter.job.upgrade;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.service.GithubService;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpgradeJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeJob.class);
    private GithubService githubService;
    static final String NAME = UpgradeJob.class.getName();
    static final String GROUP = NAME + "Group";

    UpgradeJob() {
        String[] args = {ArgName.preferredArgSource + "=" + PreferredArgSource.UI};
        githubService = new GithubService(ApplicationPropertiesFactory.getInstance(args));
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        logger.info("Executing check upgrade job {}.", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        githubService.checkUpgrades();
    }
}
