package pg.gipter.services.upgrade;

import javafx.concurrent.Task;
import pg.gipter.services.SemanticVersioning;
import pg.gipter.utils.SystemUtils;

public final class UpgradeServiceFactory {
    private UpgradeServiceFactory() {}

    public static Task<Void> getUpgradeServiceInstance(SemanticVersioning currentVersion, String githubToken) {
        if (SystemUtils.isExe()) {
            return new UpgradeExe(currentVersion, githubToken);
        }
        return new UpgradeJar(currentVersion, githubToken);
    }
}
