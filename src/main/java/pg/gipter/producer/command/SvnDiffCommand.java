package pg.gipter.producer.command;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.utils.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static pg.gipter.settings.ApplicationProperties.yyyy_MM_dd;


final class SvnDiffCommand extends AbstractDiffCommand {

    SvnDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        List<String> command = getInitialCommand();
        command.addAll(authors());
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

    List<String> authors() {
        List<String> authors = new LinkedList<>();
        if (StringUtils.notEmpty(appProps.svnAuthor())) {
            authors.add("--search");
            authors.add(appProps.svnAuthor());
        } else {
            for (String author : appProps.authors()) {
                authors.add("--search");
                authors.add(author);
            }
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            authors.add("--search");
            authors.add(appProps.committerEmail());
        }
        return authors;
    }

}
