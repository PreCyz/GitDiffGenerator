package pg.gipter.core.dao;

import pg.gipter.core.dao.configuration.*;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dao.data.DataDaoFactory;
import pg.gipter.statistics.dao.StatisticDao;
import pg.gipter.statistics.dao.StatisticDaoFactory;

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
