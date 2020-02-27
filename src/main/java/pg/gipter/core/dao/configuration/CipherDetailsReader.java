package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pg.gipter.core.model.CipherDetails;

import java.util.Optional;

class CipherDetailsReader extends ApplicationJsonReader implements SecurityProvider {

    private CipherDetailsReader() {
        super();
    }

    private static class CipherDetailsReaderHolder {
        private static final CipherDetailsReader INSTANCE = new CipherDetailsReader();
    }

    public static CipherDetailsReader getInstance() {
        return CipherDetailsReaderHolder.INSTANCE;
    }

    @Override
    public Optional<CipherDetails> readCipherDetails() {
        Optional<CipherDetails> cipherDetailsOpt = Optional.empty();
        Configuration configuration = readJsonConfig();
        if (configuration != null) {
            cipherDetailsOpt = Optional.ofNullable(configuration.getCipherDetails());
        }
        return cipherDetailsOpt;
    }

    @Override
    public void writeCipherDetails(CipherDetails cipherDetails) {
        if (cipherDetails == null) {
            logger.warn("Cipher details is null. I will not save null.");
            return;
        }
        Configuration configuration = Optional.ofNullable(readJsonConfig()).orElseGet(Configuration::new);
        configuration.setCipherDetails(cipherDetails);
        writeJsonConfig(configuration, CipherDetails.class);
    }

    @Override
    protected Optional<Gson> customGsonForSerialization() {
        return Optional.of(new GsonBuilder().setPrettyPrinting().create());
    }

    @Override
    protected Optional<Gson> customGsonForDeserialization() {
        return Optional.of(new GsonBuilder().setPrettyPrinting().create());
    }
}
