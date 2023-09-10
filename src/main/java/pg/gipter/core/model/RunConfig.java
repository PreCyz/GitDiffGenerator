package pg.gipter.core.model;

import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class RunConfig {

    public static final String RUN_CONFIGS = "runConfigs";

    private String author;
    private String gitAuthor;
    private String mercurialAuthor;
    private String svnAuthor;
    private String committerEmail;
    private ItemType itemType;
    private Boolean skipRemote;
    private Boolean fetchAll;
    private Integer fetchTimeout;
    private String itemPath;
    private String projectPath;
    private String itemFileNamePrefix;
    private Integer periodInDays;
    private transient LocalDate startDate;
    private transient LocalDate endDate;
    private String configurationName;
    private String toolkitProjectListNames;
    private Boolean deleteDownloadedFiles;
    private PreferredArgSource preferredArgSource;

    public RunConfig() {
        configurationName = ArgName.configurationName.defaultValue();
    }

    public RunConfig(String author, String gitAuthor, String mercurialAuthor, String svnAuthor, String committerEmail,
                     ItemType itemType, Boolean skipRemote, Boolean fetchAll, String itemPath, String projectPath,
                     String itemFileNamePrefix, Integer periodInDays, LocalDate startDate, LocalDate endDate,
                     String configurationName, String toolkitProjectListNames, Boolean deleteDownloadedFiles,
                     PreferredArgSource preferredArgSource, Set<SharePointConfig> sharePointConfigs,
                     Integer fetchTimeout) {
        this.author = author;
        this.gitAuthor = gitAuthor;
        this.mercurialAuthor = mercurialAuthor;
        this.svnAuthor = svnAuthor;
        this.committerEmail = committerEmail;
        this.itemType = itemType;
        this.skipRemote = skipRemote;
        this.fetchAll = fetchAll;
        this.itemPath = itemPath;
        this.projectPath = projectPath;
        this.itemFileNamePrefix = itemFileNamePrefix;
        this.periodInDays = periodInDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.configurationName = configurationName;
        this.toolkitProjectListNames = toolkitProjectListNames;
        this.deleteDownloadedFiles = deleteDownloadedFiles;
        this.preferredArgSource = preferredArgSource;
        this.fetchTimeout = fetchTimeout;
    }

    public RunConfig(RunConfig runConfig) {
        author = runConfig.getAuthor();
        gitAuthor = runConfig.getGitAuthor();
        mercurialAuthor = runConfig.getMercurialAuthor();
        svnAuthor = runConfig.getSvnAuthor();
        committerEmail = runConfig.getCommitterEmail();
        itemType = runConfig.getItemType();
        skipRemote = runConfig.getSkipRemote();
        fetchAll = runConfig.getFetchAll();
        itemPath = runConfig.getItemPath();
        projectPath = runConfig.getProjectPath();
        itemFileNamePrefix = runConfig.getItemFileNamePrefix();
        periodInDays = runConfig.getPeriodInDays();
        startDate = runConfig.getStartDate();
        endDate = runConfig.getEndDate();
        configurationName = runConfig.getConfigurationName();
        toolkitProjectListNames = runConfig.getToolkitProjectListNames();
        deleteDownloadedFiles = runConfig.getDeleteDownloadedFiles();
        preferredArgSource = runConfig.getPreferredArgSource();
        fetchTimeout = runConfig.getFetchTimeout();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGitAuthor() {
        return gitAuthor;
    }

    public void setGitAuthor(String gitAuthor) {
        this.gitAuthor = gitAuthor;
    }

    public String getMercurialAuthor() {
        return mercurialAuthor;
    }

    public void setMercurialAuthor(String mercurialAuthor) {
        this.mercurialAuthor = mercurialAuthor;
    }

    public String getSvnAuthor() {
        return svnAuthor;
    }

    public void setSvnAuthor(String svnAuthor) {
        this.svnAuthor = svnAuthor;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Boolean getSkipRemote() {
        return skipRemote;
    }

    public void setSkipRemote(Boolean skipRemote) {
        this.skipRemote = skipRemote;
    }

    public Boolean getFetchAll() {
        return fetchAll;
    }

    public void setFetchAll(Boolean fetchAll) {
        this.fetchAll = fetchAll;
    }

    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getItemFileNamePrefix() {
        return itemFileNamePrefix;
    }

    public void setItemFileNamePrefix(String itemFileNamePrefix) {
        this.itemFileNamePrefix = itemFileNamePrefix;
    }

    public Integer getPeriodInDays() {
        return periodInDays;
    }

    public void setPeriodInDays(Integer periodInDays) {
        this.periodInDays = periodInDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getToolkitProjectListNames() {
        return toolkitProjectListNames;
    }

    public void setToolkitProjectListNames(String toolkitProjectListNames) {
        this.toolkitProjectListNames = toolkitProjectListNames;
    }

    public Boolean getDeleteDownloadedFiles() {
        return deleteDownloadedFiles;
    }

    public void setDeleteDownloadedFiles(Boolean deleteDownloadedFiles) {
        this.deleteDownloadedFiles = deleteDownloadedFiles;
    }

    public PreferredArgSource getPreferredArgSource() {
        return preferredArgSource;
    }

    public void setPreferredArgSource(PreferredArgSource preferredArgSource) {
        this.preferredArgSource = preferredArgSource;
    }

    public Integer getFetchTimeout() {
        return fetchTimeout;
    }

    public void setFetchTimeout(Integer fetchTimeout) {
        this.fetchTimeout = fetchTimeout;
    }

    public String[] toArgumentArray() {
        Collection<String> arguments = new LinkedHashSet<>();
        if (getAuthor() != null) {
            arguments.add(ArgName.author.name() + "=" + getAuthor());
        }
        if (getGitAuthor() != null) {
            arguments.add(ArgName.gitAuthor.name() + "=" + getGitAuthor());
        }
        if (getMercurialAuthor() != null) {
            arguments.add(ArgName.mercurialAuthor.name() + "=" + getMercurialAuthor());
        }
        if (getSvnAuthor() != null) {
            arguments.add(ArgName.svnAuthor.name() + "=" + getSvnAuthor());
        }
        if (getCommitterEmail() != null) {
            arguments.add(ArgName.committerEmail.name() + "=" + getCommitterEmail());
        }
        if (getItemType() != null) {
            arguments.add(ArgName.itemType.name() + "=" + getItemType());
        }
        if (getSkipRemote() != null) {
            arguments.add(ArgName.skipRemote.name() + "=" + getSkipRemote());
        }
        if (getFetchAll() != null) {
            arguments.add(ArgName.fetchAll.name() + "=" + getFetchAll());
        }
        if (getItemPath() != null) {
            arguments.add(ArgName.itemPath.name() + "=" + getItemPath());
        }
        if (getProjectPath() != null) {
            arguments.add(ArgName.projectPath.name() + "=" + getProjectPath());
        }
        if (getItemFileNamePrefix() != null) {
            arguments.add(ArgName.itemFileNamePrefix.name() + "=" + getItemFileNamePrefix());
        }
        if (getPeriodInDays() != null) {
            arguments.add(ArgName.periodInDays.name() + "=" + getPeriodInDays());
        }
        if (getStartDate() != null) {
            arguments.add(ArgName.startDate.name() + "=" + getStartDate().format(DateTimeFormatter.ISO_DATE));
        }
        if (getEndDate() != null) {
            arguments.add(ArgName.endDate.name() + "=" + getEndDate().format(DateTimeFormatter.ISO_DATE));
        }
        if (getConfigurationName() != null) {
            arguments.add(ArgName.configurationName.name() + "=" + getConfigurationName());
        }
        if (getToolkitProjectListNames() != null) {
            arguments.add(ArgName.toolkitProjectListNames.name() + "=" + getToolkitProjectListNames());
        }
        if (getDeleteDownloadedFiles() != null) {
            arguments.add(ArgName.deleteDownloadedFiles.name() + "=" + getDeleteDownloadedFiles());
        }
        if (getPreferredArgSource() != null) {
            arguments.add(ArgName.preferredArgSource.name() + "=" + getPreferredArgSource());
        }
        if (getFetchTimeout() != null) {
            arguments.add(ArgName.fetchTimeout.name() + "=" + getFetchTimeout());
        }
        return arguments.toArray(new String[0]);
    }

    public static RunConfig valueFrom(String[] args) {
        RunConfig runConfig = new RunConfig();
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length > 1) {
                String argumentName = split[0];
                String argumentValue = split[1];
                if (ArgName.author.name().equals(argumentName)) {
                    runConfig.setAuthor(argumentValue);
                } else if (ArgName.gitAuthor.name().equals(argumentName)) {
                    runConfig.setGitAuthor(argumentValue);
                } else if (ArgName.mercurialAuthor.name().equals(argumentName)) {
                    runConfig.setMercurialAuthor(argumentValue);
                } else if (ArgName.svnAuthor.name().equals(argumentName)) {
                    runConfig.setSvnAuthor(argumentValue);
                } else if (ArgName.committerEmail.name().equals(argumentName)) {
                    runConfig.setCommitterEmail(argumentValue);
                } else if (ArgName.itemType.name().equals(argumentName)) {
                    runConfig.setItemType(ItemType.valueFor(argumentValue));
                } else if (ArgName.skipRemote.name().equals(argumentName)) {
                    runConfig.setSkipRemote(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.fetchAll.name().equals(argumentName)) {
                    runConfig.setFetchAll(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.itemPath.name().equals(argumentName)) {
                    runConfig.setItemPath(argumentValue);
                } else if (ArgName.projectPath.name().equals(argumentName)) {
                    runConfig.setProjectPath(argumentValue);
                } else if (ArgName.itemFileNamePrefix.name().equals(argumentName)) {
                    runConfig.setItemFileNamePrefix(argumentValue);
                } else if (ArgName.periodInDays.name().equals(argumentName)) {
                    runConfig.setPeriodInDays(Integer.parseInt(argumentValue));
                } else if (ArgName.startDate.name().equals(argumentName)) {
                    runConfig.setStartDate(LocalDate.parse(argumentValue, DateTimeFormatter.ISO_DATE));
                } else if (ArgName.endDate.name().equals(argumentName)) {
                    runConfig.setEndDate(LocalDate.parse(argumentValue, DateTimeFormatter.ISO_DATE));
                } else if (ArgName.configurationName.name().equals(argumentName)) {
                    runConfig.setConfigurationName(argumentValue);
                } else if (ArgName.toolkitProjectListNames.name().equals(argumentName)) {
                    runConfig.setToolkitProjectListNames(argumentValue);
                } else if (ArgName.deleteDownloadedFiles.name().equals(argumentName)) {
                    runConfig.setDeleteDownloadedFiles(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.preferredArgSource.name().equals(argumentName)) {
                    runConfig.setPreferredArgSource(PreferredArgSource.valueFor(argumentValue));
                } else if (ArgName.fetchTimeout.name().equals(argumentName)) {
                    runConfig.setFetchTimeout(Integer.parseInt(argumentValue));
                }
            }
        }
        return runConfig;
    }

    @Override
    public String toString() {
        return "RunConfig{" +
                "author='" + author + '\'' +
                ", gitAuthor='" + gitAuthor + '\'' +
                ", mercurialAuthor='" + mercurialAuthor + '\'' +
                ", svnAuthor='" + svnAuthor + '\'' +
                ", committerEmail='" + committerEmail + '\'' +
                ", itemType=" + itemType +
                ", skipRemote=" + skipRemote +
                ", fetchAll=" + fetchAll +
                ", fetchTimeout=" + fetchTimeout +
                ", itemPath='" + itemPath + '\'' +
                ", projectPath='" + projectPath + '\'' +
                ", itemFileNamePrefix='" + itemFileNamePrefix + '\'' +
                ", periodInDays=" + periodInDays +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", configurationName='" + configurationName + '\'' +
                ", toolkitProjectListNames='" + toolkitProjectListNames + '\'' +
                ", deleteDownloadedFiles=" + deleteDownloadedFiles +
                ", preferredArgSource=" + preferredArgSource +
                '}';
    }
}
