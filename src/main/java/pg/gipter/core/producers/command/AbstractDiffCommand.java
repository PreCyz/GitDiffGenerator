package pg.gipter.core.producers.command;

import pg.gipter.core.ApplicationProperties;

import java.util.List;

abstract class AbstractDiffCommand implements DiffCommand {

    protected final ApplicationProperties appProps;

    protected AbstractDiffCommand(ApplicationProperties appProps) {
        this.appProps = appProps;
    }

    protected String wrapWithQuotationMark(String value) {
        return "\"" + value + "\"";
    }

    protected String wrapWithSingleQuotationMark(String value) {
        return "'" + value + "'";
    }

    abstract List<String> getInitialCommand();
}
