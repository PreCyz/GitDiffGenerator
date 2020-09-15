package pg.gipter.services.keystore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProgrammaticCertificateServiceTest {

    @AfterEach
    void tearDown() {
        try {
            Files.walk(Paths.get("target", "cert"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.printf("There is something weird going on. %s%n", e.getMessage());
        }
    }

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

    @Test
    void givenCertFolderAndOneCertFile_whenAutomaticImport_thenAddCertificate() throws Exception {
        Files.createDirectory(Paths.get("target", "cert"));
        final Path cert = Files.createFile(Paths.get("target", "cert", "testCert.crt"));
        assertThat(cert.toFile().exists()).isTrue();

        final ProgrammaticCertificateService serviceSpy = spy(new ProgrammaticCertificateService());
        doReturn(CertImportStatus.SUCCESS).when(serviceSpy).addCertificate(anyString(), anyString());

        final List<CertImportResult> actual = serviceSpy.automaticImport();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getCertName()).isEqualTo("testCert.crt");
        verify(serviceSpy, times(1)).addCertificate(anyString(), anyString());
    }
}