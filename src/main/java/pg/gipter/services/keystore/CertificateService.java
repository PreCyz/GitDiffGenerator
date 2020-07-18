package pg.gipter.services.keystore;

import java.util.Map;

public interface CertificateService {
    void addCertificate(String certPath, String alias) throws Exception;
    Map<String, String> getCertificates() throws Exception;
}
