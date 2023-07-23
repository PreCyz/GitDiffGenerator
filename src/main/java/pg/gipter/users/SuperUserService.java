package pg.gipter.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.SecurityService;

public class SuperUserService {
    private static final Logger logger = LoggerFactory.getLogger(SuperUserService.class);
    private final SuperUserRepository superUserRepository;
    protected SuperUser superUser;

    private static class InstanceHolder {

        public static final SuperUserService INSTANCE = new SuperUserService();
    }
    private SuperUserService() {
        superUserRepository = new SuperUserRepository();
    }

    public static SuperUserService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private SuperUser getSuperUser() {
        if (superUser == null) {
            superUser = superUserRepository.getUser().orElseThrow(() -> new IllegalStateException("No super user."));
            logger.info("Super user loaded.");
        }
        return superUser;
    }

    private String decryptSuper(String value) {
        CipherDetails cipher = new CipherDetails();
        cipher.setCipherName("PBEwithSHA1AndDESede");
        cipher.setIterationCount(10);
        cipher.setKeySpecValue("97742dd8-9e89-492a-a2cd-15fee66e64da");
        cipher.setSaltValue("1689777695012");
        return SecurityService.getInstance().decrypt(value, cipher);
    }

    public String getUserName() {
        return decryptSuper(getSuperUser().getUsername());

    }

    public String getPassword() {
        return decryptSuper(getSuperUser().getPassword());
    }

    public boolean isCredentialsAvailable() {
        return superUser != null;
    }
}
