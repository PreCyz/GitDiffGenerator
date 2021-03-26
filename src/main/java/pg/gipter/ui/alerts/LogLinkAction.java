package pg.gipter.ui.alerts;

import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.utils.JarHelper;

public class LogLinkAction extends AbstractLinkAction {

    public LogLinkAction() {
        super(JarHelper.logsFolder());
    }

    @Override
    public void run() {
        AppManagerFactory.getInstance().launchFileManagerForLogs();
    }

}
