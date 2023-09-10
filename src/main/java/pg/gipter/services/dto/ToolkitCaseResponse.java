package pg.gipter.services.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ToolkitCaseResponse {
    @SerializedName("CasesData")
    public List<CasesData> cases;
    @SerializedName("PageNo")
    public int pageNo;
    @SerializedName("PageCount")
    public Integer pageCount;

    public ToolkitCaseResponse(List<CasesData> cases, int pageNo, Integer pageCount, boolean sortAscending) {
        this.cases = cases;
        this.pageNo = pageNo;
        this.pageCount = pageCount;
    }
}
