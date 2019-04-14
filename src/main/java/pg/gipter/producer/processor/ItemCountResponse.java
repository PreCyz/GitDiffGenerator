package pg.gipter.producer.processor;

import com.google.gson.JsonObject;

class ItemCountResponse {

    private final String project;
    private final String listName;
    private final JsonObject itemCount;

    ItemCountResponse(String project, String listName, JsonObject itemCount) {
        this.project = project;
        this.listName = listName;
        this.itemCount = itemCount;
    }

    public String getProject() {
        return project;
    }

    public String getListName() {
        return listName;
    }

    public int getItemCount() {
        if (itemCount == null || itemCount.get("d") == null || itemCount.get("d").getAsJsonObject().get("ItemCount") == null) {
            return 0;
        }
        return itemCount.get("d").getAsJsonObject()
                .get("ItemCount").getAsInt();
    }
}
