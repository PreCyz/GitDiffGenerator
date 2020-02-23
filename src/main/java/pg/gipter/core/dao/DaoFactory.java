package pg.gipter.core.dao;

import pg.gipter.core.dao.configuration.CachedConfiguration;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.dao.configuration.SecurityProvider;
import pg.gipter.core.dao.configuration.SecurityProviderFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dao.data.DataDaoFactory;
import pg.gipter.statistic.dao.StatisticDao;
import pg.gipter.statistic.dao.StatisticDaoFactory;

public final class DaoFactory {

    private DaoFactory() {
    }

    public static CachedConfiguration getCachedConfiguration() {
        return ConfigurationDaoFactory.getCachedConfigurationDao();
    }

    public static DataDao getDataDao() {
        return DataDaoFactory.getDataDao();
    }

    public static StatisticDao getStatisticDao() {
        return StatisticDaoFactory.getStatisticDao();
    }

    public static SecurityProvider getSecurityProvider() {
        return SecurityProviderFactory.getSecurityProvider();
    }
}
