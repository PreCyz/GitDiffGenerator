package pg.gipter.services.keystore;

public class CertImportResult {
    private final String certName;
    private final CertImportStatus status;
    private final String errorMsg;

    public CertImportResult(String certName, CertImportStatus status) {
        this(certName, status, null);
    }

    public CertImportResult(String certName, CertImportStatus status, String errorMsg) {
        this.certName = certName;
        this.status = status;
        this.errorMsg = errorMsg;
    }

    public String getCertName() {
        return certName;
    }

    public CertImportStatus getStatus() {
        return status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
