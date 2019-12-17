package pg.gipter.dao;

public final class DaoFactory {
    private static PropertiesDao propertiesDao;
    private static DataDao dataDao;
    private static SecurityDao securityDao;

    private DaoFactory() {}

    public static PropertiesDao getPropertiesDao() {
        if (propertiesDao == null) {
            propertiesDao = new PropertiesDaoImpl();
        }
        return propertiesDao;
    }

    public static DataDao getDataDao() {
        if (dataDao == null) {
            dataDao = new DataDaoImpl();
        }
        return dataDao;
    }

    public static StatisticDao getStatisticDao() {
        return new StatisticDaoImpl();
    }

    public static SecurityDao getSecurityDao() {
        if (securityDao == null) {
            securityDao = new SecurityDaoImpl();
        }
        return securityDao;
    }
}
