package pg.gipter.core.dao;

import pg.gipter.security.CipherDetails;

import java.util.Optional;

public interface SecurityDao {
    Optional<CipherDetails> readCipherDetails();
    void writeCipherDetails(CipherDetails cipherDetails);
}
