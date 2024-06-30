package pg.gipter.core.model;

import pg.gipter.core.ArgName;

import java.util.Collection;
import java.util.LinkedHashSet;

public class ToolkitConfig {

    public static final String TOOLKIT_CONFIG = "toolkitConfig";

    protected String toolkitUsername;
    protected String toolkitFolderName;
    private transient String toolkitDomain;
    private transient String toolkitCopyListName;
    private String toolkitHostUrl;
    private transient String toolkitCopyCase;
    private transient String toolkitWSUrl;
    private transient String toolkitUserFolderUrl;

    public ToolkitConfig() {
        toolkitUsername = ArgName.toolkitUsername.defaultValue();
        toolkitFolderName = ArgName.toolkitFolderName.defaultValue();
    }

    public ToolkitConfig(ToolkitConfig toolkitConfig) {
        toolkitUsername = toolkitConfig.getToolkitUsername();
        toolkitFolderName = toolkitConfig.getToolkitFolderName();
    }

    public String getToolkitUsername() {
        return toolkitUsername;
    }

    public void setToolkitUsername(String toolkitUsername) {
        this.toolkitUsername = toolkitUsername;
    }

    public String getToolkitFolderName() {
        return toolkitFolderName;
    }

    public void setToolkitFolderName(String toolkitFolderName) {
        this.toolkitFolderName = toolkitFolderName;
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

    public String getToolkitHostUrl() {
        return toolkitHostUrl;
    }

    public void setToolkitHostUrl(String toolkitHostUrl) {
        this.toolkitHostUrl = toolkitHostUrl;
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

    public String getToolkitUserFolderUrl() {
        return toolkitUserFolderUrl;
    }

    public void setToolkitUserFolderUrl(String toolkitUserFolder) {
        this.toolkitUserFolderUrl = toolkitUserFolder;
    }

    public String[] toArgumentArray() {
        Collection<String> arguments = new LinkedHashSet<>();
        if (getToolkitUsername() != null) {
            arguments.add(ArgName.toolkitUsername.name() + "=" + getToolkitUsername());
        }
        if (getToolkitFolderName() != null) {
            arguments.add(ArgName.toolkitFolderName.name() + "=" + getToolkitFolderName());
        }
        if (getToolkitDomain() != null) {
            arguments.add(ArgName.toolkitDomain.name() + "=" + getToolkitDomain());
        }
        if (getToolkitCopyListName() != null) {
            arguments.add(ArgName.toolkitCopyListName.name() + "=" + getToolkitCopyListName());
        }
        if (getToolkitHostUrl() != null) {
            arguments.add(ArgName.toolkitHostUrl.name() + "=" + getToolkitHostUrl());
        }
        if (getToolkitCopyCase() != null) {
            arguments.add(ArgName.toolkitCopyCase.name() + "=" + getToolkitCopyCase());
        }
        if (getToolkitWSUrl() != null) {
            arguments.add(ArgName.toolkitWSUrl.name() + "=" + getToolkitWSUrl());
        }
        if (getToolkitUserFolderUrl() != null) {
            arguments.add(ArgName.toolkitUserFolderUrl.name() + "=" + getToolkitUserFolderUrl());
        }
        return arguments.toArray(new String[0]);
    }

    public static ToolkitConfig valueFrom(String[] args) {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length > 1) {
                String argumentName = split[0];
                String argumentValue = split[1];
                if (ArgName.toolkitUsername.name().equals(argumentName)) {
                    toolkitConfig.setToolkitUsername(argumentValue);
                } else if (ArgName.toolkitFolderName.name().equals(argumentName)) {
                    toolkitConfig.setToolkitFolderName(argumentValue);
                } else if (ArgName.toolkitDomain.name().equals(argumentName)) {
                    toolkitConfig.setToolkitDomain(argumentValue);
                } else if (ArgName.toolkitCopyListName.name().equals(argumentName)) {
                    toolkitConfig.setToolkitCopyListName(argumentValue);
                } else if (ArgName.toolkitHostUrl.name().equals(argumentName)) {
                    toolkitConfig.setToolkitHostUrl(argumentValue);
                } else if (ArgName.toolkitCopyCase.name().equals(argumentName)) {
                    toolkitConfig.setToolkitCopyCase(argumentValue);
                } else if (ArgName.toolkitWSUrl.name().equals(argumentName)) {
                    toolkitConfig.setToolkitWSUrl(argumentValue);
                } else if (ArgName.toolkitUserFolderUrl.name().equals(argumentName)) {
                    toolkitConfig.setToolkitUserFolderUrl(argumentValue);
                }
            }
        }
        return toolkitConfig;
    }

    @Override
    public String toString() {
        return "ToolkitConfig{" +
                "toolkitUsername='" + toolkitUsername + '\'' +
                ", toolkitFolderName='" + toolkitFolderName + '\'' +
                ", toolkitDomain='" + toolkitDomain + '\'' +
                ", toolkitCopyListName='" + toolkitCopyListName + '\'' +
                ", toolkitHostUrl='" + toolkitHostUrl + '\'' +
                ", toolkitCopyCase='" + toolkitCopyCase + '\'' +
                ", toolkitWSUrl='" + toolkitWSUrl + '\'' +
                ", toolkitUserFolder='" + toolkitUserFolderUrl + '\'' +
                '}';
    }
}
