package pg.gipter.services.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ToolkitCasePayload {

    @SerializedName("SortFieldDefinition")
    public SortFieldDefinition sortFieldDefinition;
    @SerializedName("ItemFields")
    public List<ItemField> itemFields;
    @SerializedName("SortAscending")
    public boolean sortAscending;

    public ToolkitCasePayload(SortFieldDefinition sortFieldDefinition, List<ItemField> itemFields, boolean sortAscending) {
        this.sortFieldDefinition = sortFieldDefinition;
        this.itemFields = itemFields;
        this.sortAscending = sortAscending;
    }
}
