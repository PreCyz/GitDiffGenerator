package pg.gipter.producer.command;

import pg.gipter.producer.util.StringUtils;
import pg.gipter.settings.ApplicationProperties;

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
        command.addAll(authors());
        command.add("--date");
        command.add(String.format("\"%s to %s\"", appProps.startDate().format(yyyy_MM_dd), appProps.endDate().format(yyyy_MM_dd)));
        return command;
    }

    @Override
    List<String> getInitialCommand() {
        LinkedList<String> initialCommand = new LinkedList<>(Arrays.asList("hg", "log"));
        switch (appProps.codeProtection()) {
            case NONE:
                initialCommand.add("--patch");
                break;
            case SIMPLE:
                initialCommand.add("--style");
                initialCommand.add("changelog");
                break;
        }
        return initialCommand;
    }

    List<String> authors() {
        List<String> authors = new LinkedList<>();
        if (!appProps.mercurialAuthor().isEmpty()) {
            authors.add("--user");
            authors.add(appProps.mercurialAuthor());
        } else {
            for (String author : appProps.authors()) {
                authors.add("--user");
                authors.add(author);
            }
        }
        if (StringUtils.notEmpty(appProps.committerEmail())) {
            authors.add("--user");
            authors.add(appProps.committerEmail());
        }
        return authors;
    }

}
