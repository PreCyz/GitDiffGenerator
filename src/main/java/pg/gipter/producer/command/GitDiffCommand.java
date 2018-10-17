package pg.gipter.producer.command;

import pg.gipter.producer.util.StringUtils;
import pg.gipter.settings.ApplicationProperties;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static pg.gipter.Main.yyyy_MM_dd;

final class GitDiffCommand extends AbstractDiffCommand {

    GitDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        List<String> command = getInitialCommand();
        if (StringUtils.notEmpty(appProps.author())) {
            command.add("--author=" + appProps.author());
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            command.add("--author=" + appProps.committerEmail());
        }

        command.add("--since");
        command.add(appProps.startDate().format(yyyy_MM_dd));
        command.add("--until");
        command.add(appProps.endDate().format(yyyy_MM_dd));

        return command;
    }

    List<String> getInitialCommand() {
        List<String> initialCommand = new LinkedList<>(Arrays.asList("git", "log"));
        switch (appProps.codeProtection()) {
            case NONE:
                initialCommand.add("--patch");
                initialCommand.add("--all");
                break;
            case SIMPLE:
                initialCommand.add("--decorate");
                break;

        }
        return initialCommand;
    }
}
