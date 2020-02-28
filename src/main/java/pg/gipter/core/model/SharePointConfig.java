package pg.gipter.core.model;

import pg.gipter.core.ArgName;
import pg.gipter.utils.CryptoUtils;

import java.util.Objects;

public class SharePointConfig {

    public static final String SHARE_POINT_CONFIG = "sharePointConfig";
    public static final String URL_SUFFIX = "/Forms/AllItems.aspx";

    private String username;
    private String password;
    private String domain;
    private String url;
    private String project;
    protected String listName;

    public SharePointConfig() {
        username = ArgName.toolkitUsername.defaultValue();
        password = ArgName.toolkitPassword.defaultValue();
    }

    public SharePointConfig(SharePointConfig sharePointConfig) {
        username = sharePointConfig.getUsername();
        password = sharePointConfig.getPassword();
        domain = sharePointConfig.getDomain();
        url = sharePointConfig.getUrl();
        project = sharePointConfig.getProject();
        listName = sharePointConfig.getListName();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharePointConfig that = (SharePointConfig) o;
        return Objects.equals(getUsername(), that.getUsername()) &&
                Objects.equals(getPassword(), that.getPassword()) &&
                Objects.equals(getDomain(), that.getDomain()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getProject(), that.getProject()) &&
                Objects.equals(getListName(), that.getListName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPassword(), getDomain(), getUrl(), getProject(), getListName());
    }

    @Override
    public String toString() {
        return "SharePointConfig{" +
                "username='" + getUsername() + '\'' +
                ", password='" + CryptoUtils.encryptSafe(getPassword()) + '\'' +
                ", domain='" + getDomain() + '\'' +
                ", url='" + getUrl() + '\'' +
                ", project='" + getProject() + '\'' +
                ", listName='" + getListName() + '\'' +
                '}';
    }
}
