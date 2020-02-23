package pg.gipter.core.dao.configuration;

public final class ConfigurationDaoFactory {
    private ConfigurationDaoFactory() {}

    static ConfigurationDao getConfigurationDao() {
        return ApplicationConfiguration.getInstance();
    }

    public static CachedConfiguration getCachedConfigurationDao() {
        return CachedConfigurationProxy.getInstance();
    }
}
