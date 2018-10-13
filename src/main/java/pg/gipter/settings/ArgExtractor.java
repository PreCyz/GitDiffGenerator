package pg.gipter.settings;

import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.producer.util.StringUtils;

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
        minusDays("7"),
        committerEmail(""),
        startDate(LocalDate.now().minusDays(Integer.parseInt(minusDays.defaultValue)).format(yyyy_MM_dd)),
        endDate(LocalDate.now().format(yyyy_MM_dd)),
        itemFileName(""),
        versionControlSystem(VersionControlSystem.GIT.name()),
        codeProtected("false"),
        toolkitUsername("NO_TOOLKIT_USERNAME_GIVEN"),
        toolkitPassword("NO_TOOLKIT_PASSWORD_GIVEN"),
        toolkitDomain("NCDMZ"),;

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

    int days() {
        if (hasArgs()) {
            return Integer.parseInt(getValue(ArgName.minusDays, ArgName.minusDays.defaultValue()));
        }
        return Integer.parseInt(ArgName.minusDays.defaultValue());
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
            date = getValue(ArgName.startDate, ArgName.startDate.defaultValue()).split("-");
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

    String itemFileName() {
        if (hasArgs()) {
            return getValue(ArgName.itemFileName, ArgName.itemFileName.defaultValue());
        }
        return ArgName.itemFileName.defaultValue();
    }

    VersionControlSystem versionControlSystem() {
        if (hasArgs()) {
            String vcs = getValue(ArgName.versionControlSystem, ArgName.versionControlSystem.defaultValue());
            return VersionControlSystem.valueFor(vcs.toUpperCase());
        }
        return VersionControlSystem.valueOf(ArgName.versionControlSystem.defaultValue());
    }

    boolean codeProtected() {
        if (hasArgs()) {
            String codeProtected = getValue(ArgName.codeProtected, ArgName.codeProtected.defaultValue());
            return StringUtils.getBoolean(codeProtected);
        }
        return StringUtils.getBoolean(ArgName.codeProtected.defaultValue());
    }

    String toolkitUsername() {
        if (hasArgs()) {
            return getValue(ArgName.toolkitUsername, ArgName.toolkitUsername.defaultValue());
        }
        return ArgName.toolkitUsername.defaultValue();
    }

    String toolkitPassword() {
        if (hasArgs()) {
            return getValue(ArgName.toolkitPassword, ArgName.toolkitPassword.defaultValue());
        }
        return ArgName.toolkitPassword.defaultValue();
    }

    String toolkitDomain() {
        if (hasArgs()) {
            return getValue(ArgName.toolkitDomain, ArgName.toolkitDomain.defaultValue());
        }
        return ArgName.toolkitDomain.defaultValue();
    }

}
