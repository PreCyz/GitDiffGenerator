package pg.gipter.core.config.dao;

import java.util.Optional;

public interface GeneralSettingsDao {
    Optional<String> getLatestGithubToken();
}
