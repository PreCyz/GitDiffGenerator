package pg.gipter.env;

import pg.gipter.core.model.CipherDetails;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public interface EnvSettings {
    Optional<CipherDetails> loadCipherDetails() throws IOException;
    default CipherDetails backupCipherDetails() throws IOException {
        return loadCipherDetails().orElseGet(CipherDetails::new);
    }
    Optional<Properties> loadDbProperties();
    default Path settingsPath() {
        return Paths.get("settings.json");
    }
    default Path connectionPath() {
        return Paths.get("db.connection");
    }
}
