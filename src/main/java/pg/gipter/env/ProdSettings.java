package pg.gipter.env;

import pg.gipter.Environment;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.CookiesService;
import pg.gipter.services.SettingsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class ProdSettings extends DevSettings {

    private final SettingsService settingsService;

    protected ProdSettings(Environment environment) {
        super(environment);
        settingsService = new SettingsService();
    }

    @Override
    public Optional<CipherDetails> loadCipherDetails() {
        downloadSettingsFile();
        return super.loadCipherDetails();
    }

    private void downloadSettingsFile() {
        String settingsFileName = "settings.txt";
        try {
            File settingsTxt = settingsService.downloadAsset(settingsFileName, CookiesService.getFedAuthString());
            Files.deleteIfExists(Paths.get("settings.json"));
            boolean result = settingsTxt.renameTo(new File("settings.json"));
            if (!result) {
                logger.warn("Could not rename the file [{}].", settingsFileName);
            }
        } catch (IOException e) {
            logger.error("Could not download asset [{}]", settingsFileName);
        }
    }

    @Override
    public Optional<Properties> loadDbProperties() {
        downloadDbConnectionFile();
        return super.loadDbProperties();
    }

    private void downloadDbConnectionFile() {
        Path dbConnection = Paths.get("db.connection");
        Path bck = Paths.get("db.connection.bck");
        try {
            Files.deleteIfExists(bck);
            if (dbConnection.toFile().exists()) {
                Files.move(dbConnection, bck);
            }

            File file = settingsService.downloadAsset(dbConnection.toString(), CookiesService.getFedAuthString());

            if (file.exists()) {
                Files.deleteIfExists(bck);
                logger.info("[{}] downloaded", dbConnection);
            } else {
                Files.move(bck, dbConnection);
                logger.warn("[{}] restored", dbConnection);
            }
        } catch (IOException e) {
            logger.error("Could not download asset [{}]", dbConnection);
        }
    }
}
