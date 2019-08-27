package pg.gipter.dao;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import io.github.cbartosiak.bson.codecs.jsr310.localdatetime.LocalDateTimeAsStringCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.Properties;

public abstract class MongoDaoConfig {

    protected final Logger logger;

    private String host;
    private String username;
    private String password;
    private String databaseName;
    private MongoClient mongoClient;
    protected MongoDatabase database;

    private boolean statisticsAvailable;

    public MongoDaoConfig() {
        logger = LoggerFactory.getLogger(getClass());
        Optional<Properties> properties = loadProperties();
        if (properties.isPresent()) {
            Properties dbConfig = properties.get();
            decryptPassword(dbConfig);
            host = dbConfig.getProperty("db.host");
            username = dbConfig.getProperty("db.username");
            password = dbConfig.getProperty("db.password");
            databaseName = dbConfig.getProperty("db.databaseName");
            init();
            statisticsAvailable = true;
        }
    }

    private Optional<Properties> loadProperties() {
        String fileName = "db.properties";
        Properties properties;

        try (InputStream fis = new FileInputStream(fileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException | NullPointerException e) {
            logger.warn("Error when loading {}. Exception message is: {}", fileName, e.getMessage());
            properties = null;
        }
        return Optional.ofNullable(properties);
    }

    private void decryptPassword(Properties properties) {
        try {
            properties.replace("db.password", CryptoUtils.decrypt(properties.getProperty("db.password")));
        } catch (GeneralSecurityException e) {
            logger.warn("Can not decode property. {}", e.getMessage(), e);
        }
    }

    private void init() {
        MongoClientOptions.Builder mongoClientOptionsBuilder = MongoClientOptions.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED);
        String uri = String.format("mongodb+srv://%s:%s@%s", username, password, host);
        MongoClientURI mongoClientURI = new MongoClientURI(uri, mongoClientOptionsBuilder);
        this.mongoClient = new MongoClient(mongoClientURI);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new LocalDateTimeAsStringCodec())
        );

        this.database = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry);
    }

    public boolean isStatisticsAvailable() {
        return statisticsAvailable;
    }
}
