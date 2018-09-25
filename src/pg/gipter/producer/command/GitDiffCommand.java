package pg.gipter.producer.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class GitDiffCommand implements DiffCommand {

    GitDiffCommand() { }

    @Override
    public String commandAsString(String author, String committerEmail, String startDate, String endDate) {
        StringBuilder builder = new StringBuilder("git log -p --all");
        if (CommandUtils.notEmpty(author)) {
            builder.append(" --author='").append(author).append("'");
        }
        if (CommandUtils.notEmpty(committerEmail)) {
            builder.append(" --author=").append(committerEmail);
        }
        builder.append(" --since ").append(startDate);
        builder.append(" --until ").append(endDate);

        return builder.toString();
    }

    @Override
    public List<String> commandAsList(String author, String committerEmail, String startDate, String endDate) {
        List<String> command = new LinkedList<>(Arrays.asList("git", "log", "-p", "--all"));
        if (CommandUtils.notEmpty(author)) {
            command.add("--author=" + author);
        }
        if (CommandUtils.notEmpty(committerEmail)) {
            command.add("--author=" + committerEmail);
        }

        command.add("--since");
        command.add(startDate);
        command.add("--until");
        command.add(endDate);

        return command;
    }

}
