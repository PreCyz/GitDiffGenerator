package pg.gipter.producer.command;

import java.util.List;

abstract class AbstractDiffCommand implements DiffCommand {

    protected final boolean codeProtected;

    protected AbstractDiffCommand(boolean codeProtected) {
        this.codeProtected = codeProtected;
    }

    abstract List<String> getInitialCommand();
}
