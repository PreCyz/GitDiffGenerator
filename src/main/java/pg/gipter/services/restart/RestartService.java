package pg.gipter.services.restart;

import java.util.List;

public interface RestartService {
    void start(List<String> programArguments);
}
