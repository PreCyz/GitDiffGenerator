package pg.gipter.services.vcs;

import java.util.Optional;

public interface VcsService {
    Optional<String> getUserName();
    Optional<String> getUserEmail();
    void setProjectPath(String projectPath);
    boolean isVcsAvailableInCommandLine();
}
