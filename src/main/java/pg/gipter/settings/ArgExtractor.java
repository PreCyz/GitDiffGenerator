package pg.gipter.settings;

import pg.gipter.producer.command.UploadType;
import pg.gipter.utils.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static pg.gipter.settings.FileApplicationProperties.yyyy_MM_dd;

/** Created by Pawel Gawedzki on 17-Sep-2018.*/
final class ArgExtractor {

    private final String[] args;

    ArgExtractor(String[] args) {
        this.args = args;
    }

    String[] getArgs() {
        return args;
    }

    boolean containsArg(String argumentName) {
        if (args == null || args.length == 0) {
            return false;
        }
        return Arrays.stream(args).anyMatch(arg -> arg.startsWith(argumentName));
    }

    Set<String> authors() {
        if (containsArg(ArgName.author.name())) {
            String authors = getValue(ArgName.author, authorDefaultValue());
            return Stream.of(authors.split(","))
                    .filter(value -> !StringUtils.nullOrEmpty(value))
                    .collect(toCollection(LinkedHashSet::new));
        }
        return Stream.of(authorDefaultValue())
                .filter(value -> !StringUtils.nullOrEmpty(value))
                .collect(toCollection(LinkedHashSet::new));
    }

    String authorDefaultValue() {
        boolean useDefaultValue = StringUtils.nullOrEmpty(committerEmail());
        useDefaultValue &= StringUtils.nullOrEmpty(gitAuthor());
        useDefaultValue &= StringUtils.nullOrEmpty(svnAuthor());
        useDefaultValue &= StringUtils.nullOrEmpty(mercurialAuthor());
        return useDefaultValue ? ArgName.author.defaultValue() : null;
    }

    String gitAuthor() {
        if (containsArg(ArgName.gitAuthor.name())) {
            return getValue(ArgName.gitAuthor, ArgName.gitAuthor.defaultValue());
        }
        return ArgName.gitAuthor.defaultValue();
    }

    String mercurialAuthor() {
        if (containsArg(ArgName.mercurialAuthor.name())) {
            return getValue(ArgName.mercurialAuthor, ArgName.mercurialAuthor.defaultValue());
        }
        return ArgName.mercurialAuthor.defaultValue();
    }

    String svnAuthor() {
        if (containsArg(ArgName.svnAuthor.name())) {
            return getValue(ArgName.svnAuthor, ArgName.svnAuthor.defaultValue());
        }
        return ArgName.svnAuthor.defaultValue();
    }

    private String getValue(final ArgName argName, String defaultValue) {
        Optional<String> argument = Arrays.stream(args).filter(arg -> arg.startsWith(argName.name())).findAny();
        if (argument.isPresent()) {
            String value = argument.get();
            return value.substring(value.indexOf("=") + 1);
        }
        return defaultValue;
    }

    String itemPath() {
        if (containsArg(ArgName.itemPath.name())) {
            return getValue(ArgName.itemPath, ArgName.itemPath.defaultValue());
        }
        return ArgName.itemPath.defaultValue();
    }

    Set<String> projectPaths() {
        String[] projectPaths = new String[]{ArgName.projectPath.defaultValue()};
        if (containsArg(ArgName.projectPath.name())) {
            projectPaths = getValue(ArgName.projectPath, ArgName.projectPath.defaultValue()).split(",");
        }
        return new LinkedHashSet<>(Arrays.asList(projectPaths));
    }

    int periodInDays() {
        if (containsArg(ArgName.periodInDays.name())) {
            return Math.abs(Integer.parseInt(getValue(ArgName.periodInDays, ArgName.periodInDays.defaultValue())));
        }
        return Integer.parseInt(ArgName.periodInDays.defaultValue());
    }

    String committerEmail() {
        if (containsArg(ArgName.committerEmail.name())) {
            return getValue(ArgName.committerEmail, ArgName.committerEmail.defaultValue());
        }
        return ArgName.committerEmail.defaultValue();
    }

    LocalDate startDate() {
        String[] date = ArgName.startDate.defaultValue().split("-");
        if (containsArg(ArgName.startDate.name()) || containsArg(ArgName.periodInDays.name())) {
            date = getValue(ArgName.startDate, LocalDate.now().minusDays(periodInDays()).format(yyyy_MM_dd)).split("-");
        }
        return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
    }

    LocalDate endDate() {
        String[] date = ArgName.endDate.defaultValue().split("-");
        if (containsArg(ArgName.endDate.name())) {
            date = getValue(ArgName.endDate, ArgName.endDate.defaultValue()).split("-");
        }
        return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
    }

    String itemFileNamePrefix() {
        if (containsArg(ArgName.itemFileNamePrefix.name())) {
            return getValue(ArgName.itemFileNamePrefix, ArgName.itemFileNamePrefix.defaultValue());
        }
        return ArgName.itemFileNamePrefix.defaultValue();
    }

    UploadType uploadType() {
        if (containsArg(ArgName.uploadType.name())) {
            String codeProtection = getValue(ArgName.uploadType, ArgName.uploadType.defaultValue());
            return UploadType.valueFor(codeProtection);
        }
        return UploadType.valueFor(ArgName.uploadType.defaultValue());
    }

    boolean isConfirmationWindow() {
        if (containsArg(ArgName.confirmationWindow.name())) {
            return StringUtils.getBoolean(getValue(ArgName.confirmationWindow, ArgName.confirmationWindow.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.confirmationWindow.defaultValue());
    }

    String toolkitUsername() {
        if (containsArg(ArgName.toolkitUsername.name())) {
            return getValue(ArgName.toolkitUsername, ArgName.toolkitUsername.defaultValue()).trim().toUpperCase();
        }
        return ArgName.toolkitUsername.defaultValue();
    }

    String toolkitPassword() {
        if (containsArg(ArgName.toolkitPassword.name())) {
            return getValue(ArgName.toolkitPassword, ArgName.toolkitPassword.defaultValue());
        }
        return ArgName.toolkitPassword.defaultValue();
    }

    String toolkitDomain() {
        return ArgName.toolkitDomain.defaultValue();
    }

    String toolkitUrl() {
        return ArgName.toolkitUrl.defaultValue();
    }

    String toolkitCopyCase() {
        return ArgName.toolkitCopyCase.defaultValue();
    }

    String toolkitListName() {
        return ArgName.toolkitCopyListName.defaultValue();
    }

    String toolkitWSUrl() {
        return ArgName.toolkitWSUrl.defaultValue();
    }

    String toolkitUserFolder() {
        return ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
    }

    PreferredArgSource preferredArgSource() {
        if (containsArg(ArgName.preferredArgSource.name())) {
            String preferredArgSrc = getValue(ArgName.preferredArgSource, ArgName.preferredArgSource.defaultValue());
            return PreferredArgSource.valueFor(preferredArgSrc);
        }
        return PreferredArgSource.valueFor(ArgName.preferredArgSource.defaultValue());
    }

    boolean isSkipRemote() {
        if (containsArg(ArgName.skipRemote.name())) {
            return StringUtils.getBoolean(getValue(ArgName.skipRemote, ArgName.skipRemote.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.skipRemote.defaultValue());
    }

    boolean isUseUI() {
        if (containsArg(ArgName.useUI.name())) {
            return StringUtils.getBoolean(getValue(ArgName.useUI, ArgName.useUI.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.useUI.defaultValue());
    }

    boolean isActiveTray() {
        if (containsArg(ArgName.activeTray.name())) {
            return StringUtils.getBoolean(getValue(ArgName.activeTray, ArgName.activeTray.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.activeTray.defaultValue());
    }

    boolean isSilentMode() {
        if (containsArg(ArgName.silentMode.name())) {
            return StringUtils.getBoolean(getValue(ArgName.silentMode, ArgName.silentMode.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.silentMode.defaultValue());
    }

    Set<String> toolkitProjectListNames() {
        String[] projectPaths = new String[]{ArgName.toolkitProjectListNames.defaultValue()};
        if (containsArg(ArgName.toolkitProjectListNames.name())) {
            projectPaths = getValue(ArgName.toolkitProjectListNames, ArgName.toolkitProjectListNames.defaultValue()).split(",");
        }
        return new LinkedHashSet<>(Arrays.asList(projectPaths));
    }

    boolean isDeleteDownloadedFiles() {
        if (containsArg(ArgName.deleteDownloadedFiles.name())) {
            return StringUtils.getBoolean(getValue(ArgName.deleteDownloadedFiles, ArgName.deleteDownloadedFiles.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.deleteDownloadedFiles.defaultValue());
    }

    boolean isEnableOnStartup() {
        if (containsArg(ArgName.enableOnStartup.name())) {
            return StringUtils.getBoolean(getValue(ArgName.enableOnStartup, ArgName.enableOnStartup.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.enableOnStartup.defaultValue());
    }

    public String configurationName() {
        if (containsArg(ArgName.configurationName.name())) {
            return getValue(ArgName.configurationName, ArgName.configurationName.defaultValue());
        }
        return ArgName.configurationName.defaultValue();
    }

    public boolean isUpgradeFinished() {
        if (containsArg(ArgName.upgradeFinished.name())) {
            return StringUtils.getBoolean(getValue(ArgName.upgradeFinished, ArgName.upgradeFinished.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.upgradeFinished.defaultValue());
    }

    public String loggerLevel() {
        if (containsArg(ArgName.loggerLevel.name())) {
            return getValue(ArgName.loggerLevel, ArgName.loggerLevel.defaultValue());
        }
        return ArgName.loggerLevel.defaultValue();
    }

    boolean isFetchAll() {
        if (containsArg(ArgName.fetchAll.name())) {
            return StringUtils.getBoolean(getValue(ArgName.fetchAll, ArgName.fetchAll.defaultValue()));
        }
        return StringUtils.getBoolean(ArgName.fetchAll.defaultValue());
    }
}
