package pg.gipter.core;

import pg.gipter.core.config.service.GeneralSettingsService;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
class FileApplicationProperties extends ApplicationProperties {

    FileApplicationProperties(String[] args) {
        super(args);
    }

    @Override
    public Set<String> authors() {
        Set<String> result;
        if (currentRunConfig.getAuthor() != null && !currentRunConfig.getAuthor().isEmpty()) {
            result = Stream.of(currentRunConfig.getAuthor().split(","))
                    .filter(value -> !StringUtils.nullOrEmpty(value))
                    .collect(toCollection(LinkedHashSet::new));
        } else {
            result = argExtractor.authors();
        }
        if (result.contains(ArgName.author.defaultValue()) && isOtherAuthorsExists()) {
            result = new LinkedHashSet<>();
        }
        return result;
    }

    @Override
    public String gitAuthor() {
        if (StringUtils.notEmpty(currentRunConfig.getGitAuthor())) {
            return currentRunConfig.getGitAuthor();
        }
        return argExtractor.gitAuthor();
    }

    @Override
    public String mercurialAuthor() {
        if (StringUtils.notEmpty(currentRunConfig.getMercurialAuthor())) {
            return currentRunConfig.getMercurialAuthor();
        }
        return argExtractor.mercurialAuthor();
    }

    @Override
    public String svnAuthor() {
        if (StringUtils.notEmpty(currentRunConfig.getSvnAuthor())) {
            return currentRunConfig.getSvnAuthor();
        }
        return argExtractor.svnAuthor();
    }

    @Override
    public String itemPath() {
        String itemPath = argExtractor.itemPath();
        if (StringUtils.notEmpty(currentRunConfig.getItemPath())) {
            itemPath = currentRunConfig.getItemPath();
            if (itemType() == ItemType.STATEMENT) {
                return itemPath;
            }
        }
        return itemType() == ItemType.STATEMENT ? itemPath : Paths.get(itemPath, fileName()).toString();
    }

    @Override
    public String itemFileNamePrefix() {
        if (StringUtils.notEmpty(currentRunConfig.getItemFileNamePrefix())) {
            return currentRunConfig.getItemFileNamePrefix();
        }
        return argExtractor.itemFileNamePrefix();
    }

    @Override
    public Set<String> projectPaths() {
        if (currentRunConfig.getProjectPath() != null && !currentRunConfig.getProjectPath().isEmpty()) {
            return Stream.of(currentRunConfig.getProjectPath().split(","))
                    .filter(value -> !StringUtils.nullOrEmpty(value))
                    .collect(toCollection(LinkedHashSet::new));
        }
        return argExtractor.projectPaths();
    }

    @Override
    public int periodInDays() {
        if (currentRunConfig.getPeriodInDays() != null) {
            return Math.abs(currentRunConfig.getPeriodInDays());
        }
        return argExtractor.periodInDays();
    }

    @Override
    public String committerEmail() {
        if (StringUtils.notEmpty(currentRunConfig.getCommitterEmail())) {
            return currentRunConfig.getCommitterEmail();
        }
        return argExtractor.committerEmail();
    }

    @Override
    public LocalDate startDate() {
        if (currentRunConfig.getStartDate() == null && currentRunConfig.getPeriodInDays() != null
                && currentRunConfig.getPeriodInDays() != Integer.parseInt(ArgName.periodInDays.defaultValue())) {
            currentRunConfig.setStartDate(LocalDate.now().minusDays(currentRunConfig.getPeriodInDays()));
        }
        if (currentRunConfig.getStartDate() != null) {
            return currentRunConfig.getStartDate();
        }
        return argExtractor.startDate();
    }

    @Override
    public LocalDate endDate() {
        if (currentRunConfig.getEndDate() != null) {
            return currentRunConfig.getEndDate();
        }
        return argExtractor.endDate();
    }

    @Override
    public ItemType itemType() {
        if (currentRunConfig.getItemType() != null) {
            return currentRunConfig.getItemType();
        }
        return argExtractor.itemType();
    }

    @Override
    public boolean isConfirmationWindow() {
        if (applicationConfig.getConfirmationWindow() != null) {
            return applicationConfig.getConfirmationWindow();
        }
        return argExtractor.isConfirmationWindow();
    }

    @Override
    public boolean isSkipRemote() {
        if (currentRunConfig.getSkipRemote() != null) {
            return currentRunConfig.getSkipRemote();
        }
        return argExtractor.isSkipRemote();
    }

    @Override
    public boolean isFetchAll() {
        if (currentRunConfig.getFetchAll() != null) {
            return currentRunConfig.getFetchAll();
        }
        return argExtractor.isFetchAll();
    }

    @Override
    public boolean isDeleteDownloadedFiles() {
        if (currentRunConfig.getDeleteDownloadedFiles() != null) {
            return currentRunConfig.getDeleteDownloadedFiles();
        }
        return argExtractor.isDeleteDownloadedFiles();
    }

    @Override
    public String configurationName() {
        if (StringUtils.notEmpty(currentRunConfig.getConfigurationName())) {
            return currentRunConfig.getConfigurationName();
        }
        return argExtractor.configurationName();
    }

    @Override
    public String toolkitUsername() {
        if (StringUtils.notEmpty(toolkitConfig.getToolkitUsername()) &&
                !toolkitConfig.getToolkitUsername().equals(ArgName.toolkitUsername.defaultValue())) {
            return toolkitConfig.getToolkitUsername().trim().toUpperCase();
        }
        return argExtractor.toolkitUsername();
    }

    @Override
    public String toolkitDomain() {
        if (StringUtils.notEmpty(toolkitConfig.getToolkitDomain())) {
            return toolkitConfig.getToolkitDomain();
        }
        return argExtractor.toolkitDomain();
    }

    @Override
    public String toolkitHostUrl() {
        if (StringUtils.notEmpty(toolkitConfig.getToolkitHostUrl())) {
            return toolkitConfig.getToolkitHostUrl();
        }
        return argExtractor.toolkitHostUrl();
    }

    @Override
    public String toolkitCopyListName() {
        if (StringUtils.notEmpty(toolkitConfig.getToolkitCopyListName())) {
            return toolkitConfig.getToolkitCopyListName();
        }
        return argExtractor.toolkitListName();
    }

    @Override
    public String toolkitUserFolder() {
        if (StringUtils.notEmpty(toolkitUsername())) {
            return ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
        }
        return argExtractor.toolkitUserFolder();
    }

    @Override
    public String toolkitWSUserFolder() {
        if (StringUtils.notEmpty(toolkitUsername())) {
            return ArgName.toolkitWSUserFolder.defaultValue() + toolkitUsername();
        }
        return argExtractor.toolkitWSUserFolder();
    }

    @Override
    public Set<String> toolkitProjectListNames() {
        if (StringUtils.notEmpty(toolkitConfig.getToolkitProjectListNames())) {
            return Stream.of(toolkitConfig.getToolkitProjectListNames().split(",")).collect(Collectors.toSet());
        }
        return argExtractor.toolkitProjectListNames();
    }

    @Override
    public boolean isUseUI() {
        if (applicationConfig.getUseUI() != null) {
            return applicationConfig.getUseUI();
        }
        return argExtractor.isUseUI();
    }

    @Override
    public String githubToken() {
        Optional<String> gt = GeneralSettingsService.getInstance().getGithubToken();
        if (gt.isPresent()) {
            return gt.get();
        }
        if (StringUtils.notEmpty(applicationConfig.getGithubToken())) {
            return applicationConfig.getGithubToken();
        }
        return argExtractor.githubToken();
    }

    @Override
    public boolean isActiveTray() {
        return false;
    }

    @Override
    public boolean isEnableOnStartup() {
        if (applicationConfig.getEnableOnStartup() != null) {
            return applicationConfig.getEnableOnStartup();
        }
        return argExtractor.isEnableOnStartup();
    }

    @Override
    public boolean isUpgradeFinished() {
        if (applicationConfig.getUpgradeFinished() != null) {
            return applicationConfig.getUpgradeFinished();
        }
        return argExtractor.isUpgradeFinished();
    }

    @Override
    public String loggerLevel() {
        if (applicationConfig.getLoggingLevel() != null) {
            return applicationConfig.getLoggingLevel().toString();
        }
        return argExtractor.loggerLevel();
    }

    @Override
    public String uiLanguage() {
        if (StringUtils.notEmpty(applicationConfig.getUiLanguage())
                && BundleUtils.isLanguageSupported(applicationConfig.getUiLanguage())) {
            return applicationConfig.getUiLanguage();
        }
        return argExtractor.uiLanguage();
    }

    @Override
    public boolean isCheckLastItemEnabled() {
        if (applicationConfig.getCheckLastItemEnabled() != null) {
            return applicationConfig.getCheckLastItemEnabled();
        }
        return argExtractor.isCheckLastItemEnabled();
    }

    @Override
    public String getCheckLastItemJobCronExpression() {
        if (StringUtils.notEmpty(applicationConfig.getCheckLastItemJobCronExpression())) {
            return applicationConfig.getCheckLastItemJobCronExpression();
        }
        return argExtractor.checkLastItemJobCronExpression();
    }

    @Override
    public int fetchTimeout() {
        if (currentRunConfig.getFetchTimeout() != null) {
            return Math.abs(currentRunConfig.getFetchTimeout());
        }
        return argExtractor.fetchTimeout();
    }

    @Override
    public boolean isUploadItem() {
        if (applicationConfig.getUploadItem() != null) {
            return applicationConfig.getUploadItem();
        }
        return argExtractor.isUploadItem();
    }

    @Override
    public boolean isSmartZip() {
        if (applicationConfig.getSmartZip() != null) {
            return applicationConfig.getSmartZip();
        }
        return argExtractor.isSmartZip();
    }
}
