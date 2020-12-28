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
        InputStream certIn = ClassLoader.class.getResourceAsStream(certPath);

        final Path cacertPath = getKeystorePath();
        InputStream localCertIn = new FileInputStream(cacertPath.toFile());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(localCertIn, storepass.toCharArray());
        if (keystore.containsAlias(alias)) {
            logger.info("Alias [{}] is already in the store.", alias);
            certIn.close();
            localCertIn.close();
            return CertImportStatus.ALREADY_IMPORTED;
        }
        localCertIn.close();

        BufferedInputStream bis = new BufferedInputStream(certIn);
        CertificateFactory cf = CertificateFactory.getInstance(X_509);
        while (bis.available() > 0) {
            Certificate cert = cf.generateCertificate(bis);
            keystore.setCertificateEntry(alias, cert);
        }
        certIn.close();

        OutputStream out = new FileOutputStream(cacertPath.toFile());
        keystore.store(out, storepass.toCharArray());
        out.close();
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

        final Iterator<String> aliasIterator = keystore.aliases().asIterator();
        while (aliasIterator.hasNext()) {
            String keyAlias = aliasIterator.next();
            Optional<Key> ssoSigningKey = Optional.ofNullable(keystore.getKey(keyAlias, storepass.toCharArray()));
            ssoSigningKey.ifPresent(System.out::println);
            Certificate certificate = keystore.getCertificate(keyAlias);
            result.put(keyAlias, certificate.toString());
        }

        return result;
    }

}
