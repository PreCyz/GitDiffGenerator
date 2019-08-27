package pg.gipter.dao;

import org.junit.jupiter.api.Test;

class MongoDaoTest {

    @Test
    void name() {
        MongoDao dao = new MongoDao();

        dao.connect();
    }
}