package pg.gipter.data;

public final class DataDaoFactory {
    private DataDaoFactory() { }

    public static DataDao getDataDao() {
        return new DataDaoImpl();
    }
}
