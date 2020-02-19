package pg.gipter.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.SecurityDao;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.security.CipherDetails;
import pg.gipter.service.SecurityService;
import pg.gipter.utils.StringUtils;

import java.security.SecureRandom;
import java.util.*;

public class SecurityConverter implements Converter {

    private ConfigurationDao configurationDao;
    private SecurityDao securityDao;
    private SecurityService securityService;

    private final Logger logger = LoggerFactory.getLogger(SecurityConverter.class);

    public SecurityConverter() {
        configurationDao = DaoFactory.getConfigurationDao();
        securityDao = DaoFactory.getSecurityDao();
        securityService = new SecurityService();
    }

    @Override
    public boolean convert() {
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        if (cipherDetails.isEmpty()) {
            ToolkitConfig toolkitConfig = configurationDao.loadToolkitConfig();
            String password = toolkitConfig.getToolkitPassword();
            String decryptedPassword = null;
            if (!StringUtils.nullOrEmpty(password) && !password.equals(ArgName.toolkitPassword.defaultValue())) {
                decryptedPassword = securityService.decrypt(password);
            }

            CipherDetails generatedCipher = generateCipherDetails();
            securityDao.writeCipherDetails(generatedCipher);

            if (!StringUtils.nullOrEmpty(decryptedPassword)) {
                toolkitConfig.setToolkitPassword(decryptedPassword);
                configurationDao.saveToolkitConfig(toolkitConfig);
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
