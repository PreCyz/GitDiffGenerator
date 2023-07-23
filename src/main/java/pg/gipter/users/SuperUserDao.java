package pg.gipter.users;

import java.util.Optional;

public interface SuperUserDao {
    Optional<SuperUser> getUser();
}
