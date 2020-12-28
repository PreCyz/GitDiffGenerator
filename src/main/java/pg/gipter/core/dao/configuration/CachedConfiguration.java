package pg.gipter.core.dao.configuration;

public interface CachedConfiguration extends ConfigurationDao {
    void resetCache();
    <T> void updateCachedConfiguration(T value);
}
