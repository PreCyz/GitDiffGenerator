package pg.gipter.services.keystore;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommandLineCertificateServiceTest {

    @Test
    void whenGetKeytoolPath_thenReturnProperPath() {
        final String keytoolPath = new CommandLineCertificateService().getKeytoolPath().toString();

        assertThat(keytoolPath).isNotNull();
        assertThat(keytoolPath).startsWith(SystemUtils.javaHome());
        assertThat(keytoolPath).endsWith(Paths.get("bin", "keytool").toString());
    }

    @Test
    @Disabled
    void givenKeystore_whenGetCertificates_thenReturnListOfCerts() throws IOException {
        final Map<String, String> certificates = new CommandLineCertificateService().getCertificates();

        assertThat(certificates.size()).isGreaterThan(0);
        certificates.forEach((k, v) -> System.out.printf("Alias: %s%n%s%n", k, v));
    }
}