package pg.gipter.core.model;

import pg.gipter.core.ArgName;
import pg.gipter.utils.CryptoUtils;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class SharePointConfig {

    public static final String SHARE_POINT_CONFIGS = "sharePointConfigs";
    public static final String PASSWORD_MEMBER_NAME = "password";
    public static final String URL_SUFFIX = "/Forms/AllItems.aspx";

    private String name;
    private String username;
    private String password;
    private String domain;
    private String url;
    private String project;
    private Set<String> listNames;
    private transient String fullRequestUrl;
    private String formDigest;

    public SharePointConfig() {
        listNames = Stream.of(ArgName.toolkitProjectListNames.defaultValue()).collect(toCollection(LinkedHashSet::new));
    }

    public SharePointConfig(SharePointConfig sharePointConfig) {
        name = sharePointConfig.getName();
        username = sharePointConfig.getUsername();
        password = sharePointConfig.getPassword();
        domain = sharePointConfig.getDomain();
        url = sharePointConfig.getUrl();
        project = sharePointConfig.getProject();
        listNames = sharePointConfig.getListNames();
        fullRequestUrl = sharePointConfig.getFullRequestUrl();
        formDigest = sharePointConfig.getFormDigest();
    }

    public SharePointConfig(String username, String password, String domain, String url, String fullRequestUrl) {
        this("", username, password, domain, url, fullRequestUrl, null);
    }

    public SharePointConfig(String username, String password, String domain, String url, String fullRequestUrl, String formDigest) {
        this("", username, password, domain, url, fullRequestUrl, formDigest);
    }

    public SharePointConfig(String name, String username, String password, String domain, String url, String fullRequestUrl, String formDigest) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.url = url;
        this.fullRequestUrl = fullRequestUrl;
        this.formDigest = formDigest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<String> getListNames() {
        return listNames;
    }

    public void setListNames(Set<String> listNames) {
        this.listNames = listNames;
    }

    public String getFullRequestUrl() {
        return fullRequestUrl;
    }

    public void setFullRequestUrl(String fullRequestUrl) {
        this.fullRequestUrl = fullRequestUrl;
    }

    public String getFormDigest() {
        return formDigest;
    }

    public void setFormDigest(String formDigest) {
        this.formDigest = formDigest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharePointConfig that = (SharePointConfig) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getUsername(), that.getUsername()) &&
                Objects.equals(getPassword(), that.getPassword()) &&
                Objects.equals(getDomain(), that.getDomain()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getProject(), that.getProject()) &&
                Objects.equals(getListNames(), that.getListNames()) &&
                Objects.equals(getFormDigest(), that.getFormDigest());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getName(), getUsername(), getPassword(), getDomain(), getUrl(), getProject(), getListNames(), getFormDigest()
        );
    }

    public String toString2() {
        return "SharePointConfig{" +
                "name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", password='" + CryptoUtils.encryptSafe(getPassword()) + '\'' +
                ", domain='" + getDomain() + '\'' +
                ", url='" + getUrl() + '\'' +
                ", project='" + getProject() + '\'' +
                ", listName='" + getListNames() + '\'' +
                ", fullRequestUrl='" + getFullRequestUrl() + '\'' +
                ", getFormDigest='" + getFormDigest() + '\'' +
                '}';
    }
}
