package pg.gipter.services.dto;

import com.google.gson.annotations.SerializedName;

public class CasesData {
    public String title;
    public String id;
    public String created; //"15-06-2022 08:38"
    @SerializedName("ows_Title")
    public String owsTitle;
    @SerializedName("CaseID")
    public String caseId;
    @SerializedName("ows_Created")
    public String owsCreated; //"15-06-2022 08:38"
    @SerializedName("VisualID")
    public String visualId;
    @SerializedName("CCMSubID")
    public String ccmSubId;
    @SerializedName("CaseTypeId")
    public String caseTypeId;
    @SerializedName("CaseListID")
    public String caseListId;
    @SerializedName("CaseState")
    public String caseState;
    @SerializedName("CaseUserConnectionID")
    public String caseUserConnectionID;
    @SerializedName("AllowDelete")
    public String allowDelete;
    @SerializedName("AllowDeleteChain")
    public String allowDeleteChain;
    @SerializedName("CaseListItemID")
    public String caseListItemID;
    @SerializedName("CaseListUrl")
    public String caseListUrl;
    @SerializedName("SourceUrl")
    public String sourceUrl;
    @SerializedName("CaseAbsoluteUrl")
    public String caseAbsoluteUrl;
    @SerializedName("CaseAbsoluteUrlForJS")
    public String caseAbsoluteUrlForJS;
    @SerializedName("OrganizerAbsoluteUrlColumn")
    public String organizerAbsoluteUrlColumn;
    @SerializedName("OrganizerPrefixColumn")
    public String organizerPrefixColumn;
    @SerializedName("JournalNoteEnabled")
    public String journalNoteEnabled;
    @SerializedName("JournalNoteCaseUrl")
    public String journalNoteCaseUrl;
    @SerializedName("JournalNoteListUrl")
    public String journalNoteListUrl;
    @SerializedName("JournalNoteTemplateUrl")
    public String journalNoteTemplateUrl;

    public CasesData(String title,
                     String id,
                     String created,
                     String owsTitle,
                     String caseId,
                     String owsCreated,
                     String visualId,
                     String ccmSubId,
                     String caseTypeId,
                     String caseListId,
                     String caseState,
                     String caseUserConnectionID,
                     String allowDelete,
                     String allowDeleteChain,
                     String caseListItemID,
                     String caseListUrl,
                     String sourceUrl,
                     String caseAbsoluteUrl,
                     String caseAbsoluteUrlForJS,
                     String organizerAbsoluteUrlColumn,
                     String organizerPrefixColumn,
                     String journalNoteEnabled,
                     String journalNoteCaseUrl,
                     String journalNoteListUrl,
                     String journalNoteTemplateUrl) {
        this.title = title;
        this.id = id;
        this.created = created;
        this.owsTitle = owsTitle;
        this.caseId = caseId;
        this.owsCreated = owsCreated;
        this.visualId = visualId;
        this.ccmSubId = ccmSubId;
        this.caseTypeId = caseTypeId;
        this.caseListId = caseListId;
        this.caseState = caseState;
        this.caseUserConnectionID = caseUserConnectionID;
        this.allowDelete = allowDelete;
        this.allowDeleteChain = allowDeleteChain;
        this.caseListItemID = caseListItemID;
        this.caseListUrl = caseListUrl;
        this.sourceUrl = sourceUrl;
        this.caseAbsoluteUrl = caseAbsoluteUrl;
        this.caseAbsoluteUrlForJS = caseAbsoluteUrlForJS;
        this.organizerAbsoluteUrlColumn = organizerAbsoluteUrlColumn;
        this.organizerPrefixColumn = organizerPrefixColumn;
        this.journalNoteEnabled = journalNoteEnabled;
        this.journalNoteCaseUrl = journalNoteCaseUrl;
        this.journalNoteListUrl = journalNoteListUrl;
        this.journalNoteTemplateUrl = journalNoteTemplateUrl;
    }
}
