package pg.gipter.core.dao;

import org.junit.jupiter.api.*;
import pg.gipter.security.CipherDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityDaoImplTest {

    private SecurityDaoImpl securityDao;

    @BeforeEach
    void setUp() {
        securityDao = new SecurityDaoImpl();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.SECURITY_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenDao_whenWriteCipherDetails_thenFileExists() {
        securityDao.writeCipherDetails(new CipherDetails());

        assertThat(Files.exists(Paths.get(DaoConstants.SECURITY_JSON))).isTrue();
    }

    @Test
    void givenDao_whenReadCipherDetails_thenFileExists() {
        securityDao.writeCipherDetails(new CipherDetails());

        Optional<CipherDetails> actualCipherDetails = securityDao.readCipherDetails();

        assertThat(actualCipherDetails.isPresent()).isTrue();
    }
}