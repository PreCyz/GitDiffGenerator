package pg.gipter.services.keystore;

import java.util.List;
import java.util.Map;

public interface CertificateService {
    CertImportStatus addCertificate(String certPath, String alias) throws Exception;
    CertImportStatus removeCertificate(String alias) throws Exception;
    Map<String, String> getCertificates() throws Exception;
    List<CertImportResult> automaticImport();
    boolean hasCertToImport();
}
