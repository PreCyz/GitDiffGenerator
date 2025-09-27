package pg.gipter.services.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.FlowType;
import pg.gipter.core.ArgName;
import pg.gipter.services.*;
import pg.gipter.services.restart.RestartService;
import pg.gipter.services.restart.RestartServiceFactory;
import pg.gipter.utils.BundleUtils;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

abstract class AbstractUpgradeService extends TaskService<Void> implements UpgradeService {

    protected final GithubService githubService;
    protected final RestartService restartService;
    protected final Logger logger;

    protected AbstractUpgradeService(SemanticVersioning currentVersion, String githubToken) {
        githubService = new GithubService(currentVersion, githubToken);
        restartService = RestartServiceFactory.getRestartService();
        logger = LoggerFactory.getLogger(getClass());
    }

    protected final void finalizeUpgrade() {
        updateMsg(BundleUtils.getMsg("upgrade.progress.restarting"));
        final List<String> restartArguments = Stream.of(
                String.format("%s=%b", ArgName.upgradeFinished.name(), Boolean.TRUE),
                String.format("%s=%s", ArgName.flowType.name(), FlowType.REGULAR)
        ).collect(toList());
        restartService.start(restartArguments);
        workCompleted();
        System.exit(0);
    }

    @Override
    protected final Void call() {
        upgradeAndRestartApplication();
        return null;
    }
}
