package pg.gipter.producer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class GitCommandCreator {

    private GitCommandCreator() { }

    static String gitCommandAsString(String author, String committerEmail, String startDate, String endDate) {
        StringBuilder builder = new StringBuilder("git log -p --all");
        if (notEmpty(author)) {
            builder.append(" --author='").append(author).append("'");
        }
        if (notEmpty(committerEmail)) {
            builder.append(" --author=").append(committerEmail);
        }
        builder.append(" --since ").append(startDate);
        builder.append(" --until ").append(endDate);

        return builder.toString();
    }

    static List<String> gitCommandAsList(String author, String committerEmail, String startDate, String endDate) {
        List<String> command = new LinkedList<>(Arrays.asList("git", "log", "-p", "--all"));
        if (notEmpty(author)) {
            command.add("--author=" + author);
        }
        if (notEmpty(committerEmail)) {
            command.add("--author=" + committerEmail);
        }

        command.add("--since");
        command.add(startDate);
        command.add("--until");
        command.add(endDate);

        return command;
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
