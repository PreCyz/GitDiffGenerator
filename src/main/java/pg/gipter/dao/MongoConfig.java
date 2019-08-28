package pg.gipter.dao;

import java.util.Properties;

public final class MongoConfig {

    private MongoConfig() { }

    final static String host = "gipter-test-ruzxs.mongodb.net/test?retryWrites=true&w=majority";
    final static String username = "gipter";
    final static String password = "fYTuuYRIxR0=";
    final static String dbName = "gipter";

    final static Properties dbProperties = new Properties();
    static {
        dbProperties.put("db.host", host);
        dbProperties.put("db.username", username);
        dbProperties.put("db.password", password);
        dbProperties.put("db.dbName", dbName);
    }
}
