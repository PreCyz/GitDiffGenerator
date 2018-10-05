package pg.gipter.producer.command;

import java.util.List;

public interface DiffCommand {
    List<String> commandAsList(String author, String committerEmail, String startDate, String endDate);
}
