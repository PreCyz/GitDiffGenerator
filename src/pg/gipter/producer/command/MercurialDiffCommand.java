package pg.gipter.producer.command;

import pg.gipter.producer.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class MercurialDiffCommand extends AbstractDiffCommand {

    MercurialDiffCommand(boolean codeProtected) {
        super(codeProtected);
    }

    @Override
    public List<String> commandAsList(String author, String committerEmail, String startDate, String endDate) {
        List<String> command = getInitialCommand();
        if (StringUtils.notEmpty(author)) {
            command.add("--user");
            command.add(author);
        }
        if (StringUtils.notEmpty(committerEmail)) {
            command.add("--user");
            command.add(committerEmail);
        }

        command.add("--date");
        command.add(String.format("\"%s to %s\"",
                startDate.replace("/", "-"),
                endDate.replace("/", "-")
        ));

        return command;
    }

    @Override
    List<String> getInitialCommand() {
        LinkedList<String> initialCommand = new LinkedList<>(Arrays.asList("hg", "log"));
        if (codeProtected) {
            initialCommand.add("--style");
            initialCommand.add("changelog");
        } else {
            initialCommand.add("--patch");
        }
        return initialCommand;
    }

}
