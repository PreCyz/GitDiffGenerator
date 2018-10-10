package pg.gipter.producer.command;

import pg.gipter.producer.ApplicationProperties;
import pg.gipter.producer.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static pg.gipter.Main.yyyy_MM_dd;

final class MercurialDiffCommand extends AbstractDiffCommand {

    MercurialDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        List<String> command = getInitialCommand();
        if (StringUtils.notEmpty(appProps.author())) {
            command.add("--user");
            command.add(appProps.author());
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            command.add("--user");
            command.add(appProps.committerEmail());
        }

        command.add("--date");
        command.add(String.format("\"%s to %s\"", appProps.startDate().format(yyyy_MM_dd), appProps.endDate().format(yyyy_MM_dd)));

        return command;
    }

    @Override
    List<String> getInitialCommand() {
        LinkedList<String> initialCommand = new LinkedList<>(Arrays.asList("hg", "log"));
        if (appProps.isCodeProtected()) {
            initialCommand.add("--style");
            initialCommand.add("changelog");
        } else {
            initialCommand.add("--patch");
        }
        return initialCommand;
    }

}
