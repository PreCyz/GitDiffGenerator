package pg.gipter.toolkit.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DocumentDetails {

    private final String docType;
    private final String fileLeafRef;
    private final String fileRef;
    private final String guid;
    private final int majorVersion;
    private final int minorVersion;
    private final String fileName;
    private final String serverRelativeUrl;
    private final String project;
    private final LocalDateTime timeLastModified;
    private final LocalDateTime created;
    private final LocalDateTime modified;
    private final String title;
    private final String currentVersion;
    private final User author;
    private final User lastModifier;
    private List<VersionDetails> versions;

    DocumentDetails(String docType, String fileLeafRef, String fileRef, String guid, int majorVersion, int minorVersion,
                    String fileName, String serverRelativeUrl, String project, LocalDateTime timeLastModified, LocalDateTime created,
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
        this.project = project;
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

    public String getProject() {
        return project;
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

    public void setVersions(List<VersionDetails> versions) {
        this.versions = versions;
    }
}
