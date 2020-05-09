package pg.gipter.core.producers.command;

import pg.gipter.core.ApplicationProperties;

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
    public List<String> updateRepositoriesCommand() {
        return Collections.emptyList();
    }

    @Override
    List<String> getInitialCommand() {
        return Collections.emptyList();
    }

}
