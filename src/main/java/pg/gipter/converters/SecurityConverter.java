package pg.gipter.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.CachedConfiguration;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.SecurityService;

import java.util.Optional;

public class SecurityConverter implements Converter {

    private final CachedConfiguration cachedConfiguration;
    private final SecurityService securityService;

    private final Logger logger = LoggerFactory.getLogger(SecurityConverter.class);

    public SecurityConverter() {
        cachedConfiguration = DaoFactory.getCachedConfiguration();
        securityService = SecurityService.getInstance();
    }

    @Override
    public boolean convert() {
        Optional<CipherDetails> cipherDetails = securityService.readCipherDetails();
        if (!cipherDetails.isPresent()) {
            CipherDetails generatedCipher = securityService.generateCipherDetails();
            securityService.writeCipherDetails(generatedCipher);
        } else {
            logger.info("Security file detected.");
        }
        return true;
    }

}
