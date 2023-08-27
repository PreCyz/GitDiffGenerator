package pg.gipter.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        return SecurityService.getInstance().decrypt(value, getSuperUser().getCipherDetails());
    }

    public String getUserName() {
        return decryptSuper(getSuperUser().getUsername());
    }

    public String getPassword() {
        return decryptSuper(getSuperUser().getPassword());
    }

    public boolean isCredentialsAvailable() {
        if (superUser == null) {
            logger.info("Credentials are not available. Trying to extract them.");
            superUser = getSuperUser();
        }
        return superUser != null;
    }
}
