package pg.gipter.services.keystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractCertificateService implements CertificateService {

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

    protected Path getKeystorePath() {
        String javaHome = System.getProperty("java.home");
        if (StringUtils.nullOrEmpty(javaHome)) {
            logger.error("Can not find java home.");
            throw new IllegalStateException("Can not find java home.");
        }
        final Path path = Paths.get(javaHome, "lib", "security", "cacerts");
        logger.info("Full cacerts path: '{}'", path.toString());
        return path;
    }
}
