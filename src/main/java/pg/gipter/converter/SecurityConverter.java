package pg.gipter.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.SecurityDao;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dto.Configuration;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.security.CipherDetails;
import pg.gipter.utils.CryptoUtils;
import pg.gipter.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class SecurityConverter implements Converter {

    private ConfigurationDao configurationDao;
    private SecurityDao securityDao;

    private final Logger logger = LoggerFactory.getLogger(SecurityConverter.class);

    public SecurityConverter() {
        configurationDao = DaoFactory.getConfigurationDao();
        securityDao = DaoFactory.getSecurityDao();
    }

    @Override
    public boolean convert() {
        boolean result = false;
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        if (cipherDetails.isEmpty()) {
            CipherDetails generatedCipher = generateCipherDetails();
            securityDao.writeCipherDetails(generatedCipher);

            Optional<ToolkitConfig> toolkitConfig = readToolkitConfig();
            if (toolkitConfig.isPresent()) {
                String password = toolkitConfig.get().getToolkitPassword();
                String decryptedPassword = null;
                if (!StringUtils.nullOrEmpty(password) && !ArgName.toolkitPassword.defaultValue().equals(password)) {
                    decryptedPassword = CryptoUtils.decryptSafe(password);
                }

                if (!StringUtils.nullOrEmpty(decryptedPassword)) {
                    toolkitConfig.get().setToolkitPassword(decryptedPassword);
                    configurationDao.saveToolkitConfig(toolkitConfig.get());
                    result = true;
                }
            } else {
                logger.info("There is not toolkit credentials to convert.");
            }
        } else {
            logger.info("Security file detected.");
            result = true;
        }
        return result;
    }

    Optional<ToolkitConfig> readToolkitConfig() {
        Optional<ToolkitConfig> result = Optional.empty();
        try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            Gson gson = new GsonBuilder().create();
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            JsonObject toolkitConfigJsonObject = config.getAsJsonObject(Configuration.TOOLKIT_CONFIG);
            result = Optional.ofNullable(gson.fromJson(toolkitConfigJsonObject, ToolkitConfig.class));
        } catch (IOException | NullPointerException e) {
            logger.warn("Could not read toolkit config. {}", e.getMessage());
        }
        return result;
    }

    CipherDetails generateCipherDetails() {
        final int MAX_ITERATION_COUNT = 50;
        final int ADDITIONAL_VALUE = 1000;
        final String CIPHER = "PBEWithMD5AndDES";

        logger.info("Generating cipher details.");
        Random random = new SecureRandom();
        random.setSeed(ADDITIONAL_VALUE);
        CipherDetails cipherDetails = new CipherDetails();
        cipherDetails.setCipher(CIPHER);
        cipherDetails.setIterationCount(random.nextInt(MAX_ITERATION_COUNT) + 1);
        cipherDetails.setKeySpecValue(UUID.randomUUID().toString());
        cipherDetails.setSaltValue(String.valueOf(System.currentTimeMillis() + random.nextInt(ADDITIONAL_VALUE)));
        logger.info("Cipher details were generated.");

        return cipherDetails;
    }

}
