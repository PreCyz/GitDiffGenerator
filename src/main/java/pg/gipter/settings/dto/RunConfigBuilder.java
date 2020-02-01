package pg.gipter.settings.dto;

import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.PreferredArgSource;

import java.time.LocalDate;

public class RunConfigBuilder {

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
    private LocalDate startDate;
    private LocalDate endDate;
    private String configurationName;
    private String toolkitProjectListNames;
    private Boolean deleteDownloadedFiles;
    private PreferredArgSource preferredArgSource;


    public RunConfigBuilder withAuthor(String author) {
        this.author = author;
        return this;
    }

    public RunConfigBuilder withGitAuthor(String gitAuthor) {
        this.gitAuthor = gitAuthor;
        return this;
    }

    public RunConfigBuilder withMercurialAuthor(String mercurialAuthor) {
        this.mercurialAuthor = mercurialAuthor;
        return this;
    }

    public RunConfigBuilder withSvnAuthor(String svnAuthor) {
        this.svnAuthor = svnAuthor;
        return this;
    }

    public RunConfigBuilder withCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
        return this;
    }

    public RunConfigBuilder withUploadType(UploadType uploadType) {
        this.uploadType = uploadType;
        return this;
    }

    public RunConfigBuilder withSkipRemote(Boolean skipRemote) {
        this.skipRemote = skipRemote;
        return this;
    }

    public RunConfigBuilder withFetchAll(Boolean fetchAll) {
        this.fetchAll = fetchAll;
        return this;
    }

    public RunConfigBuilder withItemPath(String itemPath) {
        this.itemPath = itemPath;
        return this;
    }

    public RunConfigBuilder withProjectPath(String projectPath) {
        this.projectPath = projectPath;
        return this;
    }

    public RunConfigBuilder withItemFileNamePrefix(String itemFileNamePrefix) {
        this.itemFileNamePrefix = itemFileNamePrefix;
        return this;
    }

    public RunConfigBuilder withPeriodInDays(Integer periodInDays) {
        this.periodInDays = periodInDays;
        return this;
    }

    public RunConfigBuilder withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public RunConfigBuilder withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public RunConfigBuilder withConfigurationName(String configurationName) {
        this.configurationName = configurationName;
        return this;
    }

    public RunConfigBuilder withToolkitProjectListNames(String toolkitProjectListNames) {
        this.toolkitProjectListNames = toolkitProjectListNames;
        return this;
    }

    public RunConfigBuilder withDeleteDownloadedFiles(Boolean deleteDownloadedFiles) {
        this.deleteDownloadedFiles = deleteDownloadedFiles;
        return this;
    }

    public RunConfigBuilder withPreferredArgSource(PreferredArgSource preferredArgSource) {
        this.preferredArgSource = preferredArgSource;
        return this;
    }

    public RunConfig create() {
        return new RunConfig(author, gitAuthor, mercurialAuthor, svnAuthor, committerEmail, uploadType, skipRemote, fetchAll,
                itemPath, projectPath, itemFileNamePrefix, periodInDays, startDate, endDate, configurationName, toolkitProjectListNames,
                deleteDownloadedFiles, preferredArgSource);
    }
}
