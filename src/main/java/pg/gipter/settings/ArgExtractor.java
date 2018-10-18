package pg.gipter.settings;

import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.command.VersionControlSystem;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static pg.gipter.Main.yyyy_MM_dd;

/** Created by Pawel Gawedzki on 17-Sep-2018.*/
final class ArgExtractor {

    enum ArgName {
        author("NO_AUTHOR_GIVEN"),
        itemPath("NO_ITEM_PATH_GIVEN"),
        projectPath("NO_PROJECT_PATH_GIVEN"),
        periodInDays("7"),
        committerEmail(""),
        startDate(LocalDate.now().minusDays(Integer.parseInt(periodInDays.defaultValue)).format(yyyy_MM_dd)),
        endDate(LocalDate.now().format(yyyy_MM_dd)),
        itemFileNamePrefix(""),
        versionControlSystem(VersionControlSystem.GIT.name()),
        codeProtection(CodeProtection.NONE.name()),
        toolkitUsername("NO_TOOLKIT_USERNAME_GIVEN"),
        toolkitPassword("NO_TOOLKIT_PASSWORD_GIVEN"),

        //for now hardcoded
        toolkitDomain("NCDMZ"),
        toolkitListName("WorkItems"),
        toolkitUrl("https://goto.netcompany.com/cases/GTE106/NCSCOPY"),
        toolkitWSUrl(toolkitUrl.defaultValue + "/_vti_bin/lists.asmx"),
        toolkitUserFolder(toolkitUrl.defaultValue + "/Lists/" + toolkitListName.defaultValue + "/"),
        emailDomain("@netcompany.com");

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

    private boolean hasArgs() {
        return args != null && args.length > 0;
    }

    String author() {
        if (hasArgs()) {
            return getValue(ArgName.author, ArgName.author.defaultValue());
        }
        return ArgName.author.defaultValue();
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
        if (hasArgs()) {
            return getValue(ArgName.itemPath, ArgName.itemPath.defaultValue());
        }
        return ArgName.itemPath.defaultValue();
    }

    Set<String> projectPaths() {
        String[] projectPaths = new String[]{ArgName.projectPath.defaultValue()};
        if (hasArgs()) {
            projectPaths = getValue(ArgName.projectPath, ArgName.projectPath.defaultValue()).split(",");
        }
        return new LinkedHashSet<>(Arrays.asList(projectPaths));
    }

    int periodInDays() {
        if (hasArgs()) {
            return Math.abs(Integer.parseInt(getValue(ArgName.periodInDays, ArgName.periodInDays.defaultValue())));
        }
        return Integer.parseInt(ArgName.periodInDays.defaultValue());
    }

    String committerEmail() {
        if (hasArgs()) {
            return getValue(ArgName.committerEmail, ArgName.committerEmail.defaultValue());
        }
        return ArgName.committerEmail.defaultValue();
    }

    LocalDate startDate() {
        String[] date = ArgName.startDate.defaultValue().split("-");
        if (hasArgs()) {
            date = getValue(ArgName.startDate, LocalDate.now().minusDays(periodInDays()).format(yyyy_MM_dd)).split("-");
        }
        return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
    }

    LocalDate endDate() {
        String[] date = ArgName.endDate.defaultValue().split("-");
        if (hasArgs()) {
            date = getValue(ArgName.endDate, ArgName.endDate.defaultValue()).split("-");
        }
        return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
    }

    String itemFileNamePrefix() {
        if (hasArgs()) {
            return getValue(ArgName.itemFileNamePrefix, ArgName.itemFileNamePrefix.defaultValue());
        }
        return ArgName.itemFileNamePrefix.defaultValue();
    }

    VersionControlSystem versionControlSystem() {
        if (hasArgs()) {
            String vcs = getValue(ArgName.versionControlSystem, ArgName.versionControlSystem.defaultValue());
            return VersionControlSystem.valueFor(vcs.toUpperCase());
        }
        return VersionControlSystem.valueOf(ArgName.versionControlSystem.defaultValue());
    }

    CodeProtection codeProtection() {
        if (hasArgs()) {
            String codeProtection = getValue(ArgName.codeProtection, ArgName.codeProtection.defaultValue());
            return CodeProtection.valueFor(codeProtection);
        }
        return CodeProtection.valueFor(ArgName.codeProtection.defaultValue());
    }

    String toolkitUsername() {
        if (hasArgs()) {
            return getValue(ArgName.toolkitUsername, ArgName.toolkitUsername.defaultValue()).trim().toUpperCase();
        }
        return ArgName.toolkitUsername.defaultValue();
    }

    String emailDomain() {
        return ArgName.emailDomain.defaultValue();
    }

    String toolkitUserEmail() {
        String userEmail = toolkitUsername() + emailDomain();
        return userEmail.toLowerCase();
    }

    String toolkitPassword() {
        if (hasArgs()) {
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

}
