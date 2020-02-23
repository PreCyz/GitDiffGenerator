package pg.gipter.core.dao.configuration;

import pg.gipter.core.dto.CipherDetails;

import java.util.Optional;

public interface SecurityProvider {
    Optional<CipherDetails> readCipherDetails();
    void writeCipherDetails(CipherDetails cipherDetails);
}
