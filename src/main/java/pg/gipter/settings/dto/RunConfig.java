package pg.gipter.settings.dto;

import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashSet;

public class RunConfig {

    private String author;
    private String gitAuthor;
    private String mercurialAuthor;
    private String svnAuthor;
    private String committerEmail;
    private UploadType uploadType;
    private Boolean skipRemote;
    private Boolean fetchAll;
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
        /*author = ArgName.author.defaultValue();
        gitAuthor = ArgName.gitAuthor.defaultValue();
        mercurialAuthor = ArgName.mercurialAuthor.defaultValue();
        svnAuthor = ArgName.svnAuthor.defaultValue();
        committerEmail = ArgName.committerEmail.defaultValue();
        uploadType = UploadType.valueFor(ArgName.uploadType.defaultValue());
        skipRemote = StringUtils.getBoolean(ArgName.skipRemote.defaultValue());
        fetchAll = StringUtils.getBoolean(ArgName.fetchAll.defaultValue());
        itemPath = ArgName.itemPath.defaultValue();
        projectPath = ArgName.projectPath.defaultValue();
        itemFileNamePrefix = ArgName.itemFileNamePrefix.defaultValue();
        periodInDays = Integer.parseInt(ArgName.periodInDays.defaultValue());
        startDate = LocalDate.now().minusDays(periodInDays);
        endDate = LocalDate.now();
        toolkitProjectListNames = ArgName.toolkitProjectListNames.defaultValue();
        deleteDownloadedFiles = StringUtils.getBoolean(ArgName.deleteDownloadedFiles.defaultValue());
        preferredArgSource = PreferredArgSource.valueFor(ArgName.preferredArgSource.defaultValue());*/
    }

    public RunConfig(String author, String gitAuthor, String mercurialAuthor, String svnAuthor, String committerEmail,
              UploadType uploadType, Boolean skipRemote, Boolean fetchAll, String itemPath, String projectPath,
              String itemFileNamePrefix, Integer periodInDays, LocalDate startDate, LocalDate endDate,
              String configurationName, String toolkitProjectListNames, Boolean deleteDownloadedFiles,
              PreferredArgSource preferredArgSource) {
        this.author = author;
        this.gitAuthor = gitAuthor;
        this.mercurialAuthor = mercurialAuthor;
        this.svnAuthor = svnAuthor;
        this.committerEmail = committerEmail;
        this.uploadType = uploadType;
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

    public UploadType getUploadType() {
        return uploadType;
    }

    public void setUploadType(UploadType uploadType) {
        this.uploadType = uploadType;
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
        if (getUploadType() != null) {
            arguments.add(ArgName.uploadType.name() + "=" + getUploadType());
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
        return arguments.toArray(new String[0]);
    }
}
