package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

abstract class ApplicationJsonReader {

    protected final Logger logger;
    private JsonSerializer<Configuration> passwordSerializer;
    private JsonDeserializer<Configuration> passwordDeserializer;

    protected ApplicationJsonReader() {
        logger = LoggerFactory.getLogger(getClass());
        passwordSerializer = PasswordSerializer.getInstance();
        passwordDeserializer = PasswordDeserializer.getInstance();
    }

    protected final Configuration readJsonConfig() {
        Configuration configuration = new Configuration();
        Gson gson = customGsonForDeserialization().orElseGet(() ->
                new GsonBuilder()
                        .registerTypeAdapter(Configuration.class, passwordDeserializer)
                        .create());
        try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            configuration = gson.fromJson(reader, Configuration.class);
        } catch (IOException | NullPointerException e) {
            logger.warn("Warning when loading {}. Exception message is: {}",
                    DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
        }
        return configuration;
    }

    protected final void writeJsonConfig(Configuration configuration, Class<?> clazz) {
        Gson gson = customGsonForSerialization().orElseGet(() ->
                new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(Configuration.class, passwordSerializer)
                        .create()
        );
        String json = gson.toJson(configuration, Configuration.class);
        try (OutputStream os = new FileOutputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
            logger.info("File {} updated with {}.", DaoConstants.APPLICATION_PROPERTIES_JSON, clazz.getSimpleName());
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}",
                    DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }

    protected Optional<Gson> customGsonForSerialization() {
        return Optional.empty();
    }

    protected Optional<Gson> customGsonForDeserialization() {
        return Optional.empty();
    }
}
