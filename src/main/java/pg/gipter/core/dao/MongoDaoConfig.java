package pg.gipter.core.dao;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.ProgramSettings;
import pg.gipter.core.config.GeneralSettings;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.statistics.ExceptionDetails;
import pg.gipter.statistics.Statistic;
import pg.gipter.utils.CryptoUtils;

import java.security.GeneralSecurityException;
import java.util.Properties;

public abstract class MongoDaoConfig {

    protected final Logger logger;

    private final String collectionName;
    protected MongoCollection<Document> collection;
    private static MongoClient mongoClient;

    private boolean statisticsAvailable;

    protected MongoDaoConfig(String collectionName) {
        logger = LoggerFactory.getLogger(getClass());
        this.collectionName = collectionName;
    }

    public static void refresh(Properties dbConfig) {
        try {
            mongoClient.close();
            mongoClient = createMongoClient(dbConfig);
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoDaoConfig.class).error("Can not establish connection to the database.", ex);
        }
    }

    public void init() {
        Properties dbConfig = ProgramSettings.getInstance().getDbProperties();
        if (!isValidDbConfig(dbConfig)) {
            return;
        }
        if (mongoClient == null) {
            try {
                mongoClient = createMongoClient(dbConfig);
            } catch (Exception ex) {
                logger.error("Can not establish connection to the database.", ex);
                statisticsAvailable = false;
            }
        }

        if (mongoClient != null) {
            String databaseName = dbConfig.getProperty("db.dbName");
            collection = mongoClient.getDatabase(databaseName).getCollection(collectionName);
            statisticsAvailable = true;
            logger.info("Connection to the database established. [databaseName: {}]", databaseName);
        }
    }

    private static MongoClient createMongoClient(Properties dbConfig) throws GeneralSecurityException {
        CodecRegistry codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        Codec<Document> documentCodec = codecRegistry.get(Document.class);
        Codec<Statistic> statisticCodec = new StatisticCodec(codecRegistry);
        Codec<ExceptionDetails> exceptionDetailsCodec = new ExceptionDetailsCodec(codecRegistry);
        Codec<CipherDetails> cipherDetailsCodec = new CipherDetailsCodec(codecRegistry);
        Codec<GeneralSettings> generalSettingsCodec = new GeneralSettingsCodec(codecRegistry);
        codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(documentCodec,
                        statisticCodec,
                        exceptionDetailsCodec,
                        cipherDetailsCodec,
                        generalSettingsCodec),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        String host = dbConfig.getProperty("db.host");
        String username = dbConfig.getProperty("db.username");
        String password = CryptoUtils.decrypt(dbConfig.getProperty("db.password"));

        String dbUrl = String.format("mongodb+srv://%s:%s@%s", username, password, host);
        ConnectionString connectionString = new ConnectionString(dbUrl);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .codecRegistry(codecRegistry)
                .applyConnectionString(connectionString)
                .retryWrites(true)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    public boolean isStatisticsAvailable() {
        return statisticsAvailable;
    }

    private boolean isValidDbConfig(Properties dbConfig) {
        if (dbConfig == null || dbConfig.isEmpty()) {
            logger.warn("Database config is missing.");
            return false;
        }
        boolean result = true;
        String host = dbConfig.getProperty("db.host");
        if (host == null || host.isEmpty()) {
            result = false;
            logger.warn("Database host is missing.");
        }
        String username = dbConfig.getProperty("db.username");
        if (username == null || username.isEmpty()) {
            logger.warn("Database username is missing.");
            result = false;
        }
        String password = dbConfig.getProperty("db.password");
        if (password == null || password.isEmpty()) {
            logger.warn("Database password is missing.");
            result = false;
        }
        return result;
    }

}
