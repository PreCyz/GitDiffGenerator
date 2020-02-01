package pg.gipter.core.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.PasswordUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public abstract class MongoDaoConfig {

    protected final Logger logger;

    private final String DB_CONFIG = "db.properties";

    private String collectionName;
    protected MongoCollection<Document> collection;

    private boolean statisticsAvailable;

    protected MongoDaoConfig(String collectionName) {
        logger = LoggerFactory.getLogger(getClass());
        this.collectionName = collectionName;
        init(loadProperties().orElseGet(() -> MongoConfig.dbProperties));
    }

    private Optional<Properties> loadProperties() {
        Properties properties;

        try (InputStream fis = new FileInputStream(DB_CONFIG);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException | NullPointerException e) {
            logger.warn("Could not load [{}] because: {}", DB_CONFIG, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    private void init(Properties dbConfig) {
        try {
            PasswordUtils.decryptPassword(dbConfig, "db.password");
            String host = dbConfig.getProperty("db.host");
            String username = dbConfig.getProperty("db.username");
            String password = dbConfig.getProperty("db.password");
            String databaseName = dbConfig.getProperty("db.dbName");

            MongoClientOptions.Builder mongoClientOptionsBuilder = MongoClientOptions.builder()
                    .writeConcern(WriteConcern.ACKNOWLEDGED);
            String uri = String.format("mongodb+srv://%s:%s@%s", username, password, host);
            MongoClientURI mongoClientURI = new MongoClientURI(uri, mongoClientOptionsBuilder);
            MongoClient mongoClient = new MongoClient(mongoClientURI);

            collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
            statisticsAvailable = true;

            logger.info("Connection to the database established. [host: {}, databaseName: {}]", host, databaseName);
        } catch (Exception ex) {
            logger.error("Can not establish connection to the database.");
            statisticsAvailable = false;
        }
    }

    public boolean isStatisticsAvailable() {
        return statisticsAvailable;
    }

}
