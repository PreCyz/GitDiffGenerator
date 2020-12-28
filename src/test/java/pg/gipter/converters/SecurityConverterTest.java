package pg.gipter.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.SecurityProvider;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.model.ToolkitConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConverterTest {

    private SecurityConverter converter;

    private void prepareApplicationProperties() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitPassword("somePassword");
        toolkitConfig.setToolkitUsername("someUser");
        DaoFactory.getCachedConfiguration().saveToolkitConfig(toolkitConfig);
    }

    @BeforeEach
    void setup() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
            DaoFactory.getCachedConfiguration().resetCache();
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenNoJsonSecurity_whenConvert_thenCreateSecurityJsonFile() {
        prepareApplicationProperties();
        converter = new SecurityConverter();

        boolean actual = converter.convert();

        SecurityProvider securityProvider = DaoFactory.getSecurityProvider();
        Optional<CipherDetails> cipherDetails = securityProvider.readCipherDetails();
        assertThat(actual).isTrue();
        assertThat(cipherDetails.isPresent()).isTrue();
        assertThat(cipherDetails.get().getCipherName()).isEqualTo("PBEWithMD5AndDES");
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

        SecurityProvider securityDao = DaoFactory.getSecurityProvider();
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        assertThat(actual).isFalse();
        assertThat(cipherDetails.isPresent()).isTrue();
        assertThat(cipherDetails.get().getCipherName()).isEqualTo("PBEWithMD5AndDES");
        assertThat(cipherDetails.get().getIterationCount()).isBetween(0, 50);
        assertThat(cipherDetails.get().getKeySpecValue()).isNotBlank();
        assertThat(cipherDetails.get().getKeySpec()).hasSizeGreaterThan(0);
        assertThat(cipherDetails.get().getSaltValue()).isNotBlank();
        assertThat(cipherDetails.get().getSalt()).hasSizeGreaterThan(0);
    }

    @Test
    void givenJsonSecurity_whenConvert_thenDoNothing() {
        CipherDetails givenCipherDetails = new CipherDetails();
        givenCipherDetails.setCipherName("someAlgorithm");
        DaoFactory.getSecurityProvider().writeCipherDetails(givenCipherDetails);
        converter = new SecurityConverter();

        boolean actual = converter.convert();

        SecurityProvider securityDao = DaoFactory.getSecurityProvider();
        Optional<CipherDetails> cipherDetails = securityDao.readCipherDetails();
        assertThat(actual).isTrue();
        assertThat(cipherDetails.isPresent()).isTrue();
        assertThat(cipherDetails.get().getCipherName()).isEqualTo("someAlgorithm");
    }
}