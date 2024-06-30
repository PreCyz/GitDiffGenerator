package pg.gipter;

import pg.gipter.core.model.CipherDetails;
import pg.gipter.env.EnvSettings;
import pg.gipter.env.EnvSettingsFactory;

import java.io.IOException;
import java.util.Properties;

public final class ProgramSettings {

    private CipherDetails cipherDetails;
    private Properties dbProperties;
    private Environment environment;

    private ProgramSettings() {
    }

    private static class InstanceHolder {
        private static final ProgramSettings INSTANCE = new ProgramSettings();
    }

    static void initProgramSettings(Environment env) throws IOException {
        InstanceHolder.INSTANCE.environment = env;
        initProgramSettings();
    }

    public static void initProgramSettings() throws IOException {
        EnvSettings envSettings = EnvSettingsFactory.getInstance(InstanceHolder.INSTANCE.environment);
        InstanceHolder.INSTANCE.cipherDetails = envSettings.loadCipherDetails()
                .orElseGet(() -> getbackupCipherDetails(envSettings));
        InstanceHolder.INSTANCE.dbProperties = envSettings.loadDbProperties().orElseGet(Properties::new);
    }

    private static CipherDetails getbackupCipherDetails(EnvSettings envSettings) {
        try {
            return envSettings.backupCipherDetails();
        } catch (IOException ex) {
            return new CipherDetails();
        }
    }

    public static ProgramSettings getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public CipherDetails getCipherDetails() {
        return cipherDetails;
    }

    public Properties getDbProperties() {
        return dbProperties;
    }
}
