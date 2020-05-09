package pg.gipter.services;

import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;

import java.util.Optional;
import java.util.Properties;

public class DataService {

    private final DataDao dataDao;

    private static class DataServiceHolder {

        private static final DataService INSTANCE = new DataService();
    }
    public static DataService getInstance() {
        return DataServiceHolder.INSTANCE;
    }

    public DataService() {
        this.dataDao = DaoFactory.getDataDao();
    }

    public Optional<Properties> loadDataProperties() {
        return dataDao.loadDataProperties();
    }
}
