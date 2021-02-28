package pg.gipter.core;

import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/** Created by Pawel Gawedzki on 17-Sep-2018. */
class CliApplicationProperties extends ApplicationProperties {

    CliApplicationProperties(String[] args) {
        super(args);
    }

    @Override
    public Set<String> authors() {
        Set<String> authors = argExtractor.authors();
        if (!containsArg(ArgName.author.name()) && StringUtils.notEmpty(currentRunConfig.getAuthor())) {
            authors = Stream.of(currentRunConfig.getAuthor().split(",")).collect(toCollection(LinkedHashSet::new));
        }
        if (authors.contains(ArgName.author.defaultValue()) && isOtherAuthorsExists()) {
            authors = new LinkedHashSet<>();
        }
        return authors;
    }

    @Override
    public String gitAuthor() {
        String gitAuthor = argExtractor.gitAuthor();
        if (!containsArg(ArgName.gitAuthor.name()) && StringUtils.notEmpty(currentRunConfig.getGitAuthor())) {
            gitAuthor = currentRunConfig.getGitAuthor();
        }
        return gitAuthor;
    }

    @Override
    public String mercurialAuthor() {
        String mercurialAuthor = argExtractor.mercurialAuthor();
        if (!containsArg(ArgName.mercurialAuthor.name()) && StringUtils.notEmpty(currentRunConfig.getMercurialAuthor())) {
            mercurialAuthor = currentRunConfig.getMercurialAuthor();
        }
        return mercurialAuthor;
    }

    @Override
    public String svnAuthor() {
        String svnAuthor = argExtractor.svnAuthor();
        if (!containsArg(ArgName.svnAuthor.name()) && StringUtils.notEmpty(currentRunConfig.getSvnAuthor())) {
            svnAuthor = currentRunConfig.getSvnAuthor();
        }
        return svnAuthor;
    }

    @Override
    public String itemPath() {
        String itemPath = ArgName.itemPath.defaultValue();
        String argName = ArgName.itemPath.name();
        if (containsArg(argName)) {
            itemPath = argExtractor.itemPath();
        }
        if (!containsArg(argName) && StringUtils.notEmpty(currentRunConfig.getItemPath())) {
            itemPath = currentRunConfig.getItemPath();
        }
        return itemType() == ItemType.STATEMENT ? itemPath : Paths.get(itemPath, fileName()).toString();
    }

    @Override
    public String itemFileNamePrefix() {
        String itemFileNamePrefix = argExtractor.itemFileNamePrefix();
        if (!containsArg(ArgName.itemFileNamePrefix.name()) && StringUtils.notEmpty(currentRunConfig.getItemFileNamePrefix())) {
            itemFileNamePrefix = currentRunConfig.getItemFileNamePrefix();
        }
        return itemFileNamePrefix;
    }

    @Override
    public Set<String> projectPaths() {
        Set<String> projectPaths = argExtractor.projectPaths();
        if (!containsArg(ArgName.projectPath.name()) && StringUtils.notEmpty(currentRunConfig.getProjectPath())) {
            projectPaths = Stream.of(currentRunConfig.getProjectPath().split(","))
                    .collect(toCollection(LinkedHashSet::new));
        }
        return projectPaths;
    }

    @Override
    public int periodInDays() {
        int periodInDays = argExtractor.periodInDays();
        if (!containsArg(ArgName.periodInDays.name()) && currentRunConfig.getPeriodInDays() != null) {
            periodInDays = Math.abs(currentRunConfig.getPeriodInDays());
        }
        return periodInDays;
    }

    @Override
    public String committerEmail() {
        String committerEmail = argExtractor.committerEmail();
        if (!containsArg(ArgName.committerEmail.name()) && StringUtils.notEmpty(currentRunConfig.getCommitterEmail())) {
            committerEmail = currentRunConfig.getCommitterEmail();
        }
        return committerEmail;
    }

    @Override
    public LocalDate startDate() {
        LocalDate startDate = argExtractor.startDate();
        if (!containsArg(ArgName.startDate.name()) && currentRunConfig.getStartDate() != null) {
            startDate = currentRunConfig.getStartDate();
        } else if (!containsArg(ArgName.periodInDays.name()) && currentRunConfig.getPeriodInDays() != null) {
            startDate = LocalDate.now().minusDays(periodInDays());
        }
        return startDate;
    }

    @Override
    public LocalDate endDate() {
        LocalDate endDate = argExtractor.endDate();
        if (!containsArg(ArgName.endDate.name()) && currentRunConfig.getEndDate() != null) {
            endDate = currentRunConfig.getEndDate();
        }
        return endDate;
    }

    @Override
    public ItemType itemType() {
        ItemType uploadType = argExtractor.itemType();
        if (!containsArg(ArgName.itemType.name()) && currentRunConfig.getItemType() != null) {
            uploadType = currentRunConfig.getItemType();
        }
        return uploadType;
    }

    @Override
    public boolean isSkipRemote() {
        boolean skipRemote = argExtractor.isSkipRemote();
        if (!containsArg(ArgName.skipRemote.name()) && currentRunConfig.getSkipRemote() != null) {
            skipRemote = currentRunConfig.getSkipRemote();
        }
        return skipRemote;
    }

    @Override
    public boolean isFetchAll() {
        boolean fetchAll = argExtractor.isFetchAll();
        if (!containsArg(ArgName.fetchAll.name()) && currentRunConfig.getFetchAll() != null) {
            fetchAll = currentRunConfig.getFetchAll();
        }
        return fetchAll;
    }

    @Override
    public Set<String> toolkitProjectListNames() {
        Set<String> toolkitProjectListNames = argExtractor.toolkitProjectListNames();
        if (!containsArg(ArgName.toolkitProjectListNames.name()) && StringUtils.notEmpty(currentRunConfig.getToolkitProjectListNames())) {
            toolkitProjectListNames = Stream.of(currentRunConfig.getToolkitProjectListNames().split(","))
                    .collect(toCollection(LinkedHashSet::new));
        }
        return toolkitProjectListNames;
    }

    @Override
    public boolean isDeleteDownloadedFiles() {
        boolean delete = argExtractor.isDeleteDownloadedFiles();
        if (!containsArg(ArgName.deleteDownloadedFiles.name()) && currentRunConfig.getDeleteDownloadedFiles() != null) {
            delete = currentRunConfig.getDeleteDownloadedFiles();
        }
        return delete;
    }

    @Override
    public String configurationName() {
        String configurationName = argExtractor.configurationName();
        if (!containsArg(ArgName.configurationName.name()) && StringUtils.notEmpty(currentRunConfig.getConfigurationName())) {
            configurationName = currentRunConfig.getConfigurationName();
        }
        return configurationName;
    }

    @Override
    public String toolkitUsername() {
        String toolkitUsername = argExtractor.toolkitUsername();
        if (!containsArg(ArgName.toolkitUsername.name()) && StringUtils.notEmpty(toolkitConfig.getToolkitUsername())) {
            toolkitUsername = toolkitConfig.getToolkitUsername().trim().toUpperCase();
        }
        return toolkitUsername;
    }

    @Override
    public String toolkitPassword() {
        String toolkitPassword = argExtractor.toolkitPassword();
        if (!containsArg(ArgName.toolkitPassword.name()) && StringUtils.notEmpty(toolkitConfig.getToolkitPassword())) {
            toolkitPassword = toolkitConfig.getToolkitPassword();
        }
        return toolkitPassword;
    }

    @Override
    public String toolkitDomain() {
        String toolkitDomain = argExtractor.toolkitDomain();
        if (!containsArg(ArgName.toolkitDomain.name()) && StringUtils.notEmpty(toolkitConfig.getToolkitDomain())) {
            toolkitDomain = toolkitConfig.getToolkitDomain();
        }
        return toolkitDomain;
    }

    @Override
    public String toolkitUrl() {
        String toolkitUrl = argExtractor.toolkitUrl();
        if (!containsArg(ArgName.toolkitUrl.name()) && StringUtils.notEmpty(toolkitConfig.getToolkitUrl())) {
            toolkitUrl = toolkitConfig.getToolkitUrl();
        }
        return toolkitUrl;
    }

    @Override
    public String toolkitCopyListName() {
        String toolkitListName = argExtractor.toolkitListName();
        if (!containsArg(ArgName.toolkitCopyListName.name()) && StringUtils.notEmpty(toolkitConfig.getToolkitCopyListName())) {
            toolkitListName = toolkitConfig.getToolkitCopyListName();
        }
        return toolkitListName;
    }

    @Override
    public String toolkitUserFolder() {
        return ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
    }

    @Override
    public boolean isConfirmationWindow() {
        boolean confirmation = argExtractor.isConfirmationWindow();
        if (!containsArg(ArgName.confirmationWindow.name()) && applicationConfig.getConfirmationWindow() != null) {
            confirmation = applicationConfig.getConfirmationWindow();
        }
        return confirmation;
    }

    @Override
    public boolean isUseUI() {
        boolean useUI = argExtractor.isUseUI();
        if (!containsArg(ArgName.useUI.name()) && applicationConfig.getUseUI() != null) {
            useUI = applicationConfig.getUseUI();
        }
        return useUI;
    }

    @Override
    public boolean isActiveTray() {
        return false;
    }

    @Override
    public boolean isEnableOnStartup() {
        return false;
    }

    @Override
    public boolean isUpgradeFinished() {
        boolean upgradeFinished = argExtractor.isUpgradeFinished();
        if (!containsArg(ArgName.upgradeFinished.name()) && applicationConfig.getUpgradeFinished() != null) {
            upgradeFinished = applicationConfig.getUpgradeFinished();
        }
        return upgradeFinished;
    }

    @Override
    public String loggerLevel() {
        String loggerLevel = argExtractor.loggerLevel();
        if (!containsArg(ArgName.loggerLevel.name()) && applicationConfig.getLoggingLevel() != null) {
            loggerLevel = applicationConfig.getLoggingLevel().toString();
        }
        return loggerLevel;
    }

    @Override
    public String uiLanguage() {
        String uiLanguage = argExtractor.uiLanguage();
        if (!containsArg(ArgName.uiLanguage.name()) && StringUtils.notEmpty(applicationConfig.getUiLanguage())
                && BundleUtils.isLanguageSupported(applicationConfig.getUiLanguage())) {
            uiLanguage = applicationConfig.getUiLanguage();
        }
        return uiLanguage;
    }

    @Override
    public boolean isCertImportEnabled() {
        boolean certImportEnabled = argExtractor.isCertImportEnabled();
        if (!containsArg(ArgName.certImport.name()) && applicationConfig.getCertImportEnabled() != null) {
            certImportEnabled = applicationConfig.getCertImportEnabled();
        }
        return certImportEnabled;
    }

    @Override
    public boolean isCheckLastItemEnabled() {
        boolean checkLastItemEnabled = argExtractor.isCheckLastItemEnabled();
        if (!containsArg(ArgName.checkLastItem.name()) && applicationConfig.getCheckLastItemEnabled() != null) {
            checkLastItemEnabled = applicationConfig.getCheckLastItemEnabled();
        }
        return checkLastItemEnabled;
    }

    @Override
    public String getCheckLastItemJobCronExpression() {
        String cronExpression = argExtractor.checkLastItemJobCronExpression();
        if (!containsArg(ArgName.checkLastItemJobCronExpression.name())
                && applicationConfig.getCheckLastItemJobCronExpression() != null) {
            cronExpression = applicationConfig.getCheckLastItemJobCronExpression();
        }
        return cronExpression;
    }

    @Override
    public boolean useMetroSkin() {
        return argExtractor.isUseMetroSkin();
    }
}
