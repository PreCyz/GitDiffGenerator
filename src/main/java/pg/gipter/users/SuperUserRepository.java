package pg.gipter.users;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import pg.gipter.core.dao.MongoDaoConfig;

import java.util.Optional;

class SuperUserRepository extends MongoDaoConfig implements SuperUserDao {
    protected SuperUserRepository() {
        super(SuperUser.COLLECTION_NAME);
    }

    @Override
    public Optional<SuperUser> getUser() {
        Optional<SuperUser> result = Optional.empty();
        FindIterable<SuperUser> users = collection.find(
                Filters.exists("username", true),
                SuperUser.class
        );

        try (MongoCursor<SuperUser> cursor = users.cursor()) {
            if (cursor.hasNext()) {
                result = Optional.of(cursor.next());
            }
            logger.info("User statistics updated.");
        } catch (Exception ex) {
            logger.error("Could not get super user.", ex);
        }
        return result;
    }
}
