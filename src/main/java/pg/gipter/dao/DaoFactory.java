package pg.gipter.dao;

import pg.gipter.configuration.ConfigurationDao;
import pg.gipter.configuration.ConfigurationDaoFactory;
import pg.gipter.data.DataDao;
import pg.gipter.data.DataDaoFactory;
import pg.gipter.statistic.dao.StatisticDao;
import pg.gipter.statistic.dao.StatisticDaoFactory;

public final class DaoFactory {
    private static ConfigurationDao configurationDao;
    private static DataDao dataDao;

    private DaoFactory() {}

    public static ConfigurationDao getConfigurationDao() {
        if (configurationDao == null) {
            configurationDao = ConfigurationDaoFactory.getConfigurationDao();
        }
        return configurationDao;
    }

    public static DataDao getDataDao() {
        if (dataDao == null) {
            dataDao = DataDaoFactory.getDataDao();
        }
        return dataDao;
    }

    public static StatisticDao getStatisticDao() {
        return StatisticDaoFactory.getStatisticDao();
    }

    //Used in tests
    public static void reset() {
        configurationDao = null;
        dataDao = null;
    }
}
