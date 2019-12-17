package pg.gipter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.SecurityDao;
import pg.gipter.security.CipherDetails;
import pg.gipter.utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    private SecurityDao securityDao;

    public SecurityService() {
        securityDao = DaoFactory.getSecurityDao();
    }

    String encrypt(String value) throws GeneralSecurityException {
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        if (cipherDetails.isPresent()) {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cipherDetails.get().getCipher());
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(cipherDetails.get().getKeySpec()));
            Cipher pbeCipher = Cipher.getInstance(cipherDetails.get().getCipher());
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(cipherDetails.get().getSalt(), cipherDetails.get().getIterationCount()));
            return base64Encode(pbeCipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } else {
            return CryptoUtils.encrypt(value);
        }
    }

    public String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    String decrypt(String value) throws GeneralSecurityException {
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        if (cipherDetails.isPresent()) {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cipherDetails.get().getCipher());
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(cipherDetails.get().getKeySpec()));
            Cipher pbeCipher = Cipher.getInstance(cipherDetails.get().getCipher());
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(cipherDetails.get().getSalt(), cipherDetails.get().getIterationCount()));
            return new String(pbeCipher.doFinal(base64Decode(value)), StandardCharsets.UTF_8);
        } else {
            return CryptoUtils.decrypt(value);
        }
    }

    public byte[] base64Decode(String property) {
        return Base64.getDecoder().decode(property);
    }

    public void decryptPassword(Properties properties, String propertyKey) {
        try {
            properties.replace(propertyKey, decrypt(properties.getProperty(propertyKey)));
        } catch (GeneralSecurityException e) {
            logger.warn("Can not decode property. {}", e.getMessage(), e);
        }
    }

    public void encryptPassword(Properties properties, String propertyKey) {
        if (properties.containsKey(propertyKey)) {
            try {
                properties.replace(
                        propertyKey,
                        encrypt(properties.getProperty(propertyKey))
                );
            } catch (GeneralSecurityException e) {
                logger.warn("Can not decode property. {}", e.getMessage());
            }
        }
    }
}
