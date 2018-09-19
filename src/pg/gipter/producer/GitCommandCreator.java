package pg.gipter.producer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class GitCommandCreator {

    private static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private GitCommandCreator() { }

    static String gitCommand(String author, String committerEmail, int daysInThePast) {
        LocalDate now = LocalDate.now();
        LocalDate minusDays = now.minusDays(daysInThePast);
        StringBuilder builder = new StringBuilder("git log -p --all");
        if (notEmpty(author)) {
            builder.append(" --author='").append(author).append("'");
        }
        if (notEmpty(committerEmail)) {
            builder.append(" --committer='").append(committerEmail).append("'");
        }
        builder.append(" --since ").append(minusDays.format(yyyyMMdd));
        builder.append(" --until ").append(now.format(yyyyMMdd));
        return builder.toString();
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
