package pg.gipter.services.keystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

abstract class AbstractCertificateService implements CertificateService {

    protected final static String storepass = "changeit";
    protected final static String X_509 = "X.509";
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
        final Path certs = Paths.get(certFolder);
        if (Files.exists(certs) && Files.isDirectory(certs)) {
            try {
                for (Path cert : Files.list(certs).collect(toList())) {
                    final String fileName = cert.getFileName().toString();
                    try {
                        final CertImportStatus certImportStatus = addCertificate(cert.toAbsolutePath().toString(), fileName);
                        importResults.add(new CertImportResult(fileName, certImportStatus));
                        logger.info("Certificate [{}] added to the [{}] keystore.", fileName, getKeystorePath().toString());
                    } catch (Exception ex) {
                        logger.error("Certificate {} not imported.", fileName, ex);
                        importResults.add(new CertImportResult(fileName, CertImportStatus.FAILED, ex.getMessage()));
                    }
                }
            } catch (IOException ex) {
                importResults = Collections.emptyList();
            }
        } else {
            logger.info("There is no certs to import.");
        }
        return importResults;
    }

    @Override
    public boolean hasCertToImport() {
        final String certFolder = JarHelper.certFolder();
        final Path certDir = Paths.get(certFolder);
        List<Path> fileList = Collections.emptyList();
        try {
            fileList = Files.list(certDir).collect(toList());
        } catch (IOException ex) {
            logger.info("Directory does not contain any files.");
        }
        return Files.exists(certDir) && !fileList.isEmpty();
    }

    @Override
    public abstract CertImportStatus addCertificate(String certPath, String alias) throws Exception;

    @Override
    public abstract CertImportStatus removeCertificate(String alias) throws Exception;

    @Override
    public abstract Map<String, String> getCertificates() throws Exception;
}
