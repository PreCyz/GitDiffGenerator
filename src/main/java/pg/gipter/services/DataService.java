package pg.gipter.services;

import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.dao.data.ProgramData;

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

    public ProgramData loadProgramData() {
        return dataDao.readProgramData();
    }
}
