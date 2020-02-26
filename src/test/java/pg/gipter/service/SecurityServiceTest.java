package pg.gipter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.SecurityProviderFactory;
import pg.gipter.core.dto.CipherDetails;
import pg.gipter.utils.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityServiceTest {

    private SecurityService securityService = SecurityService.getInstance();

    @BeforeEach
    void setUp() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    private void createCipherDetails() {
        Random random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
        CipherDetails cipherDetails = new CipherDetails();
        cipherDetails.setCipherName("PBEWithMD5AndDES");
        cipherDetails.setIterationCount(random.nextInt(1) + 1);
        cipherDetails.setKeySpecValue(String.valueOf(System.currentTimeMillis() + random.nextInt(3)));
        cipherDetails.setSaltValue(UUID.randomUUID().toString());

        DaoFactory.getSecurityProvider().writeCipherDetails(cipherDetails);
    }

    @Test
    void givenCipherDetails_whenEncrypt_thenReturnEncryptedValue() {
        createCipherDetails();

        String actual = securityService.encrypt("someValue");

        assertThat(actual).isNotEqualTo("someValue");
    }

    @Test
    void givenDefaultCipherAndCipherDetails_whenEncrypt_thenValuesAreDifferent() {
        securityService = SecurityService.getInstance();
        String valueToEncrypt = "someValue";
        String actualDefault = securityService.encrypt(valueToEncrypt);

        createCipherDetails();
        String actualCustom = securityService.encrypt(valueToEncrypt);

        assertThat(actualDefault).isNotEqualTo(actualCustom);
    }

    @Test
    void whenGenerateCipherDetails_thenReturnCipherDetails() {
        CipherDetails actual = securityService.generateCipherDetails();

        assertThat(actual).isNotNull();
        assertThat(actual.getCipherName()).isEqualTo("PBEWithMD5AndDES");
        assertThat(actual.getIterationCount()).isGreaterThanOrEqualTo(1);
        assertThat(actual.getKeySpec()).isNotEmpty();
        assertThat(actual.getSaltValue()).isNotEmpty();
        assertThat(actual.getSalt()).isNotEmpty();
    }

    @Test
    void givenNoCipherDetails_whenDecrypt_thenReturnPasswordWithSimpleDecryption() {
        String password = "somePassword";
        String decryptedPassword = CryptoUtils.encryptSafe(password);

        String actual = securityService.decrypt(decryptedPassword);

        assertThat(actual).isEqualTo(password);
    }

    @Test
    void givenCipherDetails_whenDecrypt_thenReturnPasswordWithSimpleDecryption() {
        CipherDetails cipherDetails = securityService.generateCipherDetails();
        SecurityProviderFactory.getSecurityProvider().writeCipherDetails(cipherDetails);
        String password = "somePassword";
        String decryptedPassword = securityService.encrypt(password, cipherDetails);

        String actual = securityService.decrypt(decryptedPassword);

        assertThat(actual).isEqualTo(password);
    }
}