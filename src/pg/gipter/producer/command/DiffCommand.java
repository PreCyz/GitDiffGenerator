package pg.gipter.producer.command;

import java.util.List;

public interface DiffCommand {
    String commandAsString(String author, String committerEmail, String startDate, String endDate);
    List<String> commandAsList(String author, String committerEmail, String startDate, String endDate);
}
