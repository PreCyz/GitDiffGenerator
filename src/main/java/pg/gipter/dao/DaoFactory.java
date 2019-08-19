package pg.gipter.dao;

public final class DaoFactory {
    private DaoFactory() {}

    public static PropertiesDao getPropertiesDao() {
        return new PropertiesDaoImpl();
    }

    public static DataDao getDataDao() {
        return new DataDaoImpl();
    }

    public static PropertiesConverter getPropertiesConverter() {
        return new PropertiesConverterImpl();
    }
}
