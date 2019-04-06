package pg.gipter.toolkit.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DocumentDetailsBuilder {
    private String docType;
    private String fileLeafRef;
    private String fileRef;
    private String guid;
    private int majorVersion;
    private int minorVersion;
    private String fileName;
    private String serverRelativeUrl;
    private LocalDateTime timeLastModified;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String title;
    private String currentVersion;
    private User author;
    private User modifier;
    private List<VersionDetails> versions;

    public DocumentDetailsBuilder withDocType(String docType) {
        this.docType = docType;
        return this;
    }

    public DocumentDetailsBuilder withFileLeafRef(String fileLeafRef) {
        this.fileLeafRef = fileLeafRef;
        return this;
    }

    public DocumentDetailsBuilder withFileRef(String fileRef) {
        this.fileRef = fileRef;
        return this;
    }

    public DocumentDetailsBuilder withGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public DocumentDetailsBuilder withMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
        return this;
    }

    public DocumentDetailsBuilder withMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
        return this;
    }

    public DocumentDetailsBuilder withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DocumentDetailsBuilder withServerRelativeUrl(String serverRelativeUrl) {
        this.serverRelativeUrl = serverRelativeUrl;
        return this;
    }

    public DocumentDetailsBuilder withTimeLastModified(LocalDateTime timeLastModified) {
        this.timeLastModified = timeLastModified;
        return this;
    }

    public DocumentDetailsBuilder withCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public DocumentDetailsBuilder withModified(LocalDateTime modified) {
        this.modified = modified;
        return this;
    }

    public DocumentDetailsBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public DocumentDetailsBuilder withCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
        return this;
    }

    public DocumentDetailsBuilder withAuthor(User author) {
        this.author = author;
        return this;
    }

    public DocumentDetailsBuilder withModifier(User modifier) {
        this.modifier = modifier;
        return this;
    }

    public DocumentDetailsBuilder withVersions(List<VersionDetails> versions) {
        this.versions = versions;
        return this;
    }

    public DocumentDetails create() {
        return new DocumentDetails(docType, fileLeafRef, fileRef, guid, majorVersion, minorVersion, fileName,
                serverRelativeUrl, timeLastModified, created, modified, title, currentVersion, author, modifier, versions);
    }
}