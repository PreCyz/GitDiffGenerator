package pg.gipter.utils;

import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.Properties;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static void decryptPassword(Properties properties, String propertyKey) {
        try {
            properties.replace(propertyKey, CryptoUtils.decrypt(properties.getProperty(propertyKey)));
        } catch (GeneralSecurityException e) {
            LoggerFactory.getLogger(PasswordUtils.class).warn("Can not decode property. {}", e.getMessage(), e);
        }
    }

    public static String decrypt(String value) {
        try {
            return CryptoUtils.decrypt(value);
        } catch (GeneralSecurityException e) {
            LoggerFactory.getLogger(PasswordUtils.class).warn("Can not decrypt value.", e);
            throw new IllegalArgumentException("Can not decrypt value.");
        }
    }

    public static void encryptPassword(Properties properties, String propertyKey) {
        if (properties.containsKey(propertyKey)) {
            try {
                properties.replace(
                        propertyKey,
                        CryptoUtils.encrypt(properties.getProperty(propertyKey))
                );
            } catch (GeneralSecurityException e) {
                LoggerFactory.getLogger(PasswordUtils.class).warn("Can not decode property. {}", e.getMessage());
            }
        }
    }

    public static String encrypt(String value) {
        try {
            return CryptoUtils.encrypt(value);
        } catch (GeneralSecurityException e) {
            LoggerFactory.getLogger(PasswordUtils.class).warn("Can not encrypt string.", e);
            throw new IllegalArgumentException("Can not decrypt value.");
        }
    }
}
