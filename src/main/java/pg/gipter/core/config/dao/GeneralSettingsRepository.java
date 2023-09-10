package pg.gipter.core.config.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import pg.gipter.core.config.GeneralSettings;
import pg.gipter.core.dao.MongoDaoConfig;

import java.util.Optional;

class GeneralSettingsRepository extends MongoDaoConfig implements GeneralSettingsDao {
    protected GeneralSettingsRepository() {
        super(GeneralSettings.COLLECTION_NAME);
    }

    @Override
    public Optional<String> getLatestGithubToken() {
        FindIterable<GeneralSettings> settings = collection.find(
                Filters.exists("githubToken", true),
                GeneralSettings.class
        );
        try (MongoCursor<GeneralSettings> cursor = settings.cursor()) {
            if (cursor.hasNext()) {
                return Optional.of(cursor.next().getGithubToken());
            }
        }
        return Optional.empty();
    }
}
