package pg.gipter.core.producer.command;

import java.util.List;

public interface DiffCommand {
    List<String> commandAsList();
    List<String> updateRepositoriesCommand();
}
