package pg.gipter.services.keystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class ProgrammaticCertificateService extends AbstractCertificateService {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticCertificateService.class);

    @Override
    public CertImportStatus addCertificate(String certPath, String alias) throws Exception {
        try (InputStream certIn = ClassLoader.class.getResourceAsStream(certPath)) {

            final Path cacertPath = getKeystorePath();
            try (InputStream localCertIn = new FileInputStream(cacertPath.toFile())) {
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(localCertIn, storepass.toCharArray());
                if (keystore.containsAlias(alias)) {
                    logger.info("Alias [{}] is already in the store.", alias);
                    return CertImportStatus.ALREADY_IMPORTED;
                }

                try (BufferedInputStream bis = new BufferedInputStream(certIn)) {
                    CertificateFactory cf = CertificateFactory.getInstance(X_509);
                    while (bis.available() > 0) {
                        Certificate cert = cf.generateCertificate(bis);
                        keystore.setCertificateEntry(alias, cert);
                    }

                    try (OutputStream out = new FileOutputStream(cacertPath.toFile())) {
                        keystore.store(out, storepass.toCharArray());
                        return CertImportStatus.SUCCESS;
                    }
                }
            }
        }
    }

    @Override
    public CertImportStatus removeCertificate(String alias) throws Exception {
        try (InputStream localCertIn = new FileInputStream(getKeystorePath().toFile())) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(localCertIn, storepass.toCharArray());
            keystore.deleteEntry(alias);
            return CertImportStatus.SUCCESS;
        }
    }

    @Override
    public Map<String, String> getCertificates() throws Exception {
        Map<String, String> result = new LinkedHashMap<>();

        try (FileInputStream is = new FileInputStream(getKeystorePath().toFile())) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, storepass.toCharArray());

            final Enumeration<String> aliasIterator = keystore.aliases();
            while (aliasIterator.hasMoreElements()) {
                String keyAlias = aliasIterator.nextElement();
                Optional<Key> ssoSigningKey = Optional.ofNullable(keystore.getKey(keyAlias, storepass.toCharArray()));
                ssoSigningKey.ifPresent(System.out::println);
                Certificate certificate = keystore.getCertificate(keyAlias);
                result.put(keyAlias, certificate.toString());
            }

            return result;
        }
    }

}
