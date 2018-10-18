package pg.gipter.producer.command;

import pg.gipter.settings.ApplicationProperties;

import java.util.Collections;
import java.util.List;


final class EmptyDiffCommand extends AbstractDiffCommand {

    EmptyDiffCommand(ApplicationProperties appProps) {
        super(appProps);
    }

    @Override
    public List<String> commandAsList() {
        return Collections.emptyList();
    }

    @Override
    List<String> getInitialCommand() {
        return Collections.emptyList();
    }

}
