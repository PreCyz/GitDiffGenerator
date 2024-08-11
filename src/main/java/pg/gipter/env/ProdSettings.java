package pg.gipter.env;

import pg.gipter.Environment;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.CookiesService;
import pg.gipter.services.SettingsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.Properties;

public class ProdSettings extends DevSettings {

    private final SettingsService settingsService;

    protected ProdSettings(Environment environment) {
        super(environment);
        settingsService = new SettingsService();
    }

    @Override
    public Optional<CipherDetails> loadCipherDetails() throws IOException {
        if (!Files.exists(settingsPath())) {
            downloadSettingsFile();
        }
        return super.loadCipherDetails();
    }

    private void downloadSettingsFile() throws IOException {
        String settingsFileName = "settings.txt";
        try {
            File settingsTxt = settingsService.downloadAsset(settingsFileName, CookiesService.getFedAuthString());
            Files.deleteIfExists(settingsPath());
            boolean result = settingsTxt.renameTo(settingsPath().toFile());
            if (!result) {
                logger.warn("Could not rename the file [{}].", settingsFileName);
            }
        } catch (IOException e) {
            logger.error("Could not download asset [{}]", settingsFileName, e);
            throw e;
        }
    }

    @Override
    public Optional<Properties> loadDbProperties() {
        if (!Files.exists(connectionPath())) {
            downloadDbConnectionFile();
        }
        return super.loadDbProperties();
    }

    private void downloadDbConnectionFile() {
        Path bck = Paths.get("db.connection.bck");
        try {
            Files.deleteIfExists(bck);
            if (connectionPath().toFile().exists()) {
                Files.move(connectionPath(), bck);
            }

            File file = settingsService.downloadAsset(connectionPath().toString(), CookiesService.getFedAuthString());

            if (file.exists()) {
                Files.deleteIfExists(bck);
                logger.info("[{}] downloaded", connectionPath());
            } else {
                Files.move(bck, connectionPath());
                logger.warn("[{}] restored", connectionPath());
            }
        } catch (IOException e) {
            logger.error("Could not download asset [{}]", connectionPath());
        }
    }
}
