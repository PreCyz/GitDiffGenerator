package pg.gipter.services.restart;

import pg.gipter.utils.SystemUtils;

public class RestartServiceFactory {
    private RestartServiceFactory() {}

    public static RestartService getRestartService() {
        if (SystemUtils.isExe()) {
            return new RestartExe();
        }
        return new RestartJar();
    }
}
