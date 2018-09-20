package pg.gipter.producer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class GitCommandCreator {

    private static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private GitCommandCreator() { }

    static String gitCommandAsString(String author, String committerEmail, int daysInThePast) {
        LocalDate now = LocalDate.now();
        LocalDate minusDays = now.minusDays(daysInThePast);
        StringBuilder builder = new StringBuilder("git log -p --all");
        if (notEmpty(author)) {
            builder.append(" --author='").append(author).append("'");
        }
        if (notEmpty(committerEmail)) {
            builder.append(" --author=").append(committerEmail);
        }
        builder.append(" --since ").append(minusDays.format(yyyyMMdd));
        builder.append(" --until ").append(now.format(yyyyMMdd));
        return builder.toString();
    }

    static List<String> gitCommandAsList(String author, String committerEmail, int daysInThePast) {
        LocalDate now = LocalDate.now();
        LocalDate minusDays = now.minusDays(daysInThePast);
        List<String> command = new LinkedList<>(Arrays.asList("git", "log", "-p", "--all"));
        if (notEmpty(author)) {
            command.add(" --author=" + author);
        }
        if (notEmpty(committerEmail)) {
            command.add(" --author=" + committerEmail);
        }
        command.add(" --since ");
        command.add(String.valueOf(minusDays.format(yyyyMMdd)));

        command.add(" --until ");
        command.add(String.valueOf(now.format(yyyyMMdd)));

        return command;
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
