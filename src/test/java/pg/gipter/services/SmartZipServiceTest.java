package pg.gipter.services;

import org.junit.jupiter.api.*;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.configuration.ApplicationConfiguration;
import pg.gipter.core.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SmartZipServiceTest {

    private ApplicationConfiguration dao;

    @BeforeEach
    void setUp() {
        try {
            dao = ApplicationConfiguration.getInstance();
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @AfterEach
    void teardown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
            Files.deleteIfExists(Paths.get("applicationProperties.zip"));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
    }

    @Test
    void givenTxtFile_whenZip_thenReturnZippedFile() {
        dao.saveConfiguration(new Configuration(
                new ApplicationConfig(), new ToolkitConfig(), Collections.singletonList(new RunConfig()), null
        ));
        Path fileToZip = Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON);

        Path actualPath = new SmartZipService().zipFile(fileToZip);

        assertThat(actualPath).isNotNull();
        assertThat(actualPath.toFile().exists()).isTrue();
    }
}