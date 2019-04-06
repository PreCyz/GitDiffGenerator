package pg.gipter.toolkit.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DocumentDetails {

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
    private User lastModifier;

    private List<VersionDetails> versions;

    DocumentDetails(String docType, String fileLeafRef, String fileRef, String guid, int majorVersion, int minorVersion,
                    String fileName, String serverRelativeUrl, LocalDateTime timeLastModified, LocalDateTime created,
                    LocalDateTime modified, String title, String currentVersion, User author, User lastModifier,
                    List<VersionDetails> versions) {
        this.docType = docType;
        this.fileLeafRef = fileLeafRef;
        this.fileRef = fileRef;
        this.guid = guid;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.fileName = fileName;
        this.serverRelativeUrl = serverRelativeUrl;
        this.timeLastModified = timeLastModified;
        this.created = created;
        this.modified = modified;
        this.title = title;
        this.currentVersion = currentVersion;
        this.author = author;
        this.lastModifier = lastModifier;
        this.versions = versions;
    }

    public String getDocType() {
        return docType;
    }

    public String getFileLeafRef() {
        return fileLeafRef;
    }

    public String getFileRef() {
        return fileRef;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public String getServerRelativeUrl() {
        return serverRelativeUrl;
    }

    public LocalDateTime getTimeLastModified() {
        return timeLastModified;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public User getAuthor() {
        return author;
    }

    public User getLastModifier() {
        return lastModifier;
    }

    public List<VersionDetails> getVersions() {
        return versions;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public String getGuid() {
        return guid;
    }
}
