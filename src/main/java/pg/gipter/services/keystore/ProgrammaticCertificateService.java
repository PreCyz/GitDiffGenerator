package pg.gipter.services.keystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.*;

class ProgrammaticCertificateService extends AbstractCertificateService {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticCertificateService.class);

    @Override
    public CertImportStatus addCertificate(String certPath, String alias) throws Exception {
        final Path cacertPath = getKeystorePath();
        try (InputStream certIn = ClassLoader.class.getResourceAsStream(certPath);
             InputStream localCertIn = new FileInputStream(cacertPath.toFile())) {

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(localCertIn, storepass.toCharArray());
            if (!keystore.containsAlias(alias)) {

                try (BufferedInputStream bis = new BufferedInputStream(certIn);
                     OutputStream out = new FileOutputStream(cacertPath.toFile())) {

                    CertificateFactory cf = CertificateFactory.getInstance(X_509);
                    while (bis.available() > 0) {
                        Certificate cert = cf.generateCertificate(bis);
                        keystore.setCertificateEntry(alias, cert);
                    }
                    keystore.store(out, storepass.toCharArray());
                }

            } else {
                logger.info("Alias [{}] is already in the store.", alias);
                return CertImportStatus.ALREADY_IMPORTED;
            }
        }
        return CertImportStatus.SUCCESS;
    }

    @Override
    public CertImportStatus removeCertificate(String alias) throws Exception {
        InputStream localCertIn = new FileInputStream(getKeystorePath().toFile());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(localCertIn, storepass.toCharArray());
        keystore.deleteEntry(alias);
        return CertImportStatus.SUCCESS;
    }

    @Override
    public Map<String, String> getCertificates() throws Exception {
        Map<String, String> result = new LinkedHashMap<>();

        FileInputStream is = new FileInputStream(getKeystorePath().toFile());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, storepass.toCharArray());

        final Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String keyAlias = aliases.nextElement();
            Optional<Key> ssoSigningKey = Optional.ofNullable(keystore.getKey(keyAlias, storepass.toCharArray()));
            ssoSigningKey.ifPresent(System.out::println);
            Certificate certificate = keystore.getCertificate(keyAlias);
            result.put(keyAlias, certificate.toString());
        }

        return result;
    }

}
