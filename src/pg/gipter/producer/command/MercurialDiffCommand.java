package pg.gipter.producer.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class MercurialDiffCommand implements DiffCommand {

    MercurialDiffCommand() { }

    @Override
    public String commandAsString(String author, String committerEmail, String startDate, String endDate) {
        StringBuilder builder = new StringBuilder("hg log -p");
        if (CommandUtils.notEmpty(author)) {
            builder.append(" --user ").append(author);
        }
        if (CommandUtils.notEmpty(committerEmail)) {
            builder.append(" --user ").append(committerEmail);
        }
        builder.append(" --date \"")
                .append(startDate.replace("/", "-"))
                .append(" to ")
                .append(endDate.replace("/", "-"))
                .append("\"");

        return builder.toString();
    }

    @Override
    public List<String> commandAsList(String author, String committerEmail, String startDate, String endDate) {
        List<String> command = new LinkedList<>(Arrays.asList("hg", "log", "-p"));
        if (CommandUtils.notEmpty(author)) {
            command.add("--user");
            command.add(author);
        }
        if (CommandUtils.notEmpty(committerEmail)) {
            command.add("--user");
            command.add(committerEmail);
        }

        command.add("--date");
        command.add("\"" + startDate.replace("/", "-") + " to " + endDate.replace("/", "-") + "\"");

        return command;
    }

}
