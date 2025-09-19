package pg.gipter.core.model;

import pg.gipter.core.ArgName;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class SharePointConfig {

    public static final String SHARE_POINT_CONFIGS = "sharePointConfigs";
    public static final String PASSWORD_MEMBER_NAME = "password";

    private String name;
    private String url;
    private String project;
    private Set<String> listNames;
    private transient String fullRequestUrl;
    private String formDigest;
    private String fedAuth;
    private String gotoCookie;

    public SharePointConfig() {
        listNames = Stream.of(ArgName.toolkitProjectListNames.defaultValue()).collect(toCollection(LinkedHashSet::new));
    }

    public SharePointConfig(SharePointConfig sharePointConfig) {
        name = sharePointConfig.getName();
        url = sharePointConfig.getUrl();
        project = sharePointConfig.getProject();
        listNames = sharePointConfig.getListNames();
        fullRequestUrl = sharePointConfig.getFullRequestUrl();
        formDigest = sharePointConfig.getFormDigest();
        fedAuth = sharePointConfig.getFedAuth();
        gotoCookie = sharePointConfig.getGoto();
    }

    public SharePointConfig(String url, String fullRequestUrl) {
        this("", url, fullRequestUrl, null, null, null);
    }
    public SharePointConfig(String url, String fullRequestUrl, String fedAuth, String gotoCookie) {
        this("", url, fullRequestUrl, null, fedAuth, gotoCookie);
    }

    public SharePointConfig(String url, String fullRequestUrl, String fedAuth, String gotoCookie, String formDigest) {
        this("", url, fullRequestUrl, formDigest, fedAuth, gotoCookie);
    }

    public SharePointConfig(String name, String url, String fullRequestUrl, String formDigest, String fedAuth, String gotoCookie) {
        this.name = name;
        this.url = url;
        this.fullRequestUrl = fullRequestUrl;
        this.formDigest = formDigest;
        this.fedAuth = fedAuth;
        this.gotoCookie = gotoCookie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFedAuth() {
        return fedAuth;
    }

    public void setFedAuth(String fedAuth) {
        this.fedAuth = fedAuth;
    }

    public String getGoto() {
        return gotoCookie;
    }

    public void setGoto(String gotoCookie) {
        this.gotoCookie = gotoCookie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharePointConfig that = (SharePointConfig) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getProject(), that.getProject()) &&
                Objects.equals(getListNames(), that.getListNames()) &&
                Objects.equals(getFormDigest(), that.getFormDigest()) &&
                Objects.equals(getFedAuth(), that.getFedAuth()) &&
                Objects.equals(getGoto(), that.getGoto());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getName(), getUrl(), getProject(), getListNames(), getFormDigest(), getFedAuth(), getGoto()
        );
    }

    public String toString2() {
        return "SharePointConfig{" +
                "name='" + getName() + '\'' +
                ", url='" + getUrl() + '\'' +
                ", project='" + getProject() + '\'' +
                ", listName='" + getListNames() + '\'' +
                ", fullRequestUrl='" + getFullRequestUrl() + '\'' +
                ", formDigest='" + getFormDigest() + '\'' +
                ", FedAuth='" + getFedAuth() + '\'' +
                ", Goto='" + getGoto() + '\'' +
                '}';
    }
}
