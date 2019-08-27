package pg.gipter.utils;

import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.Properties;

public final class PasswordUtils {

    private PasswordUtils() { }

    public static void decryptPassword(Properties properties, String propertyKey) {
        try {
            properties.replace(propertyKey, CryptoUtils.decrypt(properties.getProperty(propertyKey)));
        } catch (GeneralSecurityException e) {
            LoggerFactory.getLogger(PasswordUtils.class).warn("Can not decode property. {}", e.getMessage(), e);
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
}
