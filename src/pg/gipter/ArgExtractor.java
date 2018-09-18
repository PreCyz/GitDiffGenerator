package pg.gipter;

import java.util.Arrays;
import java.util.Optional;

/** Created by Pawel Gawedzki on 17-Sep-2018.*/
final class ArgExtractor {

    enum ArgName {
        author("NO_AUTHOR_GIVEN"),
        itemPath("NO_ITEM_PATH_GIVEN"),
        projectPath("NO_PROJECT_PATH_GIVEN"),
        gitBashPath("C:\\Program Files\\Git\\bin\\bash.exe"),
        minusDays("7"),
        committerEmail("");

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

}
