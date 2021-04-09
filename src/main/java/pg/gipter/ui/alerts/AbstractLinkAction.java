package pg.gipter.ui.alerts;

public abstract class AbstractLinkAction implements Runnable {
    protected final String link;

    protected AbstractLinkAction(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }
}
