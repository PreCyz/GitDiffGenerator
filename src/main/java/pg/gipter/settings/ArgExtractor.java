package pg.gipter.settings;

import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.producer.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

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
        codeProtected("false");

        private String defaultValue;

        ArgName(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        String defaultValue() {
            return defaultValue;
        }
    }
    private ArgExtractor() {}

    private static boolean hasArgs(String[] args) {
        return args != null && args.length > 0;
    }

    static String author(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.author, ArgName.author.defaultValue());
        }
        return ArgName.author.defaultValue();
    }

    private static String getValue(String[] args, final ArgName argName, String defaultValue) {
        Optional<String> argument = Arrays.stream(args).filter(arg -> arg.startsWith(argName.name())).findAny();
        if (argument.isPresent()) {
            String value = argument.get();
            return value.substring(value.indexOf("=") + 1);
        }
        return defaultValue;
    }

    static String path(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.itemPath, ArgName.itemPath.defaultValue());
        }
        return ArgName.itemPath.defaultValue();
    }

    static String projectPaths(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.projectPath, ArgName.projectPath.defaultValue());
        }
        return ArgName.projectPath.defaultValue();
    }

    static int days(String[] args) {
        if (hasArgs(args)) {
            return Integer.parseInt(getValue(args, ArgName.minusDays, ArgName.minusDays.defaultValue()));
        }
        return Integer.parseInt(ArgName.minusDays.defaultValue());
    }

    static String committerEmail(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.committerEmail, ArgName.committerEmail.defaultValue());
        }
        return ArgName.committerEmail.defaultValue();
    }

    static LocalDate startDate(String[] args) {
        String[] date = ArgName.startDate.defaultValue().split("-");
        if (hasArgs(args)) {
            date = getValue(args, ArgName.startDate, ArgName.startDate.defaultValue()).split("-");
        }
        return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
    }

    static LocalDate endDate(String[] args) {
        String[] date = ArgName.endDate.defaultValue().split("-");
        if (hasArgs(args)) {
            date = getValue(args, ArgName.endDate, ArgName.endDate.defaultValue()).split("-");
        }
        return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
    }

    static String itemFileName(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.itemFileName, ArgName.itemFileName.defaultValue());
        }
        return ArgName.itemFileName.defaultValue();
    }

    static VersionControlSystem versionControlSystem(String[] args) {
        if (hasArgs(args)) {
            String vcs = getValue(args, ArgName.versionControlSystem, ArgName.versionControlSystem.defaultValue());
            return VersionControlSystem.valueFor(vcs.toUpperCase());
        }
        return VersionControlSystem.valueOf(ArgName.versionControlSystem.defaultValue());
    }

    static boolean codeProtected(String[] args) {
        if (hasArgs(args)) {
            String codeProtected = getValue(args, ArgName.codeProtected, ArgName.codeProtected.defaultValue());
            return StringUtils.getBoolean(codeProtected);
        }
        return StringUtils.getBoolean(ArgName.codeProtected.defaultValue());
    }

}
