package pg.gipter.env;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.Environment;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.SecurityService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;

class DevSettings implements EnvSettings {

    protected static final String SETTINGS_JSON = "settings.json";
    protected static final String DB_CONNECTION = "db.connection";

    protected final Environment environment;

    protected final Logger logger;

    protected CipherDetails cipherDetails;

    protected DevSettings(Environment environment) {
        this.environment = environment;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    protected final String getFileName(String value) {
        return environment == Environment.DEV ?
                value.replace(".", "-" + Environment.DEV.name().toLowerCase() + ".") : value;
    }

    @Override
    public Optional<CipherDetails> loadCipherDetails() throws IOException {
        Optional<CipherDetails> result = Optional.empty();
        String settingsFileName = getFileName(SETTINGS_JSON);
        try (InputStream fis = new FileInputStream(settingsFileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            result = Optional.ofNullable(
                    this.cipherDetails = new Gson().fromJson(reader, CipherDetails.Settings.class).toCipherDetails()
            );
            logger.info("Program settings loaded from [{}].", settingsFileName);
        } catch (Exception e) {
            logger.warn("Could not load [{}] because: {}", settingsFileName, e.getMessage());
            throw new IOException(e);
        }
        return result;
    }

    @Override
    public Optional<Properties> loadDbProperties() {
        Optional<Properties> properties = Optional.empty();
        String dbConnectionFileName = getFileName(DB_CONNECTION);
        try (InputStream fis = new FileInputStream(dbConnectionFileName);
             Scanner scanner = new Scanner(fis)) {

            Properties dbProperties = new Properties();
            SecurityService securityService = SecurityService.getInstance();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] prop = securityService.decrypt(line, cipherDetails).split("=");
                if (prop.length > 2) {
                    dbProperties.put(prop[0], String.join("=", Arrays.copyOfRange(prop, 1, prop.length)));
                } else {
                    dbProperties.put(prop[0], prop[1]);
                }
            }
            logger.info("Database connection loaded from [{}]", dbConnectionFileName);
            properties = Optional.of(dbProperties);
        } catch (IOException | NullPointerException e) {
            logger.warn("Could not load [{}] because: {}", dbConnectionFileName, e.getMessage());
        }
        return properties;
    }

}
