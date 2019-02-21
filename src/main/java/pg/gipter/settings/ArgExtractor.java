package pg.gipter.settings;

import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pg.gipter.settings.FilePreferredApplicationProperties.yyyy_MM_dd;

/** Created by Pawel Gawedzki on 17-Sep-2018.*/
final class ArgExtractor {

    enum ArgName {
        author("NO_AUTHORS_GIVEN"),
        gitAuthor(""),
        mercurialAuthor(""),
        svnAuthor(""),
        committerEmail(""),
        codeProtection(CodeProtection.NONE.name()),
        skipRemote("Y"),

        itemPath("NO_ITEM_PATH_GIVEN"),
        projectPath("NO_PROJECT_PATH_GIVEN"),
        itemFileNamePrefix(""),

        periodInDays("7"),
        startDate(LocalDate.now().minusDays(Integer.parseInt(periodInDays.defaultValue)).format(yyyy_MM_dd)),
        endDate(LocalDate.now().format(yyyy_MM_dd)),

        confirmationWindow("N"),
        preferredArgSource(PreferredArgSource.CLI.name()),

        toolkitUsername("NO_TOOLKIT_USERNAME_GIVEN"),
        toolkitPassword("NO_TOOLKIT_PASSWORD_GIVEN"),
        toolkitDomain("NCDMZ"),
        toolkitListName("WorkItems"),
        toolkitUrl("https://goto.netcompany.com/cases/GTE106/NCSCOPY"),
        toolkitWSUrl(toolkitUrl.defaultValue + "/_vti_bin/lists.asmx"),
        toolkitUserFolder(toolkitUrl.defaultValue + "/Lists/" + toolkitListName.defaultValue + "/");

        private String defaultValue;

        ArgName(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        String defaultValue() {
            return defaultValue;
        }
    }

    private final String[] args;

    ArgExtractor(String[] args) {
        this.args = args;
    }

    boolean containsArg(String argumentName) {
        if (args == null || args.length == 0) {
            return false;
        }
        return Arrays.stream(args).anyMatch(arg -> arg.startsWith(argumentName));
    }

    Set<String> authors() {
        if (containsArg(ArgName.author.name())) {
            String authors = getValue(ArgName.author, ArgName.author.defaultValue());
            return Stream.of(authors.split(",")).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Stream.of(ArgName.author.defaultValue()).collect(Collectors.toCollection(HashSet::new));
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
        return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
    }

    LocalDate endDate() {
        String[] date = ArgName.endDate.defaultValue().split("-");
        if (containsArg(ArgName.endDate.name())) {
            date = getValue(ArgName.endDate, ArgName.endDate.defaultValue()).split("-");
        }
        return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
    }

    String itemFileNamePrefix() {
        if (containsArg(ArgName.itemFileNamePrefix.name())) {
            return getValue(ArgName.itemFileNamePrefix, ArgName.itemFileNamePrefix.defaultValue());
        }
        return ArgName.itemFileNamePrefix.defaultValue();
    }

    CodeProtection codeProtection() {
        if (containsArg(ArgName.codeProtection.name())) {
            String codeProtection = getValue(ArgName.codeProtection, ArgName.codeProtection.defaultValue());
            return CodeProtection.valueFor(codeProtection);
        }
        return CodeProtection.valueFor(ArgName.codeProtection.defaultValue());
    }

    boolean isConfirmationWindow() {
        if (containsArg(ArgName.confirmationWindow.name())) {
            return StringUtils.getBoolean(getValue(ArgName.confirmationWindow, ArgName.codeProtection.defaultValue()));
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

    String toolkitListName() {
        return ArgName.toolkitListName.defaultValue();
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

}
