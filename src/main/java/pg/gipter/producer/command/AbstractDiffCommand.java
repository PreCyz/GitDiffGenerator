package pg.gipter.producer.command;

import pg.gipter.producer.ApplicationProperties;

import java.util.List;

abstract class AbstractDiffCommand implements DiffCommand {

    protected final ApplicationProperties appProps;

    protected AbstractDiffCommand(ApplicationProperties appProps) {
        this.appProps = appProps;
    }

    abstract List<String> getInitialCommand();
}
