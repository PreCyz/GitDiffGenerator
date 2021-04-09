package pg.gipter.ui.alerts;

import pg.gipter.services.platforms.AppManagerFactory;

public class BrowserLinkAction extends AbstractLinkAction {

    public BrowserLinkAction(String link) {
        super(link);
    }

    @Override
    public void run() {
        AppManagerFactory.getInstance().launchDefaultBrowser(link);
    }

}
