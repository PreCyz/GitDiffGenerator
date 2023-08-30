package pg.gipter.env;

import pg.gipter.Environment;
import pg.gipter.core.model.CipherDetails;

import java.util.Optional;
import java.util.Properties;

public class ProdSettings extends DevSettings {

    protected ProdSettings(Environment environment) {
        super(environment);
    }

    @Override
    public Optional<CipherDetails> loadCipherDetails() {
        downloadSettingsFile();
        return super.loadCipherDetails();
    }

    private void downloadSettingsFile() {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public Optional<Properties> loadDbProperties() {
        downloadDbPropertiesFile();
        return super.loadDbProperties();
    }

    private void downloadDbPropertiesFile() {
        throw new RuntimeException("Not implemented yet.");
    }
}
