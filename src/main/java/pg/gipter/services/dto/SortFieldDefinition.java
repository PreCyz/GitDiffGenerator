package pg.gipter.services.dto;

import com.google.gson.annotations.SerializedName;

public class SortFieldDefinition {

    @SerializedName("InternalName")
    public String internalName;
    @SerializedName("DataType")
    public String dataType;

    public SortFieldDefinition(String internalName, String dataType) {
        this.internalName = internalName;
        this.dataType = dataType;
    }
}
