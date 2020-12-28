package pg.gipter.services.keystore;

public final class CertificateServiceFactory {

    public static CertificateService getInstance(boolean isCmdInstance) {
        if (isCmdInstance) {
            return new CommandLineCertificateService();
        }
        return new ProgrammaticCertificateService();
    }
}
