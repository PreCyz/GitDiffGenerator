package pg.gipter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.SecurityProvider;
import pg.gipter.core.dto.CipherDetails;
import pg.gipter.utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    private static class SecurityServiceHolder {
        public static final SecurityService INSTANCE = new SecurityService();
    }

    private SecurityProvider securityProvider;

    private SecurityService() {
        securityProvider = DaoFactory.getSecurityProvider();
    }

    public static SecurityService getInstance() {
        return SecurityServiceHolder.INSTANCE;
    }

    public String encrypt(String value) {
        try {
            Optional<CipherDetails> cipherDetails = readCipherDetails();
            if (cipherDetails.isPresent()) {
                return encrypt(value, cipherDetails.get());
            } else {
                return CryptoUtils.encrypt(value);
            }
        } catch (GeneralSecurityException e) {
            logger.warn("Can not encrypt password. {}", e.getMessage(), e);
        }
        return ArgName.toolkitPassword.defaultValue();
    }

    public String encrypt(String value, CipherDetails cipherDetails) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cipherDetails.getCipherName());
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(cipherDetails.getKeySpec()));
            Cipher pbeCipher = Cipher.getInstance(cipherDetails.getCipherName());
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(cipherDetails.getSalt(), cipherDetails.getIterationCount()));
            return CryptoUtils.base64Encode(pbeCipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            logger.warn("Can not encrypt password. {}", e.getMessage(), e);
        }
        return ArgName.toolkitPassword.defaultValue();
    }

    public String decrypt(String value) {
        try {
            Optional<CipherDetails> cipherDetails = readCipherDetails();
            if (cipherDetails.isPresent()) {
                return decrypt(value, cipherDetails.get());
            } else {
                return CryptoUtils.decrypt(value);
            }
        } catch (GeneralSecurityException e) {
            logger.warn("Can not decrypt password. {}", e.getMessage(), e);
        }
        return ArgName.toolkitPassword.defaultValue();
    }

    public String decrypt(String value, CipherDetails cipherDetails) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cipherDetails.getCipherName());
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(cipherDetails.getKeySpec()));
            Cipher pbeCipher = Cipher.getInstance(cipherDetails.getCipherName());
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(cipherDetails.getSalt(), cipherDetails.getIterationCount()));
            return new String(pbeCipher.doFinal(CryptoUtils.base64Decode(value)), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            logger.warn("Can not decrypt password. {}", e.getMessage(), e);
        }
        return ArgName.toolkitPassword.defaultValue();
    }

    public Optional<CipherDetails> readCipherDetails() {
        return securityProvider.readCipherDetails();
    }

    public void writeCipherDetails(CipherDetails cipherDetails) {
        securityProvider.writeCipherDetails(cipherDetails);
    }

    public CipherDetails generateCipherDetails() {
        final int MAX_ITERATION_COUNT = 50;
        final int ADDITIONAL_VALUE = 1000;
        final String CIPHER = "PBEWithMD5AndDES";

        logger.info("Generating cipher details.");
        Random random = new SecureRandom();
        random.setSeed(ADDITIONAL_VALUE);
        CipherDetails cipherDetails = new CipherDetails();
        cipherDetails.setCipherName(CIPHER);
        cipherDetails.setIterationCount(random.nextInt(MAX_ITERATION_COUNT) + 1);
        cipherDetails.setKeySpecValue(UUID.randomUUID().toString());
        cipherDetails.setSaltValue(String.valueOf(System.currentTimeMillis() + random.nextInt(ADDITIONAL_VALUE)));
        logger.info("Cipher details were generated.");

        return cipherDetails;
    }
}
