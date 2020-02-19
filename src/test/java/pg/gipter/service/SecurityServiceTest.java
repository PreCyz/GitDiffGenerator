package pg.gipter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.security.CipherDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityServiceTest {

    private SecurityService securityService;

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.SECURITY_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    private void createCipherDetails() {
        Random random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
        CipherDetails cipherDetails = new CipherDetails();
        cipherDetails.setCipher("PBEWithMD5AndDES");
        cipherDetails.setIterationCount(random.nextInt(1) + 1);
        cipherDetails.setKeySpecValue(String.valueOf(System.currentTimeMillis() + random.nextInt(3)));
        cipherDetails.setSaltValue(UUID.randomUUID().toString());

        DaoFactory.getSecurityDao().writeCipherDetails(cipherDetails);
    }

    @Test
    void givenCipherDetails_whenEncrypt_thenReturnEncryptedValue() throws GeneralSecurityException {
        createCipherDetails();
        securityService = new SecurityService();

        String actual = securityService.encrypt("someValue");

        assertThat(actual).isNotEqualTo("someValue");
    }

    @Test
    void givenDefaultCipherAndCipherDetails_whenEncrypt_thenValuesAreDifferent() throws GeneralSecurityException {
        securityService = new SecurityService();
        String valueToEncrypt = "someValue";
        String actualDefault = securityService.encrypt(valueToEncrypt);

        createCipherDetails();
        String actualCustom = securityService.encrypt(valueToEncrypt);

        assertThat(actualDefault).isNotEqualTo(actualCustom);
    }
}