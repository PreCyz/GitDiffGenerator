package pg.gipter.core.model;

import pg.gipter.core.ArgName;
import pg.gipter.utils.CryptoUtils;

public class SharePointConfig extends ToolkitConfig {

    public static final String SHARE_POINT_CONFIG = "sharePointConfig";

    private String toolkitDomain;
    private String toolkitUrl;
    private String toolkitWSUrl;

    public SharePointConfig() {
        toolkitUsername = ArgName.toolkitUsername.defaultValue();
        toolkitPassword = ArgName.toolkitPassword.defaultValue();
    }

    public SharePointConfig(SharePointConfig toolkitConfig) {
        toolkitUsername = toolkitConfig.getToolkitUsername();
        toolkitPassword = toolkitConfig.getToolkitPassword();
        toolkitProjectListNames = toolkitConfig.getToolkitProjectListNames();
    }

    public String getToolkitDomain() {
        return toolkitDomain;
    }

    public void setToolkitDomain(String toolkitDomain) {
        this.toolkitDomain = toolkitDomain;
    }

    public String getToolkitUrl() {
        return toolkitUrl;
    }

    public void setToolkitUrl(String toolkitUrl) {
        this.toolkitUrl = toolkitUrl;
    }

    public String getToolkitWSUrl() {
        return toolkitWSUrl;
    }

    public void setToolkitWSUrl(String toolkitWSUrl) {
        this.toolkitWSUrl = toolkitWSUrl;
    }

    @Override
    public String toString() {
        return "ToolkitConfig{" +
                "toolkitUsername='" + getToolkitUsername() + '\'' +
                ", toolkitPassword='" + CryptoUtils.encryptSafe(getToolkitPassword()) + '\'' +
                ", toolkitDomain='" + getToolkitDomain() + '\'' +
                ", toolkitUrl='" + getToolkitUrl() + '\'' +
                ", toolkitWSUrl='" + getToolkitWSUrl() + '\'' +
                ", toolkitProjectListNames='" + getToolkitProjectListNames() + '\'' +
                '}';
    }
}
