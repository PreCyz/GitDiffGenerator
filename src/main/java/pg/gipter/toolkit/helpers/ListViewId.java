package pg.gipter.toolkit.helpers;

public class ListViewId {
    private final String listId;
    private final String viewId;

    public ListViewId(String listId, String viewId) {
        this.listId = listId;
        this.viewId = viewId;
    }

    public String listId() {
        return listId;
    }

    public String viewId() {
        return viewId;
    }
}
