package pg.gipter.producer.command;

import pg.gipter.producer.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class GitDiffCommand extends AbstractDiffCommand {

    GitDiffCommand(boolean codeProtected) {
        super(codeProtected);
    }

    @Override
    public List<String> commandAsList(String author, String committerEmail, String startDate, String endDate) {
        List<String> command = getInitialCommand();
        if (StringUtils.notEmpty(author)) {
            command.add("--author=" + author);
        }
        if (StringUtils.notEmpty(committerEmail)) {
            command.add("--author=" + committerEmail);
        }

        command.add("--since");
        command.add(startDate);
        command.add("--until");
        command.add(endDate);

        return command;
    }

    List<String> getInitialCommand() {
        List<String> initialCommand = new LinkedList<>(Arrays.asList("git", "log"));
        if (codeProtected) {
            initialCommand.add("--decorate");
        } else {
            initialCommand.add("--patch");
            initialCommand.add("--all");
        }
        return initialCommand;
    }
}
