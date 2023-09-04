package pg.gipter;

import pg.gipter.core.model.CipherDetails;
import pg.gipter.env.EnvSettings;
import pg.gipter.env.EnvSettingsFactory;

import java.util.Properties;

public final class ProgramSettings {

    private CipherDetails cipherDetails;
    private Properties dbProperties;
    private Environment environment;

    private ProgramSettings() {}

    private static class InstanceHolder {
        private static final ProgramSettings INSTANCE = new ProgramSettings();
    }

    static void initProgramSettings(Environment env) {
        InstanceHolder.INSTANCE.environment = env;
        initProgramSettings();
    }

    public static void initProgramSettings() {
        if (InstanceHolder.INSTANCE.environment == null) {
            throw new RuntimeException("Environment is not set!");
        }
        EnvSettings instance = EnvSettingsFactory.getInstance(InstanceHolder.INSTANCE.environment);
        InstanceHolder.INSTANCE.cipherDetails = instance.loadCipherDetails().orElseThrow(IllegalStateException::new);
        InstanceHolder.INSTANCE.dbProperties = instance.loadDbProperties().orElseThrow(IllegalStateException::new);
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
