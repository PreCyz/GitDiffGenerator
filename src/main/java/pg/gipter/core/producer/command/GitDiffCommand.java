package pg.gipter.core.producer.command;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.utils.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static pg.gipter.core.ApplicationProperties.yyyy_MM_dd;

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

    @Override
    public List<String> updateRepositoriesCommand() {
        return Stream.of("git", "fetch", "--all").collect(toList());
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
        initialCommand.add("--all");
        return initialCommand;
    }

    List<String> authors() {
        List<String> authors = new LinkedList<>();
        if (!appProps.gitAuthor().isEmpty()) {
            authors.add("--author=" + wrapWithQuotationMarks(appProps.gitAuthor()));
        } else {
            authors = appProps.authors()
                    .stream()
                    .map(author -> "--author=" + wrapWithQuotationMarks(author))
                    .collect(toCollection(LinkedList::new));
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            authors.add("--author=" + appProps.committerEmail());
        }
        return authors;
    }
}
