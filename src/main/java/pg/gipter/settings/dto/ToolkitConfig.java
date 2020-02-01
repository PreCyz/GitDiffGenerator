package pg.gipter.settings.dto;

import pg.gipter.settings.ArgName;

import java.util.Collection;
import java.util.LinkedHashSet;

public class ToolkitConfig {

    private String toolkitUsername;
    private String toolkitPassword;
    private transient String toolkitDomain;
    private transient String toolkitCopyListName;
    private transient String toolkitUrl;
    private transient String toolkitCopyCase;
    private transient String toolkitWSUrl;
    private transient String toolkitUserFolder;
    private String toolkitProjectListNames;

    public ToolkitConfig() {
        toolkitUsername = ArgName.toolkitUsername.defaultValue();
        toolkitPassword = ArgName.toolkitPassword.defaultValue();
        /*toolkitDomain = ArgName.toolkitDomain.defaultValue();
        toolkitCopyListName = ArgName.toolkitCopyListName.defaultValue();
        toolkitUrl = ArgName.toolkitUrl.defaultValue();
        toolkitCopyCase = ArgName.toolkitCopyCase.defaultValue();
        toolkitWSUrl = ArgName.toolkitWSUrl.defaultValue();
        toolkitUserFolder = ArgName.toolkitUserFolder.defaultValue();
        toolkitProjectListNames = ArgName.toolkitProjectListNames.defaultValue();*/
    }

    public String getToolkitUsername() {
        return toolkitUsername;
    }

    public void setToolkitUsername(String toolkitUsername) {
        this.toolkitUsername = toolkitUsername;
    }

    public String getToolkitPassword() {
        return toolkitPassword;
    }

    public void setToolkitPassword(String toolkitPassword) {
        this.toolkitPassword = toolkitPassword;
    }

    public String getToolkitDomain() {
        return toolkitDomain;
    }

    public void setToolkitDomain(String toolkitDomain) {
        this.toolkitDomain = toolkitDomain;
    }

    public String getToolkitCopyListName() {
        return toolkitCopyListName;
    }

    public void setToolkitCopyListName(String toolkitCopyListName) {
        this.toolkitCopyListName = toolkitCopyListName;
    }

    public String getToolkitUrl() {
        return toolkitUrl;
    }

    public void setToolkitUrl(String toolkitUrl) {
        this.toolkitUrl = toolkitUrl;
    }

    public String getToolkitCopyCase() {
        return toolkitCopyCase;
    }

    public void setToolkitCopyCase(String toolkitCopyCase) {
        this.toolkitCopyCase = toolkitCopyCase;
    }

    public String getToolkitWSUrl() {
        return toolkitWSUrl;
    }

    public void setToolkitWSUrl(String toolkitWSUrl) {
        this.toolkitWSUrl = toolkitWSUrl;
    }

    public String getToolkitUserFolder() {
        return toolkitUserFolder;
    }

    public void setToolkitUserFolder(String toolkitUserFolder) {
        this.toolkitUserFolder = toolkitUserFolder;
    }

    public String getToolkitProjectListNames() {
        return toolkitProjectListNames;
    }

    public void setToolkitProjectListNames(String toolkitProjectListNames) {
        this.toolkitProjectListNames = toolkitProjectListNames;
    }

    public String[] toArgumentArray() {
        Collection<String> arguments = new LinkedHashSet<>();
        if (getToolkitUsername() != null) {
            arguments.add(ArgName.toolkitUsername.name() + "=" + getToolkitUsername());
        }
        if (getToolkitPassword() != null) {
            arguments.add(ArgName.toolkitPassword.name() + "=" + getToolkitPassword());
        }
        if (getToolkitDomain() != null) {
            arguments.add(ArgName.toolkitDomain.name() + "=" + getToolkitDomain());
        }
        if (getToolkitCopyListName() != null) {
            arguments.add(ArgName.toolkitCopyListName.name() + "=" + getToolkitCopyListName());
        }
        if (getToolkitUrl() != null) {
            arguments.add(ArgName.toolkitUrl.name() + "=" + getToolkitUrl());
        }
        if (getToolkitCopyCase() != null) {
            arguments.add(ArgName.toolkitCopyCase.name() + "=" + getToolkitCopyCase());
        }
        if (getToolkitWSUrl() != null) {
            arguments.add(ArgName.toolkitWSUrl.name() + "=" + getToolkitWSUrl());
        }
        if (getToolkitUserFolder() != null) {
            arguments.add(ArgName.toolkitUserFolder.name() + "=" + getToolkitUserFolder());
        }
        if (getToolkitProjectListNames() != null) {
            arguments.add(ArgName.toolkitProjectListNames.name() + "=" + getToolkitProjectListNames());
        }
        return arguments.toArray(new String[0]);
    }
}
