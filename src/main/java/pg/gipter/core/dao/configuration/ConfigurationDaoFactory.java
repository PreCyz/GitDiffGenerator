package pg.gipter.core.dao.configuration;

public final class ConfigurationDaoFactory {
    private ConfigurationDaoFactory() {}

    public static ConfigurationDao getConfigurationDao() {
        return new ApplicationConfigurationDao();
    }
}
