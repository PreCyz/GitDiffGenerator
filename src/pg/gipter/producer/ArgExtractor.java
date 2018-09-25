package pg.gipter.producer;

import pg.gipter.producer.command.VersionControlSystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

/** Created by Pawel Gawedzki on 17-Sep-2018.*/
final class ArgExtractor {

    static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    enum ArgName {
        author("NO_AUTHOR_GIVEN"),
        itemPath("NO_ITEM_PATH_GIVEN"),
        projectPath("NO_PROJECT_PATH_GIVEN"),
        gitBashPath("NO_GIT_BASH_GIVEN"),
        minusDays("7"),
        committerEmail(""),
        startDate(LocalDate.now().minusDays(Integer.parseInt(minusDays.defaultValue)).format(yyyyMMdd)),
        endDate(LocalDate.now().format(yyyyMMdd)),
        itemFileName(""),
        versionControlSystem(VersionControlSystem.GIT.name());

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
            String author = argument.get();
            return author.substring(author.indexOf("=") + 1);
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
        return Integer.parseInt(ArgName.gitBashPath.defaultValue());
    }

    static String gitBashPath(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.gitBashPath, ArgName.gitBashPath.defaultValue());
        }
        return ArgName.gitBashPath.defaultValue();
    }

    static String gitCommitterEmail(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.committerEmail, ArgName.committerEmail.defaultValue());
        }
        return ArgName.committerEmail.defaultValue();
    }

    static String startDate(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.startDate, ArgName.startDate.defaultValue());
        }
        return ArgName.startDate.defaultValue();
    }

    static String endDate(String[] args) {
        if (hasArgs(args)) {
            return getValue(args, ArgName.endDate, ArgName.endDate.defaultValue());
        }
        return ArgName.endDate.defaultValue();
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
        return VersionControlSystem.valueOf(ArgName.itemFileName.defaultValue());
    }

}
