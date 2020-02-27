package pg.gipter.core.producer.command;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.utils.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static pg.gipter.core.ApplicationProperties.yyyy_MM_dd;

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
    public List<String> updateRepositoriesCommand() {
        return Stream.of("svn", "update").collect(toList());
    }

    @Override
    List<String> getInitialCommand() {
        LinkedList<String> initialCommand = new LinkedList<>(Arrays.asList("svn", "log"));
        switch (appProps.itemType()) {
            case SIMPLE:
                initialCommand.add("--diff");
                break;
            case PROTECTED:
                initialCommand.add("--verbose");
                break;
        }
        return initialCommand;
    }

    List<String> authors() {
        List<String> authors = new LinkedList<>();
        if (StringUtils.notEmpty(appProps.svnAuthor())) {
            authors.add("--search");
            authors.add(wrapWithQuotationMarks(appProps.svnAuthor()));
        } else {
            for (String author : appProps.authors()) {
                authors.add("--search");
                authors.add(wrapWithQuotationMarks(author));
            }
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            authors.add("--search");
            authors.add(appProps.committerEmail());
        }
        return authors;
    }

}
