package pg.gipter.ui.alerts;

public abstract class AbstractLinkAction implements Runnable {
    protected final String link;
    protected final String text;

    protected AbstractLinkAction(String link) {
        this.link = link;
        this.text = "";
    }

    public AbstractLinkAction(String link, String text) {
        this.link = link;
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public String getText() {
        return text;
    }
}
