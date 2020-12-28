package pg.gipter.core.dao.data;

public final class DataDaoFactory {
    private DataDaoFactory() { }

    public static DataDao getDataDao() {
        return DataAccess.getInstance();
    }
}
