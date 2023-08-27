package pg.gipter.core.dao.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.model.CipherDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityDaoImplTest {

    private CipherDetailsReader securityDao;

    @BeforeEach
    void setUp() {
        securityDao = CipherDetailsReader.getInstance();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenDao_whenWriteCipherDetails_thenFileExists() {
        securityDao.writeCipherDetails(new CipherDetails());

        assertThat(Files.exists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON))).isTrue();
    }

    @Test
    void givenDao_whenReadCipherDetails_thenFileExists() {
        securityDao.writeCipherDetails(new CipherDetails());

        Optional<CipherDetails> actualCipherDetails = securityDao.readCipherDetails();

        assertThat(actualCipherDetails.isPresent()).isTrue();
    }
}