package pg.gipter.converter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.dao.DaoConstants;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.SecurityDao;
import pg.gipter.security.CipherDetails;
import pg.gipter.settings.ArgName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConverterTest {

    private SecurityConverter converter;

    private void prepareApplicationProperties() throws GeneralSecurityException {
        Properties properties = new Properties();
        properties.setProperty(ArgName.toolkitPassword.name(), "uPb2PFrLB6TLM8mp1HNORA\u003d\u003d");
        properties.setProperty(ArgName.toolkitUsername.name(), "someUser");
        DaoFactory.getPropertiesDao().saveToolkitSettings(properties);
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.SECURITY_JSON));
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenNoJsonSecurity_whenConvert_thenCreateSecurityJsonFile() throws GeneralSecurityException {
        prepareApplicationProperties();
        converter = new SecurityConverter();

        boolean actual = converter.convert();

        SecurityDao securityDao = DaoFactory.getSecurityDao();
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        assertThat(actual).isTrue();
        assertThat(cipherDetails.isPresent()).isTrue();
        assertThat(cipherDetails.get().getCipher()).isEqualTo("PBEWithMD5AndDES");
        assertThat(cipherDetails.get().getIterationCount()).isBetween(0, 50);
        assertThat(cipherDetails.get().getKeySpecValue()).isNotBlank();
        assertThat(cipherDetails.get().getKeySpec()).hasSizeGreaterThan(0);
        assertThat(cipherDetails.get().getSaltValue()).isNotBlank();
        assertThat(cipherDetails.get().getSalt()).hasSizeGreaterThan(0);
    }

    @Test
    void givenNoJsonSecurityAndNoCredentials_whenConvert_thenCreateSecurityJsonFile() {
        converter = new SecurityConverter();

        boolean actual = converter.convert();

        SecurityDao securityDao = DaoFactory.getSecurityDao();
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        assertThat(actual).isFalse();
        assertThat(cipherDetails.isPresent()).isTrue();
        assertThat(cipherDetails.get().getCipher()).isEqualTo("PBEWithMD5AndDES");
        assertThat(cipherDetails.get().getIterationCount()).isBetween(0, 50);
        assertThat(cipherDetails.get().getKeySpecValue()).isNotBlank();
        assertThat(cipherDetails.get().getKeySpec()).hasSizeGreaterThan(0);
        assertThat(cipherDetails.get().getSaltValue()).isNotBlank();
        assertThat(cipherDetails.get().getSalt()).hasSizeGreaterThan(0);
    }

    @Test
    void givenJsonSecurity_whenConvert_thenDoNothing() {
        CipherDetails givenCipherDetails = new CipherDetails();
        givenCipherDetails.setCipher("someAlgorithm");
        DaoFactory.getSecurityDao().writeCipherDetails(givenCipherDetails);
        converter = new SecurityConverter();

        boolean actual = converter.convert();

        SecurityDao securityDao = DaoFactory.getSecurityDao();
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        assertThat(actual).isFalse();
        assertThat(cipherDetails.isPresent()).isTrue();
        assertThat(cipherDetails.get().getCipher()).isEqualTo("someAlgorithm");
    }
}