package pg.gipter.statistics;

import org.bson.types.ObjectId;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.ui.RunType;
import pg.gipter.ui.UploadStatus;

import java.io.Serializable;
import java.util.*;

public class Statistic implements Serializable {

    public static final String COLLECTION_NAME = "statistics";

    private ObjectId id;
    private String username;
    private String lastExecutionDate;
    private String firstExecutionDate;
    private String lastSuccessDate;
    private String lastFailedDate;
    private String javaVersion;
    private UploadStatus lastUpdateStatus;
    private RunType lastRunType;
    private Map<VersionControlSystem, Set<String>> controlSystemMap;
    private Set<String> systemUsers;
    private String applicationVersion;
    private List<ExceptionDetails> exceptions;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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

    public Map<VersionControlSystem, Set<String>> getControlSystemMap() {
        return controlSystemMap;
    }

    public void setControlSystemMap(Map<VersionControlSystem, Set<String>> controlSystemMap) {
        this.controlSystemMap = controlSystemMap;
    }

    public void setSystemUsers(Set<String> systemUsers) {
        this.systemUsers = systemUsers;
    }

    public Set<String> getSystemUsers() {
        return systemUsers;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public List<ExceptionDetails> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<ExceptionDetails> exceptions) {
        this.exceptions = exceptions;
    }
}
