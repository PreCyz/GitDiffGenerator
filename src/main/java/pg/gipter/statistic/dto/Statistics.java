package pg.gipter.statistic.dto;

import pg.gipter.ui.RunType;
import pg.gipter.ui.UploadStatus;

import java.io.Serializable;

public class Statistics implements Serializable {

    public static final String COLLECTION_NAME = "statistics";

    private String id;
    private String username;
    private String lastExecutionDate;
    private String firstExecutionDate;
    private String lastSuccessDate;
    private String lastFailedDate;
    private String javaVersion;
    private UploadStatus lastUpdateStatus;
    private RunType lastRunType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastExecutionDate() {
        return lastExecutionDate;
    }

    public void setLastExecutionDate(String lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }

    public String getFirstExecutionDate() {
        return firstExecutionDate;
    }

    public void setFirstExecutionDate(String firstExecutionDate) {
        this.firstExecutionDate = firstExecutionDate;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public void setLastUpdateStatus(UploadStatus lastUpdateStatus) {
        this.lastUpdateStatus = lastUpdateStatus;
    }

    public UploadStatus getLastUpdateStatus() {
        return lastUpdateStatus;
    }

    public RunType getLastRunType() {
        return lastRunType;
    }

    public void setLastRunType(RunType lastRunType) {
        this.lastRunType = lastRunType;
    }

    public String getLastSuccessDate() {
        return lastSuccessDate;
    }

    public void setLastSuccessDate(String lastSuccessDate) {
        this.lastSuccessDate = lastSuccessDate;
    }

    public String getLastFailedDate() {
        return lastFailedDate;
    }

    public void setLastFailedDate(String lastFailedDate) {
        this.lastFailedDate = lastFailedDate;
    }
}
