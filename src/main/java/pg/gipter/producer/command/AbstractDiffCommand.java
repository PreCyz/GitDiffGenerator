package pg.gipter.producer.command;

import pg.gipter.settings.ApplicationProperties;

import java.util.List;

abstract class AbstractDiffCommand implements DiffCommand {

    protected final ApplicationProperties appProps;

    protected AbstractDiffCommand(ApplicationProperties appProps) {
        this.appProps = appProps;
    }

    protected String wrapWithQuotationMarks(String value) {
        return "\"" + value + "\"";
    }

    abstract List<String> getInitialCommand();
}
