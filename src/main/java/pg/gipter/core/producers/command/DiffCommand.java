package pg.gipter.core.producers.command;

import java.util.List;

public interface DiffCommand {
    List<String> commandAsList();
    List<String> updateRepositoriesCommand();
}
