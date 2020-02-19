package pg.gipter.core.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.security.CipherDetails;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class SecurityDaoImpl implements SecurityDao {

    private final Logger logger = LoggerFactory.getLogger(SecurityDaoImpl.class);

    @Override
    public Optional<CipherDetails> readCipherDetails() {
        try (InputStream fis = new FileInputStream(DaoConstants.SECURITY_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            return Optional.of(new Gson().fromJson(reader, CipherDetails.class));
        } catch (IOException | NullPointerException e) {
            logger.warn("Warning when loading {}. Exception message is: {}", DaoConstants.SECURITY_JSON, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void writeCipherDetails(CipherDetails cipherDetails) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(cipherDetails, CipherDetails.class);
        try (OutputStream os = new FileOutputStream(DaoConstants.SECURITY_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
            logger.info("File {} saved.", DaoConstants.SECURITY_JSON);
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}", DaoConstants.SECURITY_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }
}
