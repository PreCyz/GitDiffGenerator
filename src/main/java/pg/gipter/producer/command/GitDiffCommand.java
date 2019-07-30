package pg.gipter.producer.command;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.utils.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toCollection;
import static pg.gipter.settings.ApplicationProperties.yyyy_MM_dd;

final class GitDiffCommand extends AbstractDiffCommand {

    GitDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        List<String> command = getInitialCommand();
        command.addAll(authors());
        command.add("--since");
        command.add(appProps.startDate().format(yyyy_MM_dd));
        command.add("--until");
        command.add(appProps.endDate().format(yyyy_MM_dd));
        return command;
    }

    List<String> getInitialCommand() {
        List<String> initialCommand = new LinkedList<>(Arrays.asList("git", "log"));
        if (!appProps.isSkipRemote()) {
            initialCommand.add("--remotes=origin*");
        }
        switch (appProps.uploadType()) {
            case SIMPLE:
                initialCommand.add("--patch");
                break;
            case PROTECTED:
                initialCommand.add("--oneline");
                break;

        }
        initialCommand.add("--branches=*");
        return initialCommand;
    }

    List<String> authors() {
        List<String> authors = new LinkedList<>();
        if (!appProps.gitAuthor().isEmpty()) {
            authors.add("--author=" + appProps.gitAuthor());
        } else {
            authors = appProps.authors()
                    .stream()
                    .map(author -> "--author=" + author)
                    .collect(toCollection(LinkedList::new));
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            authors.add("--author=" + appProps.committerEmail());
        }
        return authors;
    }
}
