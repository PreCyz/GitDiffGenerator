package pg.gipter.producer.command;

import pg.gipter.producer.util.StringUtils;
import pg.gipter.settings.ApplicationProperties;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class TfvcDiffCommand extends AbstractDiffCommand {

    TfvcDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        List<String> command = getInitialCommand();
        if (StringUtils.notEmpty(appProps.tfvcAuthor())) {
            command.add("/user:" + appProps.tfvcAuthor());
        }
        /*if (StringUtils.notEmpty(appProps.committerEmail())) {
            command.add("--gitAuthor=" + appProps.committerEmail());
        }*/

        String dateRange = String.format("/version:D\"%s\"~D\"%s\"",
                appProps.startDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                appProps.endDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
        command.add(dateRange);

        return command;
    }

    List<String> getInitialCommand() {
        List<String> initialCommand = new LinkedList<>(Arrays.asList("tf","history", "$/", "/recursive", "/format:detailed", "/noprompt"));
        return initialCommand;
    }
}
