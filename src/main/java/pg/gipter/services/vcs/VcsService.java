package pg.gipter.services.vcs;

import java.util.Optional;

public interface VcsService {
    Optional<String> getUserName(String projectPath);
    Optional<String> getUserEmail(String projectPath);
}
