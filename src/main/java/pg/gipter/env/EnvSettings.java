package pg.gipter.env;

import pg.gipter.core.model.CipherDetails;

import java.util.Optional;
import java.util.Properties;

public interface EnvSettings {
    Optional<CipherDetails> loadCipherDetails();
    Optional<Properties> loadDbProperties();
}
