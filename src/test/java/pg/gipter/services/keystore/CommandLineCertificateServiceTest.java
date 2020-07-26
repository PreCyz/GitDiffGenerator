package pg.gipter.services.keystore;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommandLineCertificateServiceTest {

    @Test
    void whenGetKeytoolPath_thenReturnProperPath() {
        final String keytoolPath = new CommandLineCertificateService().getKeytoolPath().toString();

        assertThat(keytoolPath).isNotNull();
        assertThat(keytoolPath).startsWith(System.getProperty("java.home"));
        assertThat(keytoolPath).endsWith("bin" + File.separatorChar + "keytool");
    }

    @Test
    @Disabled
    void givenKeystore_whenGetCertificates_thenReturnListOfCerts() throws IOException {
        final Map<String, String> certificates = new CommandLineCertificateService().getCertificates();

        assertThat(certificates.size()).isGreaterThan(0);
        certificates.forEach((k, v) -> System.out.printf("Alias: %s%n%s%n", k, v));
    }
}