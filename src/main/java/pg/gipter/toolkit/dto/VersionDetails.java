package pg.gipter.toolkit.dto;

import java.time.LocalDateTime;

public class VersionDetails {

    private User creator;
    private String checkInComment;
    private LocalDateTime created;
    private int id;
    private boolean isCurrentVersion;
    private long size;
    private String downloadUrl;
    private Double versionLabel;

    public VersionDetails(User creator, String checkInComment, LocalDateTime created, int id, boolean isCurrentVersion, long size, String downloadUrl, double versionLabel) {
        this.creator = creator;
        this.checkInComment = checkInComment;
        this.created = created;
        this.id = id;
        this.isCurrentVersion = isCurrentVersion;
        this.size = size;
        this.downloadUrl = downloadUrl;
        this.versionLabel = versionLabel;
    }

    public User getCreator() {
        return creator;
    }

    public String getCheckInComment() {
        return checkInComment;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public int getId() {
        return id;
    }

    public boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    public long getSize() {
        return size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public Double getVersionLabel() {
        return versionLabel;
    }
}
