package pg.gipter.core.dto;

import pg.gipter.core.ArgName;
import pg.gipter.utils.CryptoUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

public class ToolkitConfig {

    public static final String TOOLKIT_CONFIG = "toolkitConfig";

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
    }

    public ToolkitConfig(ToolkitConfig toolkitConfig) {
        toolkitUsername = toolkitConfig.getToolkitUsername();
        toolkitPassword = toolkitConfig.getToolkitPassword();
        toolkitProjectListNames = toolkitConfig.getToolkitProjectListNames();
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

    public static ToolkitConfig valueFrom(String[] args) {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length > 1) {
                String argumentName = split[0];
                String argumentValue = split[1];
                if (ArgName.toolkitUsername.name().equals(argumentName)) {
                    toolkitConfig.setToolkitUsername(argumentValue);
                } else if (ArgName.toolkitPassword.name().equals(argumentName)) {
                    toolkitConfig.setToolkitPassword(argumentValue);
                } else if (ArgName.toolkitDomain.name().equals(argumentName)) {
                    toolkitConfig.setToolkitDomain(argumentValue);
                } else if (ArgName.toolkitCopyListName.name().equals(argumentName)) {
                    toolkitConfig.setToolkitCopyListName(argumentValue);
                } else if (ArgName.toolkitUrl.name().equals(argumentName)) {
                    toolkitConfig.setToolkitUrl(argumentValue);
                } else if (ArgName.toolkitCopyCase.name().equals(argumentName)) {
                    toolkitConfig.setToolkitCopyCase(argumentValue);
                } else if (ArgName.toolkitWSUrl.name().equals(argumentName)) {
                    toolkitConfig.setToolkitWSUrl(argumentValue);
                } else if (ArgName.toolkitUserFolder.name().equals(argumentName)) {
                    toolkitConfig.setToolkitUserFolder(argumentValue);
                } else if (ArgName.toolkitProjectListNames.name().equals(argumentName)) {
                    toolkitConfig.setToolkitProjectListNames(argumentValue);
                }
            }
        }
        return toolkitConfig;
    }

    @Override
    public String toString() {
        return "ToolkitConfig{" +
                "toolkitUsername='" + toolkitUsername + '\'' +
                ", toolkitPassword='" + CryptoUtils.encryptSafe(toolkitPassword) + '\'' +
                ", toolkitDomain='" + toolkitDomain + '\'' +
                ", toolkitCopyListName='" + toolkitCopyListName + '\'' +
                ", toolkitUrl='" + toolkitUrl + '\'' +
                ", toolkitCopyCase='" + toolkitCopyCase + '\'' +
                ", toolkitWSUrl='" + toolkitWSUrl + '\'' +
                ", toolkitUserFolder='" + toolkitUserFolder + '\'' +
                ", toolkitProjectListNames='" + toolkitProjectListNames + '\'' +
                '}';
    }
}
