package pg.gipter.services.keystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

abstract class AbstractCertificateService implements CertificateService {

    protected final String storepass = "changeit";
    protected final String X_509 = "X.509";
    protected final Logger logger;

    public AbstractCertificateService() {
        logger = LoggerFactory.getLogger(getClass());
    }

    protected Path getKeytoolPath() {
        String javaHome = System.getProperty("java.home");
        if (StringUtils.nullOrEmpty(javaHome)) {
            logger.error("Can't find java home.");
            throw new IllegalStateException("Can't find java home.");
        }
        return Paths.get(javaHome, "bin", "keytool");
    }

    @Override
    public Path getKeystorePath() {
        String javaHome = System.getProperty("java.home");
        if (StringUtils.nullOrEmpty(javaHome)) {
            logger.error("Can not find java home.");
            throw new IllegalStateException("Can not find java home.");
        }
        final Path path = Paths.get(javaHome, "lib", "security", "keystore.jks");
        logger.info("Full cacerts path: '{}'", path.toString());
        return path;
    }

    @Override
    public List<CertImportResult> automaticImport() {
        List<CertImportResult> importResults = new LinkedList<>();
        final String certFolder = JarHelper.certFolder();
        final File certs = Paths.get(certFolder).toFile();
        if (certs.isDirectory() && certs.exists()) {
            for (File cert : certs.listFiles()) {
                try {
                    final CertImportStatus certImportStatus = addCertificate(cert.getAbsolutePath(), cert.getName());
                    importResults.add(new CertImportResult(cert.getName(), certImportStatus));
                    logger.info("Certificate [{}] added to the [{}] keystore.", cert.getName(), getKeystorePath().toString());
                } catch (Exception ex) {
                    logger.error("Certificate {} not imported.", cert.getName(), ex);
                    importResults.add(new CertImportResult(cert.getName(), CertImportStatus.FAILED, ex.getMessage()));
                }
            }
        } else {
            logger.info("There is no certs to import.");
        }
        return importResults;
    }

    @Override
    public boolean hasCertToImport() {
        final String certFolder = JarHelper.certFolder();
        final File certDir = Paths.get(certFolder).toFile();
        return certDir.exists() && certDir.listFiles() != null && certDir.listFiles().length > 0;
    }

    @Override
    public abstract CertImportStatus addCertificate(String certPath, String alias) throws Exception;

    @Override
    public abstract CertImportStatus removeCertificate(String alias) throws Exception;

    @Override
    public abstract Map<String, String> getCertificates() throws Exception;
}
