package pg.gipter.dao;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import io.github.cbartosiak.bson.codecs.jsr310.localdatetime.LocalDateTimeAsStringCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
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

    private String host;
    private String username;
    private String password;
    private String databaseName;
    private static MongoClient mongoClient;
    protected MongoDatabase database;

    private boolean statisticsAvailable;

    public MongoDaoConfig() {
        logger = LoggerFactory.getLogger(getClass());
        if (mongoClient == null) {
            Optional<Properties> properties = loadProperties();
            if (properties.isPresent()) {
                Properties dbConfig = properties.get();
                PasswordUtils.decryptPassword(dbConfig, "db.password");
                host = dbConfig.getProperty("db.host");
                username = dbConfig.getProperty("db.username");
                password = dbConfig.getProperty("db.password");
                databaseName = dbConfig.getProperty("db.dbName");
                init();
                statisticsAvailable = true;
            }
        }
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
            logger.warn("Error when loading {}. Exception message is: {}", DB_CONFIG, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    private void init() {
        MongoClientOptions.Builder mongoClientOptionsBuilder = MongoClientOptions.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED);
        String uri = String.format("mongodb+srv://%s:%s@%s", username, password, host);
        MongoClientURI mongoClientURI = new MongoClientURI(uri, mongoClientOptionsBuilder);
        mongoClient = new MongoClient(mongoClientURI);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new LocalDateTimeAsStringCodec())
        );

        database = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry);
        logger.info("Connection to database established.");
    }

    public boolean isStatisticsAvailable() {
        return statisticsAvailable;
    }

    public void refreshConnection() {
        logger.info("Refreshing connection to database.");
        mongoClient = null;
        init();
    }
}
