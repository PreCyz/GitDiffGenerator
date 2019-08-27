package pg.gipter.statistic.dto;

import java.io.Serializable;

public class GipterUser implements Serializable {

    public static final String COLLECTION_NAME = "users";

    private String id;
    private String username;
    private String lastExecutionDate;
    private String firstExecutionDate;
    private String javaVersion;

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
}
