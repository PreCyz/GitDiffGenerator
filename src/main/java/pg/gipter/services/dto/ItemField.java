package pg.gipter.services.dto;

import com.google.gson.annotations.SerializedName;

public class ItemField {
    @SerializedName("DisplayName")
    public String displayName;
    @SerializedName("FieldType")
    public String fieldType;
    @SerializedName("InternalName")
    public String internalName;

    public ItemField(String displayName, String fieldType, String internalName) {
        this.displayName = displayName;
        this.fieldType = fieldType;
        this.internalName = internalName;
    }
}
