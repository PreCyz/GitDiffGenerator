package pg.gipter.core.dao;

import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dao.data.DataDaoFactory;
import pg.gipter.statistic.dao.StatisticDao;
import pg.gipter.statistic.dao.StatisticDaoFactory;

public final class DaoFactory {
    private static ConfigurationDao configurationDao;
    private static DataDao dataDao;
    private static SecurityDao securityDao;

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

    public static SecurityDao getSecurityDao() {
        if (securityDao == null) {
            securityDao = new SecurityDaoImpl();
        }
        return securityDao;
    }

    //Used in tests
    public static void reset() {
        configurationDao = null;
        dataDao = null;
        securityDao = null;
    }
}
