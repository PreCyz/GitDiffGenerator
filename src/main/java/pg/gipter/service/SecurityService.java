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

public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    private SecurityDao securityDao;

    public SecurityService() {
        securityDao = DaoFactory.getSecurityDao();
    }

    public String encrypt(String value) {
        try {
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
        } catch (GeneralSecurityException e) {
            logger.warn("Can not encrypt password. {}", e.getMessage(), e);
        }
        return null;
    }

    public String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String decrypt(String value) {
        try {
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
        } catch (GeneralSecurityException e) {
            logger.warn("Can not decrypt password. {}", e.getMessage(), e);
        }
        return null;
    }

    public byte[] base64Decode(String property) {
        return Base64.getDecoder().decode(property);
    }
}
