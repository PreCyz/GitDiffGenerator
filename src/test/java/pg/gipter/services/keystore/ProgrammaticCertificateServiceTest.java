package pg.gipter.services.keystore;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProgrammaticCertificateServiceTest {

    @Test
    void whenGetCecartsFile_thenReturnPath() {
        final Path cacertsPath = new ProgrammaticCertificateService().getKeystorePath();

        assertThat(cacertsPath).isNotNull();
        assertThat(cacertsPath.toFile().exists()).isTrue();
        assertThat(cacertsPath.toString())
                .endsWith("lib" + File.separatorChar + "security" + File.separatorChar + "cacerts");
    }

    @Test
    void givenKeystore_whenGetCertificates_thenReturnListOfCerts() throws Exception {
        Map<String, String> actual = new ProgrammaticCertificateService().getCertificates();

        assertThat(actual).isNotNull();
        actual.forEach((k, v) -> System.out.printf("Alias: %s%n%s%n", k, v));
    }
}