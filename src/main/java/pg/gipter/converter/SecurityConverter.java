package pg.gipter.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.dao.SecurityDao;
import pg.gipter.security.CipherDetails;
import pg.gipter.service.SecurityService;
import pg.gipter.settings.ArgName;
import pg.gipter.utils.StringUtils;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class SecurityConverter implements Converter {

    private PropertiesDao propertiesDao;
    private SecurityDao securityDao;
    private SecurityService securityService;

    private final Logger logger = LoggerFactory.getLogger(SecurityConverter.class);

    public SecurityConverter() {
        propertiesDao = DaoFactory.getPropertiesDao();
        securityDao = DaoFactory.getSecurityDao();
        securityService = new SecurityService();
    }

    @Override
    public boolean convert() {
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        if (!cipherDetails.isPresent()) {
            Properties properties = propertiesDao.loadToolkitCredentials();
            String password = properties.getProperty(ArgName.toolkitPassword.name());
            String decryptedPassword = null;
            if (!StringUtils.nullOrEmpty(password) && !password.equals(ArgName.toolkitPassword.defaultValue())) {
                decryptedPassword = securityService.decrypt(password);
            }

            CipherDetails generatedCipher = generateCipherDetails();
            securityDao.writeCipherDetails(generatedCipher);

            if (!StringUtils.nullOrEmpty(decryptedPassword)) {
                properties.replace(ArgName.toolkitPassword.name(), securityService.encrypt(decryptedPassword));
                propertiesDao.saveToolkitSettings(properties);
                return true;
            } else {
                return false;
            }
        }
        logger.info("Security file detected.");
        return false;
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
