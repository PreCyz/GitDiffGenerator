package pg.gipter.producer.command;

import pg.gipter.producer.util.StringUtils;
import pg.gipter.settings.ApplicationProperties;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static pg.gipter.Main.yyyy_MM_dd;


final class SvnDiffCommand extends AbstractDiffCommand {

    SvnDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        List<String> command = getInitialCommand();
        if (StringUtils.notEmpty(appProps.author())) {
            command.add("--search");
            command.add(appProps.author());
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            command.add("--search");
            command.add(appProps.committerEmail());
        }

        command.add("--revision");
        command.add(String.format("{%s}:{%s}",
                appProps.startDate().format(yyyy_MM_dd),
                //svn measure dates from 12am so 1 day needs to be added to get all changes from actual end date.
                appProps.endDate().plusDays(1).format(yyyy_MM_dd)
        ));

        return command;
    }

    @Override
    List<String> getInitialCommand() {
        LinkedList<String> initialCommand = new LinkedList<>(Arrays.asList("svn", "log"));
        switch (appProps.codeProtection()) {
            case NONE:
                initialCommand.add("--diff");
                break;
            case SIMPLE:
                initialCommand.add("--verbose");
                break;
        }
        return initialCommand;
    }

}
